/*
package watermark.service

import java.util.concurrent.{ExecutorService, ThreadFactory, Executors}
import scala.concurrent.duration._
import argonaut._
import argonaut.Argonaut._
import argonaut.ArgonautShapeless._
import org.http4s.argonaut._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}
import org.http4s.Uri
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._

object waterMarkServer {
  import waterMarkServiceUtils._
  import asyncUtils._

  val errorResponse: PartialFunction[Throwable, Task[Response]] = {
    case t =>
      InternalServerError(t.toString)
  }

  val wms = HttpService {
    case GET -> Root =>
      Ok("Welcome to the Watermark service!")

    case req @ POST -> Root / "ticket" =>
      (for {
        content <- req.as[String]
        doc     <- stringToDoc(content)
        ticket  <- genTicket(doc)
        _       <- storeDoc(doc, ticket)
        //Add the doc to the stream
        _       <- Task.now(docStream(doc, ticket).offer(doc))
        res     <- Ok(ticket.id.toString)
      } yield res).handleWith(errorResponse)

    // If Future[waterMark] has completed, then the user can retrieve a watermarked document
    case req @ POST -> Root / "waterMark" / ticket =>
      documentsMap.get(Ticket(ticket.toInt)).map((x) => Ok(encode(x).nospaces)).getOrElse(NotFound("Ticket not Found"))
  }

  val executorService: ExecutorService = {
    Executors.newFixedThreadPool(math.max(4, Runtime.getRuntime.availableProcessors), Strategy.DefaultDaemonThreadFactory)
  }

  def createServer(svc: HttpService): Task[Server] =
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
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
*/
