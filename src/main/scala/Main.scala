import cats._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.server.blaze._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

case class Player(skill: Int)

object Main extends IOApp {

  implicit val decoder = jsonOf[IO, Player]

  val matchmakingService = HttpRoutes
    .of[IO] {
      case request @ POST -> Root / "player" / "add" =>
        for {
          player   <- request.as[Player]
          id       <- DBConnection.addPlayer(player)
          response <- Ok(s"Added player with skill ${player.skill}. Id: $id")
        } yield response
      case GET -> Root / "player" / IntVar(id) / "remove" =>
        Ok(s"Removed player with id: $id")
      case GET -> Root / "player" / IntVar(id) / "set" / "skill" / IntVar(value) =>
        Ok(s"Set skill for player $id to $value")
      case GET -> Root / "player" / IntVar(id) / "match" / "1vs1" =>
        Ok(s"The queue id is 1 for 1vs1 match")
      case GET -> Root / "queue" / IntVar(id) =>
        Ok(s"Approximate match time for queue $id entry is 100ms")
      case GET -> Root / "match" / IntVar(id) =>
        Ok(s"The players for the match $id have been found: Player1, Player2")
    }
    .orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- DBConnection.init
      blazeServer <- BlazeServerBuilder[IO]
                      .bindHttp(8080, "localhost")
                      .withHttpApp(Main.matchmakingService)
                      .resource
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield blazeServer
  }
}
