package awscala.sts

case class FederationToken(user: FederatedUser, credentials: TemporaryCredentials)

