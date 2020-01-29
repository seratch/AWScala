package awscala

import awscala._
import ec2._
import org.slf4j._
import org.scalatest._
import java.io._

class EC2Spec extends FlatSpec with Matchers {

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

    ec2.runAndAwait("ami-2819aa29", keyPair).headOption.foreach { instance =>
      instance.withKeyPair(kpFile) { i =>
        i.ssh { client =>
          client.exec("ls -la").map { result =>
            log.info(s"Run command on this EC2 instance:${instance.instanceId} Result:\n" + result.stdOutAsString())
          }
        }
      }
      kpFile.delete()

      instance.terminate()

      //Unless  EC2 instance has been terminated, you cannot delete SecurityGroup.
      while (!ec2.instances.exists(i => i.instanceId == instance.instanceId && i.state.getName == "terminated")) {
        Thread.sleep(3000L)
      }
      ec2.deleteKeyPair(keyPairName)
      ec2.deleteSecurityGroup(groupName)
    }
  }
}
