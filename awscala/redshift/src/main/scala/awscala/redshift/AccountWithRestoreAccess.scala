package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

case class AccountWithRestoreAccess(accountId: String) extends aws.model.AccountWithRestoreAccess {

  setAccountId(accountId)
}
