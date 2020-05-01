import cats._
import cats.effect._
import cats.implicits._

trait Matchmaker {
  def findMatches(): IO[Unit]
}

object Matchmaker {
  val oneVsOneMatchmaker: Matchmaker = () => ???
}
