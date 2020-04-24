import cats.effect.IO

object Entities {
  trait Players {
    def add(player: Player): IO[Int]
    def remove(id: Int): IO[Int]
  }

  trait Queues {}

  trait Matches {}
}
