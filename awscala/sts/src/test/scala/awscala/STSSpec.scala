package awscala

import awscala._, sts._

import org.slf4j._
import org.scalatest._

class STSSpec extends FlatSpec with Matchers {

  behavior of "STS"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val sts = STS()

    val federation: FederationToken = sts.federationToken(
      name = "anonymous-user",
      policy = Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource("*"))))),
      durationSeconds = 1200
    )

    val signinToken: String = sts.signinToken(federation.credentials)
    log.info(s"SigninToken: ${signinToken}")

    val loginUrl: String = sts.loginUrl(
      credentials = federation.credentials,
      consoleUrl = "https://console.aws.amazon.com/iam",
      issuerUrl = "http://example.com/internal/auth"
    )
    log.info(s"LoginUrl: ${loginUrl}")
  }

}
