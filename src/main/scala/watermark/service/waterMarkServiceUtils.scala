package watermark.service

import scala.concurrent._
import scala.util.control.Breaks._
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._
import akka.stream._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

//do I need all of these?
import akka._
import akka.util._
import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.stream.actor._
import akka.stream.ActorMaterializer


object waterMarkServiceUtils {

  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  implicit val system = ActorSystem("water-mark")
  implicit val materializer = ActorMaterializer()
  //for akka futures
  //import system.dispatcher

  //how do we add the http requests to a stream?
  /*
  -A stream usually begins at a source, so this is also how we start
  an Akka Stream
  -The Source type is parameterized with two types: the first one is the
   type of element that this source emits and the second one may signal
    that running the source produces some auxiliary value
    -Materialize a SourceQueue onto which elements can be pushed for emitting
    from the source. The queue contains a buffer, if elements are pushed onto
     the queue faster than the source is consumed the overflow will be handled
      with a strategy specified by the user. Functionality for tracking when an
       element has been emitted is available through SourceQueue.offer.
*/
  val buffersize = 100
  //If the buffer is full when a new element is available this strategy backpressures
  //the upstream publisher until space becomes available in the buffer.
  val overlfowStrategy = akka.stream.OverflowStrategy.backpressure

  val docStream = Source.queque(buffersize, overlfowStrategy)
    .to(Sink foreach(doc => futureOfDoc(doc, ticket)))
    .run()
  //how does work?
  //docStream.offer(doc)

  //FLOW or a source?
//  val docStream: Source[document, NotUsed]
  //consumer function:
  //? fromInputStream? statefulMapConcat mapAsyncUnordered
  //SINK
  //val docSink = docStream.runForeachParallel(doc => futureOfDoc(doc))
  //run with immplicit ActorMaterializer
  //docStream.runWith(docSink)

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

  //To represent a database of Documents that could be queried by TicketId.
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

  //When using a real db, the db would create unique Id's for each document.
  var nextTicketNum = 500

  //The document can be retieved with the key, which is a Ticket of type Int.
  def genTicket(doc: Document): Task[Ticket] = Task.delay {
    val ticketNum = nextTicketNum
    nextTicketNum += 1
    Ticket(ticketNum)
    }

  //Stores Document in an immutable Map.
  def storeDoc(doc: Document, ticket: Ticket): Task[Unit] = Task.delay {
    documentsMap = documentsMap + (ticket -> doc)
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

  //Create waterMarked document asynchronously.
  def futureOfDoc(d: Document, ticket: Ticket): Future[Document] = {
    val f: Future[Document] = Future { waterMark(d) }
    //When the future completes add the watermarked document to a new database,
    //instead of overwritting the document.
    f.map { doc => documentsMap = documentsMap + (ticket -> doc)
      //we don't actually need to return the document do we?
      doc
    }
  }

}
