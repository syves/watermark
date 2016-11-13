package watermark.service

import scala.concurrent._
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._
//import io.circe._
//import io.circe.parser._
//import io.circe.syntax._
//import io.circe.generic.auto._


object waterMarkServiceUtils {

  //To represent a database of Documents that could be queried by Ticket.
  var documentsMap = collection.immutable.Map[Ticket, Document]()

  val emptyW = new WaterMark(None, None, None, None)

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

  case class Ticket(id: Int)

  //Document codec
  //implicit val decodeDocument: Decoder[Document] =
   //Decoder.forProduct3("id", "first_name", "last_name")(User.apply)

  //implicit val encodeDocument: Encoder[Document] =
   //Encoder.forProduct4("content", "title", "author", "topic")(doc =>
     //(doc.watermark.content, doc.watermark.title, doc.watermark.author, doc.watermark.topic)
   //)

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

  def genTicket(doc: Document): Task[Ticket] = Task.delay {
    //I'm using the document hashCode to simulate uniqiue id tat would be created by a db.
    //And to avoid race conditions on a global variable.
    Ticket(doc.hashCode)
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
}
