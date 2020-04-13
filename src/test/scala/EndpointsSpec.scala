import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EndpointsSpec extends AnyFlatSpec with Matchers {
  "player" should "be added successfully" in {
    val response = Main.matchmakingService.run(Request(method = Method.POST, uri = uri"/player/add")).unsafeRunSync()
    response.status shouldBe Ok
  }
}
