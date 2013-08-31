package awscala

import awscala._, ec2._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._

import java.io._

class EC2Spec extends FlatSpec with ShouldMatchers {

  behavior of "EC2"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val ec2 = EC2.at(Region.Tokyo)

    val groupName = s"awscala-unit-test-securitygroup-${System.currentTimeMillis}"
    val groupDescription = "for awscala unit test"
    val keyPairName = s"awscala-unit-test-keypair-${System.currentTimeMillis}"

    //create SecurityGroup
    ec2.createSecurityGroup(groupName, groupDescription).foreach { s =>
      log.info(s"Created SecurityGroup:${s}")
    }

    //create keyPair
    val kpFile: java.io.File = new java.io.File(keyPairName + ".pem")
    val keyPair = ec2.createKeyPair(keyPairName)

    keyPair.material.foreach { m =>
      log.info(s"Created KeyPair:${m}")
      val pw = new PrintWriter(kpFile)
      pw.println(m)
      pw.close()
      sys.process.Process(s"chmod 600 ${kpFile.getAbsolutePath}")
    }

    ec2.run(RunInstancesRequest("ami-2819aa29").withKeyName(keyPairName).withInstanceType("t1.micro").withSecurityGroups(groupName)).headOption.foreach {
      inst =>

        inst.withKeyPair(kpFile) {
          _.ssh {
            client =>
              client.exec("ls -la").right.map { result =>
                log.info(s"Run command on this EC2 instance:${inst.instanceId} Result:\n" + result.stdOutAsString())
              }
          }
        }
        kpFile.delete()

        inst.terminate()

        //Unless  EC2 instance has been terminated, you cannot delete SecurityGroup.
        while (!ec2.instances.exists(i => i.instanceId == inst.instanceId && i.state.getName == "terminated")) {
          Thread.sleep(3000L)
        }
        ec2.deleteKeyPair(keyPairName)
        ec2.deleteSecurityGroup(groupName)
    }
  }
}
