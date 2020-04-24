import cats.effect.IO

object Entities {
  trait Players {
    def addPlayer(player: Player): IO[Int]
  }

  trait Queues {}

  trait Matches {}
}
