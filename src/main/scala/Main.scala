import cats.effect._
import org.http4s._
import org.http4s.server.blaze._
import org.http4s.dsl.io._
import org.http4s.implicits._
import cats.implicits._

object Main extends IOApp {

  val matchmakingService = HttpRoutes
    .of[IO] {
      case req @ POST -> Root / "player" / "add" =>
        Ok(s"Added player. Id: 1")
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

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(Main.matchmakingService)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
