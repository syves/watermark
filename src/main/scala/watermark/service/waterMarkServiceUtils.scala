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
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._

object waterMarkServiceUtils {

  implicit val system = ActorSystem("water-mark")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val buffersize = 100
  //If the buffer is full when a new element is available this strategy backpressures
  //the upstream publisher until space becomes available in the buffer.
  val overlfowStrategy = akka.stream.OverflowStrategy.backpressure

  //Materialize a SourceQueue onto which elements can be pushed for emitting from the source.
  def docStream(doc: Document, ticket: Ticket): SourceQueueWithComplete[Document] =
    Source.queue(buffersize, overlfowStrategy)
      .to(Sink foreach((doc: Document) => futureOfDoc(doc, ticket)))
      .run()

  case class WaterMark(
    content: Option[String],
    title: Option[String],
    author: Option[String],
    topic: Option[String])

  case class Document(
    content: String,
    title: String,
    author: String,
    topic: Option[String],
    watermark: WaterMark)

  val emptyW = new WaterMark(None, None, None, None)

  case class Ticket(id: Int)

  //To represent a database of Documents that could be queried by Ticket.
  var documentsMap = collection.immutable.Map[Ticket, Document]()

  def stringToDoc(s: java.lang.String): Task[Document] = Task.delay {
    s.split("\t") match {
      case Array(content, title, author, topic) => new Document(
        content,
        title,
        author,
        Some(topic),
        emptyW)
      case Array(content, title, author) => new Document(
        content,
        title,
        author,
        None,
        emptyW)
      case x => new Document("","","", None, emptyW)
    }
  }

  //When using a real db, the db would create unique Id's for each document.
  var nextTicketNum = 500

  def genTicket(doc: Document): Task[Ticket] = Task.delay {
    val ticketNum = nextTicketNum
    nextTicketNum += 1
    Ticket(ticketNum)
    }

  def storeDoc(doc: Document, ticket: Ticket): Task[Unit] = Task.delay {
    documentsMap = documentsMap + (ticket -> doc)
  }

  def waterMark(l: Document): Document = l match {
    case Document(content, title, author, topic, WaterMark(_, _, _, _)) =>
      new Document(
        content,
        title,
        author,
        topic,
      new WaterMark(
        Some(content),
        Some(title),
        Some(author),
        topic))
  }

  //Create waterMarked document asynchronously.
  def futureOfDoc(d: Document, ticket: Ticket): Future[Document] = {
    val f: Future[Document] = Future { waterMark(d) }
    //When the future completes add the watermarked document to a new database.
    f.map { doc => documentsMap = documentsMap + (ticket -> doc)
      doc
    }
  }
}
