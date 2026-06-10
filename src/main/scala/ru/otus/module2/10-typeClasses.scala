package ru.otus.module2

import ru.otus.module2.JsValue._
import ru.otus.module2.type_classes.Eq.given_Eq_String

enum JsValue:
  case JsObject(get: Map[String, JsValue])
  case JsString(get: String)
  case JsNumber(get: Double)
  case JsNull
// 1

object type_classes extends App {

   trait JsonWriter[T]:
       def toJson(v: T): JsValue

   object JsonWriter:
    def apply[T](using ev: JsonWriter[T]) = ev
    private val from = [T] => (f: T => JsValue) => new JsonWriter[T]:
     override def toJson(v: T): JsValue = f(v)

    given JsonWriter[String] = from[String](JsString)
    given JsonWriter[Int] = from[Int](JsNumber)

    given optJson [T](using jw: JsonWriter[T]): JsonWriter[Option[T]] = from[Option[T]] :
      case Some(value) => jw.toJson(value)
      case None => JsNull

   def toJson[T: JsonWriter](v: T): JsValue =
    summon[JsonWriter[T]].toJson(v)
  
   println(toJson("vffv"))
   println(toJson(10))

  //  "fvhfujhubvf".toJson
  //  10.toJson
   println( toJson( Option(10)))
  //  Option("vdfvf").toJson

  


  // 1 type constructor
  trait Ordering[T]:
    def less(a: T, b: T): Boolean

  object Ordering {

    def from[A](f: (A, A) => Boolean): Ordering[A] = new Ordering[A] {
      override def less(a: A, b: A): Boolean = f(a, b)
    }

    given Ordering[Int] = from[Int](_ < _)

    given Ordering[String] = from[String](_.length < _.length)

    given Ordering[User] = from[User](_.age < _.age)
  }

  case class User(name: String, age: Int)


  def greatest[A](a: A, b: A)(using ord: Ordering[A]): A =
    if(ord.less(a, b)) b else a


  greatest(5, 10)
  greatest("ab", "abcd")
  greatest(User("Bob", 16), User("Alice", 18))

    
  trait Eq[T]{
    extension (a: T) def ===(b: T): Boolean
  }
  
  object Eq {
    given Eq[String] = new Eq[String] {
      extension (a: String) override def ===(b: String): Boolean = a == b
    }
  } 
    
//  extension [T](a: T)(using eq: Eq[T]){
//    def ===(b: T): Boolean = eq.===(a, b)
//  }

  val result = List("a", "b", "c").filter(str => str === "1")







}
















