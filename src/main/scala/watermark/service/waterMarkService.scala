package watermark.service

import java.util.concurrent.{ExecutorService, ThreadFactory, Executors}
import scala.concurrent.duration._

import org.http4s.headers.`Content-Type`
import org.http4s.MediaType._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.client.blaze._
import scalaz.concurrent.{Task, Strategy}
//I may want to run as an app, automatic cleanup/shutdown
import org.http4s.server.{Server, ServerApp}
import org.http4s.dsl._
import org.http4s._
import org.http4s.Uri
import scalaz._, Scalaz._

object waterMarkServer {
  import waterMarkServiceUtils._

  val errorResponse: PartialFunction[Throwable, Task[Response]] = {
    case t =>
      t.printStackTrace()
      InternalServerError(t.toString)
  }

  val client = PooledHttp1Client()
  //Task[String] represents the asynchronous nature of a client request.
  val getTicket = client.expect[String]("http://localhost:8080/ticket")
  val getWaterMark = client.expect[String]("http://localhost:8080/watermark")

  //return a task of response?
  //HttpService is just a type alias for Kleisli[Task, Request, Response]
  val wms = HttpService {
    case GET -> Root =>
      Ok("Welcome to the watermark service!")

    case req @ POST -> Root / "ticket" =>
      (for {
        //content .as[String ]is a Java.lang.String, to.String is a char
        content <- req.as[String]
        //event   <- addToStream(content)
        doc <- stringToDoc(content)
        ticket <- genTicket(doc)
        //_ = println("2")
        res <- Ok(ticket.id.toString)
        _ = println("3")
      } yield res).handleWith(errorResponse)
      
      /*
      case req @ POST -> Root / "waterMark" =>
        (for {
          ticket <- req.as[String]
          res   <- markDocs(ducmentMAp)?
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
    //It is best to run your Task “at the end of the world.”
    serve.run
    //client.shutdownNow()
    //server.shutdownNow()
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
