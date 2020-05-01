import java.util.concurrent.Executors
import java.sql.Timestamp
import java.time.LocalDateTime

import cats._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.util.transactor.Transactor.Aux
import scala.io.Source
import scala.concurrent.ExecutionContext

class RaidersDB private (conf: RaidersDB.Conf) {
  private val executionContext      = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val cs: ContextShift[IO] = IO.contextShift(executionContext)

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",   // driver classname
    "jdbc:postgresql:raiders", // connect URL (driver-specific)
    conf.username,             // user
    conf.password,             // password
    Blocker.liftExecutionContext(executionContext)
  )

  private val playersSql        = Fragment.const(Source.fromResource(RaidersDB.playersFilename).mkString)
  private val matchesSql        = Fragment.const(Source.fromResource(RaidersDB.matchesFilename).mkString)
  private val queueSql          = Fragment.const(Source.fromResource(RaidersDB.queueFilename).mkString)
  private val matchedPlayersSql = Fragment.const(Source.fromResource(RaidersDB.matchedPlayersFilename).mkString)

  def migrate: IO[Unit] = for {
    _ <- playersSql.stripMargin.update.run.transact(xa)
    _ <- matchesSql.stripMargin.update.run.transact(xa)
    _ <- matchedPlayersSql.stripMargin.update.run.transact(xa)
    _ <- queueSql.stripMargin.update.run.transact(xa)
  } yield ()
}

object RaidersDB {
  final val playersFilename        = "migrations/players.sql"
  final val matchesFilename        = "migrations/matches.sql"
  final val queueFilename          = "migrations/queue.sql"
  final val matchedPlayersFilename = "migrations/matched_players.sql"

  def apply(conf: Conf): RaidersDB = new RaidersDB(conf)

  def players(implicit raidersDB: RaidersDB): Entities.Players = new Entities.Players {
    override def add(player: Player): IO[Int] =
      sql"insert into players (skill) values (${player.skill}) returning id".update
        .withUniqueGeneratedKeys[Int]("id")
        .transact(raidersDB.xa)

    override def remove(id: Int): IO[Int] =
      sql"delete from players where id=$id".update.run.transact(raidersDB.xa)

    override def setSkill(id: Int, skill: Int): IO[Int] =
      sql"update players set skill=$skill where id=$id".update.run.transact(raidersDB.xa)

    override def skill(id: Int): IO[Option[Int]] = ???
  }

  def queue(implicit raidersDB: RaidersDB): Entities.Queue = new Entities.Queue {
    override def add(playerId: Int): IO[Int] =
      sql"insert into queue (player_id, request_time) values ($playerId, ${Timestamp.valueOf(LocalDateTime.now())})".update.run
        .transact(raidersDB.xa)

    override def remove(playerId: Int): IO[Int] = ???

    override def has(playerId: Int): IO[Boolean] = ???

    override def entries(): IO[List[Entities.Queue.Entry]] = ???
  }

  def matchedPlayers(implicit raidersDB: RaidersDB): Entities.MatchedPlayers = new Entities.MatchedPlayers {

    override def remove(playerId: Int): IO[Int] = ???

    override def matchId(playerId: Int): IO[Option[Int]] = ???

    override def players(matchId: Int): IO[List[Int]] = ???

    override def add(playerId: Int, matchId: Int): IO[Int] = ???
  }

  def matches(implicit raidersDB: RaidersDB): Entities.Matches = new Entities.Matches {
    override def add(): IO[Int] = ???

    override def remove(matchId: Int): IO[Int] = ???
  }

  def entityProvider(implicit raidersDB: RaidersDB): EntityProvider = new EntityProvider {
    override def players: Entities.Players = RaidersDB.players

    override def queue: Entities.Queue = RaidersDB.queue

    override def matches: Entities.Matches = RaidersDB.matches

    override def matchedPlayers: Entities.MatchedPlayers = RaidersDB.matchedPlayers
  }

  case class Conf(
      username: String,
      password: String,
  )
}
