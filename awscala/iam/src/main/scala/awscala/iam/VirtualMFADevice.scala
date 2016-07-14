package awscala.iam

import awscala._
import com.amazonaws.services.{ identitymanagement => aws }

object VirtualMFADevice {

  def apply(user: User, g: aws.model.MFADevice): VirtualMFADevice = new VirtualMFADevice(
    base32StringSeed = None,
    enabledAt = new DateTime(g.getEnableDate),
    qrCodePng = None,
    serialNumber = g.getSerialNumber,
    user = user
  )
  def apply(g: aws.model.VirtualMFADevice): VirtualMFADevice = new VirtualMFADevice(
    base32StringSeed = Some(g.getBase32StringSeed),
    enabledAt = new DateTime(g.getEnableDate),
    qrCodePng = Some(g.getQRCodePNG),
    serialNumber = g.getSerialNumber,
    user = User(g.getUser)
  )
}

case class VirtualMFADevice(
  serialNumber: String, base32StringSeed: Option[java.nio.ByteBuffer], qrCodePng: Option[java.nio.ByteBuffer],
  user: User, enabledAt: DateTime
)
    extends aws.model.VirtualMFADevice {

  setBase32StringSeed(base32StringSeed.orNull[java.nio.ByteBuffer])
  setEnableDate(enabledAt.toDate)
  setQRCodePNG(qrCodePng.orNull[java.nio.ByteBuffer])
  setSerialNumber(serialNumber)
  setUser(user)

  def enable(user: User, code1: String, code2: String)(implicit iam: IAM) = {
    iam.enableVirtualMFADevice(this, user, code1, code2)
  }
  def disable(user: User)(implicit iam: IAM) = {
    iam.disableVirtualMFADevice(this, user)
  }

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

