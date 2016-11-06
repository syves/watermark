import org.scalatest.AsyncFlatSpec
import scala.collection.mutable.Map
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}

//TO run this file, from root of directory
//sbt
//run
//test

class waterMarkSpec extends AsyncFlatSpec {

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
  var watermarkedDocs = collection.immutable.Map[Int, Document]()
  var documentsMap = collection.immutable.Map[Int, Document]()

  val input = List(
    "book\tCosmos\tCarl Sagan\tScience",
    "journal\tThe Journal of cell biology\tRockefeller University Press",
    "book\tA brief history of time\tStephen W Hawking\tScience")

  val documents = List[Document](
    Document("book", "Cosmos", "Carl Sagan", Some("Science"), emptyW),
    Document(
      "journal",
      "The Journal of cell biology",
      "Rockefeller University Press",
      None,
      emptyW),
    Document("book", "A brief history of time", "Stephen W Hawking", Some("Science"), emptyW))

  //Tests below

  //Converts initial input into unwatermarked documents
  def stringToDoc(s: String): Document = {
    val props = s.split("\t")
    val content: String = props(0)
    val title: String = props(1)
    val author: String = props(2)
    val topic: Option[String] = if (props.length == 3){ None } else { Some(props(3)) }
    val watermark: WaterMark = emptyW
    new Document(content, title, author, topic, watermark)
  }

  "stringToDoc" should "immediately creates a book Document with an empty watermark" in {
    val actual = stringToDoc(input(0))
    val expected = Document("book", "Cosmos", "Carl Sagan", Some("Science"), emptyW)
    assert(actual == expected)
  }

  "stringToDoc" should "immediately creates a journal Document with an empty watermark" in {
    val actual = stringToDoc(input(1))
    val expected = Document(
      "journal",
      "The Journal of cell biology",
      "Rockefeller University Press",
      None, emptyW)
    assert(actual == expected)
  }

  //Creates a new document with a watermark
  def docWithWaterMark(l: Document): Document = l match {
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

  "docWithWaterMark" should "immediately creates a document with a Watermark for a book" in {
    val actual = docWithWaterMark(documents(0))
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

  "docWithWaterMark" should "immediately creates a document with a WaterMark for a journal" in {
    val actual = docWithWaterMark(documents(1))
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

  "docWithWaterMark" should "immediately creates a document with a Watermark for a book2" in {
    val actual = docWithWaterMark(documents(2))
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

  def genTicketAndDoc(s: String): Int = {
    val ticket = 6666
    val doc = stringToDoc(s)
    documentsMap = documentsMap + (ticket -> doc)
    ticket
    }

  "genTicketAndDoc" should "immediately returns a ticket" in {
    var documentsMap = collection.immutable.Map[Int, Document]()
    val actual = genTicketAndDoc(input(2))
    val expected = 6666
    assert(actual == expected)
  }

  def isProcessed(ticket: Int): Boolean = {
    watermarkedDocs.contains(ticket)
  }

  "isProcessed" should "immediately returns a Boolean" in {
    var documentsMap = collection.immutable.Map[Int, Document]()
    genTicketAndDoc(input(2))
    val actual = isProcessed(6666)
    val expected = false
    assert(actual == expected)
  }

  def futureOfMap(d: Document, ticket: Int): Future[collection.immutable.Map[Int, Document]] = {
    val f: Future[Document] = Future { docWithWaterMark(d) }
    f.map { doc => watermarkedDocs = watermarkedDocs + (ticket -> doc)
      watermarkedDocs
    }
  }

  behavior of "futureOfWaterMark"

  it should "eventually return Future of Map of ticket, Document" in {
    var watermarkedDocs = collection.immutable.Map[Int, Document]()
    val expected = collection.immutable.Map[Int, Document](8888 -> Document(
        "book",
        "A brief history of time",
        "Stephen W Hawking",
        Some("Science"),
        WaterMark(
          Some("book"),
          Some("A brief history of time"),
          Some("Stephen W Hawking"),
          Some("Science"))))
    val futureWaterMark = futureOfMap(documents(2), 8888)
    futureWaterMark map { waterMap => assert(waterMap == expected)}
  }
}
