package awscala

import awscala._, ec2._

import org.slf4j._
import org.scalatest._

import java.io._

class KeyPairSpec extends FlatSpec with Matchers {

  behavior of "KeyPair"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "properly implement the inherited Java methods" in {
    implicit val ec2 = EC2.at(Region.Tokyo)

    val keyPairName = s"awscala-unit-test-keypair-${System.currentTimeMillis}-${new scala.util.Random().nextInt(100)}"
    val keyPair = ec2.createKeyPair(keyPairName)

    try {
      keyPair.name should be(keyPairName)
      keyPair.fingerprint.size should be > (0)
      // Newly created key pair has material (i.e. private key)
      keyPair.material should be('defined)
      keyPair.material.get.size should be > (0)

      keyPair.getKeyName should equal(keyPair.name)
      keyPair.getKeyFingerprint should equal(keyPair.fingerprint)
      keyPair.getKeyMaterial should equal(keyPair.material.get)

      // Key pair without private key info defined
      KeyPair("my-key", "fingerprint", None).getKeyMaterial should be(null)

    } finally {
      ec2.deleteKeyPair(keyPairName)
    }
  }
}
