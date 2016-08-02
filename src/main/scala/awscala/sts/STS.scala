package awscala.sts

import awscala._
import com.amazonaws.services.{ securitytoken => aws }
import com.amazonaws.util.json.Jackson
import java.net._

object STS {
  def apply(credentials: Credentials)(implicit region: Region): STS = new STSClient(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey))
  def apply(credentialsProvider: CredentialsProvider = CredentialsLoader.load()): STS = new STSClient(credentialsProvider)
  def apply(accessKeyId: String, secretAccessKey: String): STS = {
    new STSClient(BasicCredentialsProvider(accessKeyId, secretAccessKey))
  }
}

/**
 * Amazon Security Token Service Java client wrapper
 * @see [[http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/]]
 */
trait STS extends aws.AWSSecurityTokenService {

  def sessionToken: SessionToken = SessionToken(TemporaryCredentials(getSessionToken().getCredentials))

  def sessionToken(serialNumber: String, tokenCode: String, durationSeconds: Int): SessionToken = {
    SessionToken(TemporaryCredentials(getSessionToken(
      new aws.model.GetSessionTokenRequest()
        .withSerialNumber(serialNumber)
        .withTokenCode(tokenCode)
        .withDurationSeconds(durationSeconds)
    ).getCredentials))
  }

  def federationToken(name: String, policy: Policy, durationSeconds: Int): FederationToken = {
    val result = getFederationToken(new aws.model.GetFederationTokenRequest()
      .withName(name)
      .withPolicy(policy.toJSON)
      .withDurationSeconds(durationSeconds))

    FederationToken(
      user = FederatedUser(result.getFederatedUser),
      credentials = TemporaryCredentials(result.getCredentials)
    )
  }

  def decodeAuthorizationMessage(message: String): String = {
    decodeAuthorizationMessage(new aws.model.DecodeAuthorizationMessageRequest().withEncodedMessage(message)).getDecodedMessage
  }

  private[this] val SIGNIN_URL = "https://signin.aws.amazon.com/federation"

  def signinToken(credentials: TemporaryCredentials): String = {
    val sessionJsonValue = s"""{"sessionId":"${credentials.accessKeyId}","sessionKey":"${credentials.secretAccessKey}","sessionToken":"${credentials.sessionToken}"}\n"""
    val url = SIGNIN_URL + "?Action=getSigninToken&SessionType=json&Session=" + java.net.URLEncoder.encode(sessionJsonValue, "UTF-8")
    val response = scala.io.Source.fromURL(new java.net.URL(url)).getLines.mkString("\n")
    Jackson.jsonNodeOf(response).get("SigninToken").asText()
  }

  def loginUrl(credentials: TemporaryCredentials, consoleUrl: String = "https://console.aws.amazon.com/iam", issuerUrl: String = ""): String = {
    val token = URLEncoder.encode(signinToken(credentials), "UTF-8")
    val issuer = URLEncoder.encode(issuerUrl, "UTF-8")
    val destination = URLEncoder.encode(consoleUrl, "UTF-8")
    s"${SIGNIN_URL}?Action=login&SigninToken=${token}&Issuer=${issuer}&Destination=${destination}"
  }

  def assumeRole(id: String, arn: String, sessionKey: String): TemporaryCredentials = {
    val assumeRoleReq = new aws.model.AssumeRoleRequest()
    assumeRoleReq.setExternalId(id)
    assumeRoleReq.setRoleArn(arn)
    assumeRoleReq.setRoleSessionName(sessionKey)
    val response = assumeRole(assumeRoleReq)
    TemporaryCredentials(response.getCredentials)
  }
}

/**
 * Default Implementation
 *
 * @param credentialsProvider credentialsProvider
 */
class STSClient(credentialsProvider: CredentialsProvider = CredentialsLoader.load())
  extends aws.AWSSecurityTokenServiceClient(credentialsProvider)
  with STS
