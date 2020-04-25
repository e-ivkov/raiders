import java.util.concurrent.Executors

import cats._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
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

  private val playersSql = Fragment.const(Source.fromResource("migrations/players.sql").mkString)

  def migrate: IO[Int] = playersSql.stripMargin.update.run.transact(xa)
}

object RaidersDB {
  def apply(conf: Conf): RaidersDB = new RaidersDB(conf)

  def players(implicit raidersDB: RaidersDB): Entities.Players = new Entities.Players {
    override def add(player: Player) =
      sql"insert into players (skill) values (${player.skill}) returning id".update
        .withUniqueGeneratedKeys[Int]("id")
        .transact(raidersDB.xa)

    override def remove(id: Int): IO[Int] = ???
  }

  case class Conf(
      username: String,
      password: String,
  )
}
