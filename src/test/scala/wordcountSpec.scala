import org.scalatest.AsyncFlatSpec
import scala.collection.mutable.Map
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._

class wordcountSpec extends AsyncFlatSpec {
  import WordcountServiceUtils._

  "storeWord" should "immediately stores a word and increments if word is already present" in {
      var wordsMap = collection.mutable.Map[String, Int]("cat" -> 1)
      val input = "cat"
      storeWord()(input, wordsMap)
      val expected = collection.mutable.Map[String, Int]("cat" -> 2)
      assert(wordsMap == expected)
  }
  "strToWords" should "immediately converts a string of char into an unsorted list of words" in {
    val input = "The cat sat on the mat."
    val actual = strToWords(input)
    val expected = List("the", "on", "sat", "cat", "the")
    assert(actual == expected)
  }
  "sortByOccur" should "immediately sorts a dictionary into a List of words ordered by occurance decending and if a contest occur in lex order" in {
    val input = collection.immutable.Map[String,Int]("mat" -> 1, "on" -> 1, "cat" -> 1, "sat" -> 1, "the" -> 2)
    val actual = sortByOccur(input)
    val expected = List("the", "cat", "mat", "on", "sat")
    assert(actual == expected)
  }

  /*
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
  */
}
