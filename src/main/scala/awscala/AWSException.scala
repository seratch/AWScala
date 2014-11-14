package awscala

/**
 * Thrown if an unrecoverable error occurred during an AWS call.
 *
 * Created by salah_hawila on 10/24/14.
 */
class AWSException(msg: String, exception: Throwable = null) extends Exception(msg: String, exception: Throwable) {
}
