import java.util.concurrent.Executors

import cats._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext

class RaidersDB private {
  private val executionContext      = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val cs: ContextShift[IO] = IO.contextShift(executionContext)

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",   // driver classname
    "jdbc:postgresql:raiders", // connect URL (driver-specific)
    "postgres",                // user
    "docker",                  // password
    Blocker.liftExecutionContext(executionContext)
  )

  def migrate = sql"""
                  |create table if not exists players (
                  |    id serial primary key,
                  |    skill int not null
                  |)
       """.stripMargin.update.run.transact(xa)
}

object RaidersDB {
  def apply(): RaidersDB = new RaidersDB()

  def players(implicit raidersDB: RaidersDB): Entities.Players = new Entities.Players {
    override def add(player: Player) =
      sql"insert into players (skill) values (${player.skill}) returning id".update
        .withUniqueGeneratedKeys[Int]("id")
        .transact(raidersDB.xa)

    override def remove(id: Int): IO[Int] = ???
  }
}
