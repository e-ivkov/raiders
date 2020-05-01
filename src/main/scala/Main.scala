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
import java.time.LocalDateTime

// Requests
case class Player(skill: Int)

// Responses
case class PlayerAdded(id: Int)

sealed trait SearchStatus
object SearchStatus {

  case class Found(matchId: Int, status: String = "FOUND") extends SearchStatus

  case class Searching(status: String = "SEARCHING") extends SearchStatus

  case class NotStarted(status: String = "SEARCH NOT STARTED") extends SearchStatus

}

case class MatchedPlayers(players: List[Int])

// Configuration

case class ServiceConf(
    db: RaidersDB.Conf,
    matchmaking: Matchmaker.Conf
)

object Main extends IOApp {

  implicit val decoder: EntityDecoder[IO, Player] = jsonOf[IO, Player]

  def matchmakingService(entityProvider: EntityProvider, matchmaker: Matchmaker) = HttpRoutes
    .of[IO] {
      case request @ POST -> Root / "player" / "add" =>
        for {
          player   <- request.as[Player]
          id       <- entityProvider.players.add(player)
          response <- Ok(PlayerAdded(id).asJson)
        } yield response
      case GET -> Root / "player" / IntVar(id) / "remove" =>
        for {
          nLines   <- entityProvider.players.remove(id)
          response <- if (nLines > 0) Ok(s"Removed player with id: $id") else NotFound(s"No player found with id: $id")
        } yield response
      case GET -> Root / "player" / IntVar(id) / "set" / "skill" / IntVar(value) =>
        for {
          nLines <- entityProvider.players.setSkill(id, value)
          response <- if (nLines > 0) Ok(s"Set skill for player $id to $value")
                     else NotFound(s"No player found with id: $id")
        } yield response
      case GET -> Root / "player" / IntVar(id) / "search" / "match" / "1vs1" =>
        for {
          _        <- entityProvider.queue.add(id)
          _        <- matchmaker.findMatch(List(), LocalDateTime.now()).pure[IO]
          response <- Ok(s"Started searching")
        } yield response
      case GET -> Root / "player" / IntVar(id) / "search" / "status" =>
        for {
          queueHas      <- entityProvider.queue.has(id)
          matchIdOption <- entityProvider.matchedPlayers.matchId(id)
          response <- if (queueHas) Ok(SearchStatus.Searching().asJson)
                     else
                       matchIdOption match {
                         case Some(matchId) => Ok(SearchStatus.Found(matchId).asJson)
                         case None          => Ok(SearchStatus.NotStarted().asJson)
                       }
        } yield response
      case GET -> Root / "match" / IntVar(id) / "players" =>
        for {
          players  <- entityProvider.matchedPlayers.players(id)
          response <- Ok(MatchedPlayers(players).asJson)
        } yield response
      case GET -> Root / "match" / IntVar(id) / "accept" =>
        for {
          _ <- entityProvider.matches.remove(id)
          response <- Ok(
                       s"Match $id have been accepted, the data about this match will be deleted."
                     )
        } yield response
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
                      .withHttpApp(
                        Main
                          .matchmakingService(RaidersDB.entityProvider, Matchmaker.oneVsOneMatchmaker(conf.matchmaking))
                      )
                      .resource
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield blazeServer
  }
}
