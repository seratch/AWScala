package awscala

/**
 * Thrown if an unrecoverable error occurred during an AWS call.
 */
class AWSException(msg: String, exception: Throwable = null) extends Exception(msg: String, exception: Throwable) {
}
