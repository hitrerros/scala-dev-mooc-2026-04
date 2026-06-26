package ru.otus.module2

import ru.otus.module2.type_classes.Eq.given_Eq_String
import ru.otus.module2.type_classes.JsValue.{JsNull, JsNumber, JsString}
import ru.otus.module2.type_classes.JsonWriter.{given}


object type_classes extends App {

  sealed trait JsValue

  object JsValue {
    final case class JsObject(get: Map[String, JsValue]) extends JsValue

    final case class JsString(get: String) extends JsValue

    final case class JsNumber(get: Double) extends JsValue

    case object JsNull extends JsValue
  }
  
  // 1
  
  trait JsonWriter[-T] {
    def toJson(v: T): JsValue
  }
  
  object JsonWriter {
    
    def apply[T](implicit ev: JsonWriter[T]): JsonWriter[T] = ev
    
    def from[T](f: T => JsValue): JsonWriter[T] = (v: T) => f(v)

    implicit val jsonStringWriter: JsonWriter[String] = from(v => JsString(v))
    implicit val jsonIntWriter: JsonWriter[Int] =  from(v => JsNumber(v))
    implicit def jsonOptWriter[T](implicit jw : JsonWriter[T]): JsonWriter[Option[T]] = {
      case Some(value) => toJson(value)
      case None => JsNull
    }
  }

  def toJson[T: JsonWriter](v: T): JsValue = implicitly(JsonWriter[T]).toJson(v)
  
  toJson("vffv")
  toJson(10)
  toJson(Option(10))
  toJson(Some(10))


  //  "fvhfujhubvf".toJson
//  10.toJson
//  Option(10).toJson
//  Option("vdfvf").toJson
  
  


  // 1 type constructor
  trait Ordering[T]:
    def less(a: T, b: T): Boolean

  object Ordering {

    def from[A](f: (A, A) => Boolean): Ordering[A] = new Ordering[A] {
      override def less(a: A, b: A): Boolean = f(a, b)
    }

     implicit val orderingInt : Ordering[Int] = from(_ < _)
     implicit val stringInt: Ordering[String] = from(_.length < _.length)
     implicit val userInt: Ordering[User] = from(_.age < _.age)
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
















