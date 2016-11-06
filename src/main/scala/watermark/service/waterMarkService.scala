package watermark.service

import java.util.concurrent.{ExecutorService, ThreadFactory, Executors}
import scala.concurrent.duration._

import org.http4s.headers.`Content-Type`
import org.http4s.MediaType._
import org.http4s.server.blaze.BlazeBuilder
import scalaz.concurrent.{Task, Strategy}
import org.http4s.server.Server
import org.http4s.dsl._
import org.http4s._
import scalaz._, Scalaz._

object waterMarkServer {
  //import waterMarkServiceUtils._

  val errorResponse: PartialFunction[Throwable, Task[Response]] = {
    case t => InternalServerError(t.toString)
  }

  val wms = HttpService {
    case GET -> Root =>
      Ok("Welcome to the watermark service!")
    /*
    case req @ POST -> Root / "watermark" =>
      (for {
        content <- req.as[String]
        event   <- doSomethingWithEvent(content)
        res     <- Ok("Everything's good")
      } yield res).handleWith(errorResponse)

      case req @ POST -> Root / "poll" =>
        (for {
          content <- req.as[String]
          event   <- doSomethingWithEvent(content)
          res     <- Ok("Everything's good")
        } yield res).handleWith(errorResponse)
      */
  }

  val executorService: ExecutorService = {
    Executors.newFixedThreadPool(math.max(4, Runtime.getRuntime.availableProcessors), Strategy.DefaultDaemonThreadFactory)
  }

  def createServer(svc: HttpService): Task[Server] =
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      //.withIdleTimeout(300.seconds)
      .withServiceExecutor(executorService)
      .mountService(svc, "/")
      .start

  def serve: Task[Unit] = {
    for {
      server <- createServer(wms)
      _      <- Task.delay(println("Sever started hit any key to exit"))
      _      <- waitForInput
      _      <- server.shutdown
    } yield ()
  }

  def main(args: Array[String]): Unit =  {
    serve.run
  }

  // Lifted from unfiltered Quasar open source library
  // NB: available() returns 0 when the stream is closed, meaning the server
  //     will run indefinitely when started from a script.
  private def waitForInput: Task[Unit] = for {
  _    <- Task.delay(java.lang.Thread.sleep(250))
                .handle { case _: java.lang.InterruptedException => () }
    test <- Task.delay(Option(System.console).isEmpty || System.in.available() <= 0)
                .handle { case _ => true }
    done <- if (test) waitForInput else Task.now(())
  } yield done
}
