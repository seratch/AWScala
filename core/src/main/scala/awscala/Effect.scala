package awscala

import com.amazonaws.auth.{ policy => aws }

object Effect {
  val Allow = aws.Statement.Effect.Allow
  val Deny = aws.Statement.Effect.Deny
}
