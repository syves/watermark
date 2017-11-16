object WordCountService extends App {

  //def sortByOccur
  val input = collection.immutable.Map[String,Int]("mat" -> 1, "on" -> 1, "cat" -> 1, "sat" -> 1, "the" -> 2)
  def sort(dict: collection.immutable.Map[String, Int]): List[String] =
    dict.toSeq.sortWith{
      case (left,right) =>
              if (left._2 == right._2)  left._1 < right._1
              else left._2 > right._2
    }.map(_._1).toList

}
