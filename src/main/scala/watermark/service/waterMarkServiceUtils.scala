package watermark.service

import scala.concurrent._
import scala.util.control.Breaks._
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._

//TO Run this file
//sbt waterMarkService.scala, then script: ....

object waterMarkServiceUtils {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

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

  //All documents unprocessed or processed have a ticket ID, the ticket id from an
  //unprocessed document matches it's watermarked version.
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

  //Use isProcessed to check if a single Document from the original queque has been processed.
  def isProcessed(ticket: Ticket): Boolean = {
    watermarkedDocs.contains(ticket)
  }

  //Get a new Doc with a populated watermark.
  def docWithWaterMark(l: Document): Document = l match {
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
    val f: Future[Document] = Future { docWithWaterMark(d) }
    f.map { doc => watermarkedDocs = watermarkedDocs + (ticket -> doc)
      doc
    }
  }

  //Watermark each doc in the original Map.
  def markDocs(m: collection.immutable.Map[Ticket, Document]): Task[Unit] = Task.delay {
    m.foreach { case (ticket, doc) =>
      println()
      futureOfDoc(doc, ticket)
    }
  }

  val input = List(
      "book\tCosmos\tCarl Sagan\tScience",
      "journal\tThe Journal of cell biology\tRockefeller University Press",
      "book\tA brief history of time\tStephen W Hawking\tScience")

}

    /*
    1.we get all the tickets currently in original map- queque
    for each check if the Ticket exists in map of watermareked docs.

    2. do we want to return the watermarked docs each time?
    or a message if not done?

 	  3. Stream continues to be processes-  we have to keep checking for new tickets
 	  Q4..what happens when all the tickets are processed?
    */
