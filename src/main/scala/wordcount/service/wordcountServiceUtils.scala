import scala.concurrent._
import scalaz.concurrent.{Task, Strategy}
import scalaz._, Scalaz._
import argonaut._
//import argonaut.Argonaut._
//import argonaut.ArgonautShapeless._
//import org.http4s.argonaut._
//import org.http4s._

object WordcountServiceUtils {

  //var wordsMap = collection.immutable.Map[String, Int]()
  var wordsMap = collection.mutable.Map[String, Int]()
  //val encode = EncodeJson.of[?]

  def storeWord()(word: String, dict: collection.mutable.Map[String, Int]): Unit = {
    this.synchronized {
      if (dict.contains(word)) dict(word) = dict(word) + 1
      else dict += (word -> 1)
    }
    println(dict)
  }

  def sortByOccur(dict: collection.immutable.Map[String, Int]): List[String] =
    dict.toSeq.sortWith{
      case (left,right) =>
              if (left._2 == right._2)  left._1 < right._1
              else left._2 > right._2
    }.map(_._1).toList
    //List(the, mat, on, cat, sat)

  def strToWords(str: String): List[String] = {
    var res = List[String]();  var w = ""
      str.foreach{ s => s match {
        case '.'  => ()
        case ' ' => res = w :: res; w = ""
        case char => w = w + char.toLower
      }
    }
    res
  }
}
