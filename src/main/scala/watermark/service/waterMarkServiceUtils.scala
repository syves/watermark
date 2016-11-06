package watermark.service

import scala.concurrent._
import scala.util.control.Breaks._

//TO Run this file
//scalac waterMark.scala && scala waterMark.scala

/*
 A global publishing company that publishes books and journals wants to develop
 a service to watermark their documents.

 -A document (books, journals) has a title, author and a watermark property. +
 -An empty watermark property indicates that the document has not been watermarked yet. +
 -Book publications include topics in business, science and media. +
 -Journals don’t include any specific topics. +
 -The watermark service has to be asynchronous. +
 -For a given content document the service should return a ticket
  which can be used to poll the status of processing. +
 -If the watermarking is finished the document can be retrieved with the ticket. +
 -The watermark of a book or a journal is identified by setting the watermark property of the object. +
 -For a book the watermark includes the properties content, title, author and topic. +
 -The journal watermark includes the content, title and author. +

 Create an appropriate object-oriented model for the problem. +
 Implement the Watermark-Service, meeting the above conditions. +
 Provide Unit-Tests to ensure the functionality of the service. +
*/

object WaterMarkService {
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
  def stringToDoc(s: String): Document = {
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
    }
  }

  var nextTicketNum = 500

  //Stores Document in an immutable Map.
  //The document can be retieved with the key, which is a Ticket of type Int.
  def genTicketAndDoc(s: String): Ticket = {
    val ticketNum = nextTicketNum
    nextTicketNum += 1
    val doc = stringToDoc(s)
    documentsMap = documentsMap + (Ticket(ticketNum) -> doc)
    Ticket(ticketNum)
    }

  //Add the incomming docs to the immutable Map.
  def createDocsAndGetTickets(l: List[String]): List[Ticket] = {
    l.map (genTicketAndDoc(_))
  }

  //Use isProcessed to check if a single Document from the original queque has been processed.
  def isProcessed(ticket: Ticket): Boolean = {
    watermarkedDocs.contains(Ticket)
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
  def futureOfMap(d: Document, ticket: Ticket): Future[collection.immutable.Map[Ticket, Document]] = {
    val f: Future[Document] = Future { docWithWaterMark(d) }
    f.map { doc => watermarkedDocs = watermarkedDocs + (ticket -> doc)
      watermarkedDocs
    }
  }

  //condition for polling,
  def allProcessed(l: List[Ticket]): Boolean = {
    l.forall(isProcessed)
  }

  //Watermark each doc in the original Map.
  def markDocs(m: collection.immutable.Map[Ticket, Document]): Unit = {
    m.foreach { case (ticket, doc) =>
      futureOfMap(doc, ticket)
    }
  }

  //see unit tests by running waterMarkSpec.scala
  def main(args: Array[String]) {
    //Mimic an input from STDIN.
    //FIXME! imput is only captured once, not an inifinite stream.
    //We must at least check for incoming data!

    val input = List(
      "book\tCosmos\tCarl Sagan\tScience",
      "journal\tThe Journal of cell biology\tRockefeller University Press",
      "book\tA brief history of time\tStephen W Hawking\tScience")

    val listOfTickets: List[Int] = createDocsAndGetTickets(input)

    markDocs(documentsMap)

    val poll: Future[?string message or ]

    /*
    1.we get all the tickets currently in original map- queque
    2. for each check if the Ticket exists in map of watermareked docs.

    Q1. do we want to return all the watermarked docs each time?
    or a message with the ones yet to be watermarked?
    Q2. How do we make this non blocking?
    	while true poll. with a future of results inside?

    -For a given content document the service should return a ticket
  		which can be used to poll the status of processing. +
 	-If the watermarking is finished the document can be retrieved with the ticket. +
 	is this a clue?
 	....when the ticket is a Future[watermarked] then return, return a future for each Ticket,
 	Q3...so then are we polling for new tickets as well?
 	we have to keep checking for new tickets
 	Q4..what happens when all the tickets are processed?
    */

    /*
    ### I was unable to get polling to work without blocking the futures.
    I look forward to learning more about working with Futures, threads, and AKKA.

    val poll = new Thread {
      override def run() {
        breakable {
          while (true) {
            Thread.sleep(1000)
            if (allProcessed(listOfTickets)) break
            println("Some tickets have not been processed.")
          }
        }
        println("All the tickets have been processed")
        println(s"List of tickets for new Documents: ${listOfTickets}")
        println("")
        println(s"WaterMarked documents : ${watermarkedDocs.toString}")
      }
    }
    poll.start()
    */
    println(s"List of tickets for new Documents: ${listOfTickets}")
    println("")
    Thread.sleep(3000)
    println(s"WaterMarked documents : ${watermarkedDocs.toString}")
  }
}
