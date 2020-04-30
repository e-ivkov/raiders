import cats.effect.IO

object Entities {
  trait Players {
    def add(player: Player): IO[Int]
    def remove(id: Int): IO[Int]
    def setSkill(id: Int, skill: Int): IO[Int]
  }

  trait Queue {
    def add(playerId: Int): IO[Int]
  }

  trait Matches {}
}
