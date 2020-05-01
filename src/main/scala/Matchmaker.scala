import Entities.Queue
import cats._
import cats.effect._
import cats.implicits._
import scala.math.max
import java.time.{LocalDateTime, ZoneOffset}

trait Matchmaker {
  def makeMatches(entityProvider: EntityProvider): IO[Unit] = {
    def getQueue = {
      for {
        queueEntities <- entityProvider.queue.entries()
        matchmakingEntities <- queueEntities
                                .map(entry =>
                                  for {
                                    Some(skill) <- entityProvider.players.skill(entry.playerId)
                                  } yield Matchmaker.Entry(entry.playerId, skill, entry.timestamp)
                                )
                                .sequence
      } yield matchmakingEntities
    }
    for {
      queue                       <- getQueue
      currentTime                 <- LocalDateTime.now().pure[IO]
      Some(foundMatch: List[Int]) <- findMatch(queue, currentTime).pure[IO]
      matchId                     <- entityProvider.matches.add()
      _ <- foundMatch
            .map(playerId =>
              for {
                _ <- entityProvider.queue.remove(playerId)
                _ <- entityProvider.matchedPlayers.add(playerId, matchId)
              } yield ()
            )
            .sequence
      _ <- makeMatches(entityProvider)
    } yield ()
  }

  def findMatch(queue: List[Matchmaker.Entry], currentTime: LocalDateTime): Option[List[Int]]
}

object Matchmaker {
  case class Entry(playerId: Int, skill: Int, searchStart: LocalDateTime)

  case class Conf(tolerance: Float, timeLimitSec: Long, maxSkill: Int)

  def oneVsOneMatchmaker(conf: Conf): Matchmaker =
    (queue: List[Matchmaker.Entry], currentTime: LocalDateTime) => {
      def isMatch(first: Matchmaker.Entry, second: Matchmaker.Entry): Boolean = {
        val skillDiffRatio = (first.skill - second.skill).abs / conf.maxSkill
        val timeSpent = (currentTime.toEpochSecond(ZoneOffset.UTC) - max(
          first.searchStart.toEpochSecond(ZoneOffset.UTC),
          second.searchStart.toEpochSecond(ZoneOffset.UTC)
        ))
        val timeSpentRatio: Double = if (timeSpent < conf.timeLimitSec) timeSpent / conf.timeLimitSec else 1.0
        val k                      = skillDiffRatio * (1.0 - timeSpentRatio)
        k <= conf.tolerance
      }
      val pairs = for (first <- queue; second <- queue) yield (first, second)
      pairs
        .find(pair => isMatch(pair._1, pair._2) && pair._1.playerId != pair._2.playerId)
        .map(pair => List(pair._1.playerId, pair._2.playerId))
    }
}
