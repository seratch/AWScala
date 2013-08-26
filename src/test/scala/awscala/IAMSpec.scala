package awscala

import awscala._, iam._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._

class IAMSpec extends FlatSpec with ShouldMatchers {

  behavior of "IAM"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val iam = IAM()

    val groupName = s"awscala-unit-test-group-${System.currentTimeMillis}"
    val userName = s"awscala-unit-test-user-${System.currentTimeMillis}"

    val group: Group = iam.createGroup(groupName)
    log.info(s"Created Group: ${group}")

    val user: User = iam.createUser(userName)
    // user.setLoginPassword("dummy-password-xxx-yyy") // takes long time to prepare
    log.info(s"Created User: ${user}")

    group.add(user)
    group.remove(user)

    val policyName = s"awscala-unit-test-group-policy-${System.currentTimeMillis}"

    import awscala.auth.policy._
    val policy: Policy = Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource("*")))))
    group.putPolicy(policyName, policy.toJSON)

    group.policyNames.foreach { policyName =>
      group.policy(policyName).destroy()
    }
    group.destroy()

    user.destroy()
  }

}
