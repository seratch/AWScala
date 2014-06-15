/**
 *
 */
package awscala

/**
 * @author Derrick Burns <derrick.burns@rincaro.com>
 *
 */
import scala.collection.JavaConverters._

trait Sequencer[Item, Result, Marker] {
  def getInitial: Result
  def getMarker(r: Result): Marker
  def getFromMarker(marker: Marker): Result
  def getList(r: Result): java.util.List[Item]

  def sequence: Seq[Item] = {
    case class State[Item](items: List[Item], nextMarker: Option[Marker])

    @scala.annotation.tailrec
    def next(state: State[Item]): (Option[Item], State[Item]) = state match {
      case State(head :: tail, nextMarker) => (Some(head), State(tail, nextMarker))
      case State(Nil, Some(nextMarker)) => {
        val result = getFromMarker(nextMarker)
        next(State[Item](getList(result).asScala.toList, Option(getMarker(result))))
      }
      case State(Nil, None) => (None, state)
    }

    def toStream(state: State[Item]): Stream[Item] =
      next(state) match {
        case (Some(item), nextState) => Stream.cons(item, toStream(nextState))
        case (None, _) => Stream.Empty
      }

    val result = getInitial
    toStream(State(getList(result).asScala.toList, Option(getMarker(result))))
  }
}