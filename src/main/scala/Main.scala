import cats.effect._
import org.http4s._
import org.http4s.server.blaze._
import org.http4s.dsl.io._
import org.http4s.implicits._
import cats.implicits._

object Main extends IOApp {

  val helloWorldService = HttpRoutes
    .of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }
    .orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(Main.helloWorldService)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
