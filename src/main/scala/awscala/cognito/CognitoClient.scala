package awscala.cognito

import awscala.Credentials
import com.amazonaws.auth.{AnonymousAWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.cognitoidentity.model.GetIdRequest
import com.amazonaws.services.{cognitoidentity => cognito}

/**
  * Basic clent
  */
object CognitoClient {

  def apply(credentials: Credentials) =
    new cognito.AmazonCognitoIdentityClient(new BasicAWSCredentials(credentials.getAWSSecretKey,
      credentials.getAWSAccessKeyId))

  def apply() = new cognito.AmazonCognitoIdentityClient(new AnonymousAWSCredentials())
}

case class CognitoIdentityParams(identityPoolId: String, accountId:String)

trait CognitoClient extends cognito.AmazonCognitoIdentityClient {

  def getIdentityId(params: CognitoIdentityParams): String = {
    val idRequest = new GetIdRequest()
    idRequest.setAccountId(params.accountId)
    idRequest.setIdentityPoolId(params.identityPoolId)
    this.getId(idRequest).getIdentityId
  }
}