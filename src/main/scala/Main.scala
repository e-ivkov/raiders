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
import pureconfig._
import pureconfig.generic.auto._

case class Player(skill: Int)

object Main extends IOApp {

  implicit val decoder = jsonOf[IO, Player]

  def matchmakingService(players: Entities.Players) = HttpRoutes
    .of[IO] {
      case request @ POST -> Root / "player" / "add" =>
        for {
          player   <- request.as[Player]
          id       <- players.add(player)
          response <- Ok(s"Added player with skill ${player.skill}. Id: $id")
        } yield response
      case GET -> Root / "player" / IntVar(id) / "remove" =>
        for {
          _        <- players.remove(id)
          response <- Ok(s"Removed player with id: $id")
        } yield response
      case GET -> Root / "player" / IntVar(id) / "set" / "skill" / IntVar(value) =>
        for {
          _        <- players.setSkill(id, value)
          response <- Ok(s"Set skill for player $id to $value")
        } yield response
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
      conf <- IO.fromEither(
               ConfigSource.default.load[RaidersDB.Conf].leftMap(e => new Throwable(e.prettyPrint()))
             )
      implicit0(db: RaidersDB) <- IO.pure(RaidersDB(conf))
      _                        <- db.migrate
      blazeServer <- BlazeServerBuilder[IO]
                      .bindHttp(8080, "localhost")
                      .withHttpApp(Main.matchmakingService(RaidersDB.players))
                      .resource
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield blazeServer
  }
}
