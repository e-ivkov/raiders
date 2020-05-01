import cats._
import cats.effect._
import cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.LocalDateTime

class MatchmakerSpec extends AnyFlatSpec with Matchers {
  "players" should "be matched by the end of time limit" in {
    val conf    = Matchmaker.Conf(0.5f, 10, 100)
    val players = List(Matchmaker.Entry(0, 0, LocalDateTime.MIN), Matchmaker.Entry(1, 100, LocalDateTime.MIN))
    Matchmaker.oneVsOneMatchmaker(conf).findMatch(players, LocalDateTime.MIN.plusSeconds(10)) shouldBe Some(List(0, 1))
  }

  "players with no skill diff" should "be matched" in {
    val conf    = Matchmaker.Conf(0.5f, 10, 100)
    val players = List(Matchmaker.Entry(0, 0, LocalDateTime.MIN), Matchmaker.Entry(1, 0, LocalDateTime.MIN))
    Matchmaker.oneVsOneMatchmaker(conf).findMatch(players, LocalDateTime.MIN) shouldBe Some(List(0, 1))
  }
}
