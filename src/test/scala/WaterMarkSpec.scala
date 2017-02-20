import org.scalatest.AsyncFlatSpec
import scala.collection.mutable.Map
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._

//sbt ~test

class waterMarkSpec extends AsyncFlatSpec {

  val emptyW = new WaterMark(None, None, None, None)
  var documentsMap = collection.immutable.Map[Ticket, Document]()
  var nextTicketNum = 500

  val input = List(
    "book\tCosmos\tCarl Sagan\tScience",
    "journal\tThe Journal of cell biology\tRockefeller University Press",
    "book\tA brief history of time\tStephen W Hawking\tScience")

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

  val documents = List[Document](
    Document("book", "Cosmos", "Carl Sagan", Some("Science"), emptyW),
    Document(
      "journal",
      "The Journal of cell biology",
      "Rockefeller University Press",
      None,
      emptyW),
    Document("book", "A brief history of time", "Stephen W Hawking", Some("Science"), emptyW))

  //Tests below-------------------------------------------------------------------------------

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

  "stringToDoc" should "immediately creates a book Document with an empty watermark" in {
    val actual = stringToDoc(input(0)).run
    val expected = Document("book", "Cosmos", "Carl Sagan", Some("Science"), emptyW)
    assert(actual == expected)
  }

  "stringToDoc" should "immediately creates a journal Document with an empty watermark" in {
    val actual = stringToDoc(input(1)).run
    val expected = Document(
      "journal",
      "The Journal of cell biology",
      "Rockefeller University Press",
      None, emptyW)
    assert(actual == expected)
  }

  "stringToDoc" should "immediately return an empty doc if imput is bad" in {
    val actual = stringToDoc("angry cat").run
    val expected = Document("", "", "", None, emptyW)
    assert(actual == expected)
  }

  def waterMark(l: Document): Document = l match {
    case Document(content, title, author, topic, WaterMark(_, _, _, _)) => new Document(
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

  "waterMark" should "immediately creates a document with a Watermark for a book" in {
    val actual = waterMark(documents(0))
    val expected: Document = new Document(
      "book",
      "Cosmos",
      "Carl Sagan",
      Some("Science"),
      new WaterMark(
        Some("book"),
        Some("Cosmos"),
        Some("Carl Sagan"),
        Some("Science")))
    assert(actual == expected)
  }

  "waterMark" should "immediately creates a document with a WaterMark for a journal" in {
    val actual = waterMark(documents(1))
    val expected: Document = new Document(
      "journal",
      "The Journal of cell biology",
      "Rockefeller University Press",
      None,
      new WaterMark(
        Some("journal"),
        Some("The Journal of cell biology"),
        Some("Rockefeller University Press"),
        None))
    assert(actual == expected)
  }

  "waterMark" should "immediately creates a document with a Watermark for a book2" in {
    val actual = waterMark(documents(2))
    val expected: Document = new Document(
      "book",
      "A brief history of time",
      "Stephen W Hawking",
      Some("Science"),
      new WaterMark(
        Some("book"),
        Some("A brief history of time"),
        Some("Stephen W Hawking"),
        Some("Science")))
        assert(actual == expected)
      }

  def futureOfDoc(d: Document, ticket: Ticket): Future[Document] = {
    val f: Future[Document] = Future { waterMark(d) }
    f.map { doc => documentsMap = documentsMap + (ticket -> doc)
      doc
    }
  }

  behavior of "futureOfDoc"

  it should "eventually return Future of Document" in {
    val expected = Document(
        "book",
        "A brief history of time",
        "Stephen W Hawking",
        Some("Science"),
        WaterMark(
          Some("book"),
          Some("A brief history of time"),
          Some("Stephen W Hawking"),
          Some("Science")))
    val futureWaterMark = futureOfDoc(documents(2), Ticket(502))
    futureWaterMark map { futureDoc => assert(futureDoc == expected)}
  }
}
