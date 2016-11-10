package watermark.service

import scala.concurrent._
import scala.util.control.Breaks._
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._
import akka.stream._
import akka.stream.scaladsl._
//do I need all of these?
import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

object waterMarkServiceUtils {

  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  implicit val system = ActorSystem("water-mark")
  implicit val materializer = ActorMaterializer()

  //how do we add the http requests to a stream?
  /*
  -A stream usually begins at a source, so this is also how we start
  an Akka Stream
  -The Source type is parameterized with two types: the first one is the
   type of element that this source emits and the second one may signal
    that running the source produces some auxiliary value
*/
  val documents: Source[String, Ticket]
  //consumer function:
  //val reqStream = documents.runForeach(req =>
    stringToDoc(req) //task[Document]
    genTicket(doc) //also saves to dic with empty watermark? in memory?
    waterMark(doc) //and adds to another collection, could overwrite entry?


  )(materializer)

/*
 IOResult is a type that IO operations return in Akka Streams in order
 to tell you how many bytes or elements were consumed and whether the
 stream terminated normally or exceptionally.
 */
  val result: Future[IOResult] =
    reqStream
      .map(...)
      .runWith(save to dictionary?)


  //Constructors
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

  var watermarkedDocs = collection.immutable.Map[Ticket, Document]()
  var documentsMap = collection.immutable.Map[Ticket, Document]()

  //Creates a Document with an empty watermark.
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

  var nextTicketNum = 500

  //Stores Document in an immutable Map.
  //The document can be retieved with the key, which is a Ticket of type Int.
  def genTicket(doc: Document): Task[Ticket] = Task.delay {
    val ticketNum = nextTicketNum
    nextTicketNum += 1
    documentsMap = documentsMap + (Ticket(ticketNum) -> doc)
    Ticket(ticketNum)
    }

  //Get a new Doc with a populated watermark.
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

  //Create waterMarks asynchronously.
  def futureOfDoc(d: Document, ticket: Ticket): Future[Document] = {
    val f: Future[Document] = Future { waterMark(d) }
    f.map { doc => watermarkedDocs = watermarkedDocs + (ticket -> doc)
      doc
    }
  }

}
