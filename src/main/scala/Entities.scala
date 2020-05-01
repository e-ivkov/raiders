import cats.effect.IO
import java.time.LocalDateTime

object Entities {
  trait Players {
    def add(player: Player): IO[Int]
    def remove(id: Int): IO[Int]
    def setSkill(id: Int, skill: Int): IO[Int]
  }

  trait Queue {
    def add(playerId: Int): IO[Int]
    def remove(playerId: Int): IO[Int]
    def has(playerId: Int): IO[Boolean]
    def entries(): IO[List[Queue.Entry]]
  }

  object Queue {
    case class Entry(playerId: Int, timestamp: LocalDateTime)
  }

  trait MatchedPlayers {
    def add(playerId: Int): IO[Int]
    def remove(playerId: Int): IO[Int]
    def matchId(playerId: Int): IO[Option[Int]]
    def players(matchId: Int): IO[List[Int]]
  }

  trait Matches {
    def add(playerId: Int): IO[Int]
    def remove(playerId: Int): IO[Int]
  }
}
