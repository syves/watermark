package watermark.service

import scala.concurrent._
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._
import argonaut._
import argonaut.Argonaut._
import argonaut.ArgonautShapeless._
import org.http4s.argonaut._
import org.http4s._

object waterMarkServiceUtils {

  //To represent a database of Documents that could be queried by Ticket.
  var documentsMap = collection.immutable.Map[Ticket, Document]()
  val emptyW = new WaterMark(None, None, None, None)
  val encode = EncodeJson.of[Document]

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
    //looks like I can try using an atomic integer or syncronizing on an object containing the val?
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
