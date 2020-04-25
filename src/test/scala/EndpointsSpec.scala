import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import cats._
import cats.effect._
import cats.implicits._

class EndpointsSpec extends AnyFlatSpec with Matchers {
  object TestPlayers extends Entities.Players {
    override def add(player: Player): IO[Int] = 1.pure[IO]

    override def remove(id: Int): IO[Int] = 1.pure[IO]

    override def setSkill(id: Int, skill: Int): IO[Int] = 1.pure[IO]
  }

  "player" should "be added successfully" in {
    val response = Main
      .matchmakingService(TestPlayers)
      .run(Request(method = POST, uri = uri"/player/add").withEntity(Player(100).asJson))
      .unsafeRunSync()
    response.status shouldBe Ok
  }

  "player" should "be removed successfully" in {
    val response = Main
      .matchmakingService(TestPlayers)
      .run(Request(method = GET, uri = uri"/player/1/remove"))
      .unsafeRunSync()
    response.status shouldBe Ok
  }

  "player skill" should "be set successfully" in {
    val response = Main
      .matchmakingService(TestPlayers)
      .run(Request(method = GET, uri = uri"/player/1/set/skill/1"))
      .unsafeRunSync()
    response.status shouldBe Ok
  }
}
