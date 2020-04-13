import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._

class EndpointsSpec extends AnyFlatSpec with Matchers {
  "player" should "be added successfully" in {
    val response = Main.matchmakingService
      .run(Request(method = POST, uri = uri"/player/add").withEntity(Player(100).asJson))
      .unsafeRunSync()
    response.status shouldBe Ok
  }
}
