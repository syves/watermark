package watermark.service

import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.actor._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.stream.scaladsl.{Sink, Source}
import akka.util._


object asyncUtils {

  import waterMarkServiceUtils._
  implicit val system = ActorSystem("water-mark")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val buffersize = 100
  //If the buffer is full when a new element is available this strategy backpressures
  //the upstream publisher until space becomes available in the buffer.
  val overlfowStrategy = akka.stream.OverflowStrategy.backpressure

  //Create waterMarked document asynchronously.
  //refactored -moved create new map into doc stream.
  def futureOfDoc(d: Document): Future[Document] = {
    Future { waterMark(d) }
  }

  //Materialize a SourceQueue onto which elements can be pushed for emitting from the source.
  def docStream(doc: Document, ticket: Ticket): SourceQueueWithComplete[Document] =
    Source.queue(buffersize, overlfowStrategy)
      //When the future completes add the watermarked document to a new database.
      .to(Sink foreach((doc: Document) => futureOfDoc(doc).map { doc => documentsMap = documentsMap + (ticket -> doc)
        //println(ticket, doc.title)
        doc
      }))
      .run()

}
