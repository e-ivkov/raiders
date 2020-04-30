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

case class GameConf(maxSkill: Int)

case class MatchmakingConf(tolerance: Float, timeLimitMs: Int)

case class ServiceConf(
    db: RaidersDB.Conf,
    game: GameConf,
    matchmaking: MatchmakingConf
)

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
      case GET -> Root / "player" / IntVar(id) / "search" / "match" / "1vs1" =>
        Ok(s"Started searching")
      case GET -> Root / "player" / IntVar(id) / "search" / "status" =>
        Ok(
          s"Approximate match time for player $id is 100ms (FOUND - player in matched_players | SEARCHING - player in a queue | NO SEARCH STARTED)"
        )
      case GET -> Root / "match" / IntVar(id) / "players" =>
        Ok(s"The players for the match $id have been found: Player1, Player2")
      case GET -> Root / "match" / IntVar(id) / "accept" =>
        Ok(
          s"Match $id have been accepted, matchmaking entries (matched_players, matches) for these players will be deleted."
        )
      case GET -> Root / "match" / IntVar(id) / "reject" =>
        Ok(
          s"Match $id have been rejected, (matches, matched_players) for these players will be deleted and they will be added back to queue."
        )
    }
    .orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    for {
      conf <- IO.fromEither(
               ConfigSource.default.load[ServiceConf].leftMap(e => new Throwable(e.prettyPrint()))
             )
      implicit0(db: RaidersDB) <- IO.pure(RaidersDB(conf.db))
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
