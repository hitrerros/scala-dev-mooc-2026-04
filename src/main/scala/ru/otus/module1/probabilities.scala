package ru.otus.module1

val rnd = new scala.util.Random()
val BLACK = 0
val WHITE = 1
val numberOfElements = 10000

case class BucketList(ob : List[Int]) {
  def extractTwo : Boolean  = rnd.shuffle(ob).take(2).contains(WHITE)
}

object BucketList {
   def apply() : BucketList = BucketList(List(WHITE, WHITE, WHITE, BLACK, BLACK, BLACK))
}

object probabilities extends App {
  val buckets =  (1 to numberOfElements).map(_=>BucketList())
  val results = buckets.map(_.extractTwo)
  val whites: Seq[Boolean]  = results.filter(_==true)

  println( s"probability =  ${(whites.size.toDouble / numberOfElements)}" )
}
