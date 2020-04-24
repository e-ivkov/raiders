import cats._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

object RaidersDB {

  private implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",                                    // driver classname
    "jdbc:postgresql:raiders",                                  // connect URL (driver-specific)
    "postgres",                                                 // user
    "docker",                                                   // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  def init = sql"""
                  |create table if not exists players (
                  |    id serial primary key,
                  |    skill int not null
                  |)
       """.stripMargin.update.run.transact(xa)

  object Players extends Entities.Players {
    override def addPlayer(player: Player) =
      sql"insert into players (skill) values (${player.skill}) returning id".update
        .withUniqueGeneratedKeys[Int]("id")
        .transact(xa)
  }
}
