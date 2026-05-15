package ru.otus.module1



import ru.otus.module1.variance.{Animal, Cat}

import scala.annotation.tailrec
import scala.language.postfixOps



/**
 * referential transparency
 */


 // recursion

object recursion {

  /**
   * Реализовать метод вычисления n!
   * n! = 1 * 2 * ... n
   */

  def fact(n: Int): Int = {
    var _n = 1
    var i = 2
    while (i <= n){
      _n *= i
      i += 1
    }
    _n
  }


  def factRec(n: Int): Int =
    if(n <= 0) 1 else n * factRec(n - 1)


  def factTailRec(n: Int): Int = {
    def loop(n: Int, accum: Int): Int =
      if(n <= 0) accum
      else loop(n - 1, n * accum)
    loop(n, 1)
  }



  /**
   * Реализовать вычисление N числа Фибоначчи
   * F0 = 0, F1 = 1, Fn = Fn-1 + Fn - 2
   */


}



object hof{

  def dumb(string: String): Unit = {
    Thread.sleep(1000)
    println(string)
  }

  // обертки

  def logRunningTime[A, B](f: A => B): A => B = a =>
    val start = System.currentTimeMillis()
    val result = f(a)
    val end = System.currentTimeMillis()
    println(end - start)
    result



  // изменение поведения ф-ции

  def isOdd(i: Int): Boolean = i % 2 > 0
  lazy val isEven: Int => Boolean = not(isOdd)
  def not[A](f: A => Boolean): A => Boolean = a => !f(a)



  // изменение самой функции

  def sum(x: Int, y: Int): Int = x + y

  def curried[A, B, C](f: (A, B) => C): A => B => C = a => b => f(a, b)

  curried(sum) // Int => Int => Int

  def partial2[A, B, C](a: A, f: (A, B) => C): B => C = curried(f)(a)

  val r: Int => Int = partial2(2, sum)
  r(3) // 5

}


object variance {


  // Invariance Вне зависимости от отношений между типами A и B, Box[A] и Box[B] два разных типа
  // + Covariance Если А является подтипом В, то Box[A] является подтипом Box[B]
  // - Contravariance Если А является подтипом В, то Box[A] является супер типом Box[B]

  class Box[+T](val item: T)

  class Feeder[-T] {
    def feed(v: T): Unit = println("Feeding")
  }

  sealed trait Animal

  case class Cat() extends Animal

  case class Dog() extends Animal

  val animalFeeder: Feeder[Animal] = Feeder[Animal]()
  val catFeeder: Feeder[Cat] = catFeeder
  catFeeder.feed(Cat())

  def feed(a: Animal): Unit = ???

  feed(Cat())
  feed(Dog())

  // trait Function1[-R, +T] = R => T

  val f1 : Animal => Dog = ???
  val f2: Dog => Animal = f1




}






/**
 *  Реализуем тип Option
 */



 object opt {


  /**
   *
   * Реализовать структуру данных Option, который будет указывать на присутствие либо отсутствие результата
   */


  sealed trait Option[+T] {
    def isEmpty: Boolean = if(this.isInstanceOf[None.type]) true else false

    def map[B](f: T => B): Option[B] = flatMap(v => Option(f(v)))

    def flatMap[B](f: T => Option[B]): Option[B] =
      if (isEmpty) None
      else f(this.asInstanceOf[Some[T]].v)

    /**
     *
     * Реализовать метод zip, который будет создавать Option от пары значений из 2-х Option
     */
    def zip[B](p: Option[B]): Option[(T, B)]

    /**
     *
     * Реализовать метод filter, который будет возвращать не пустой Option
     * в случае если исходный не пуст и предикат от значения = true
     */

    def filter(f: T => Boolean): Option[T]
  }

  object Option {
    def apply[T](v: T): Option[T] = Some(v)
  }

  case class Some[T](v: T) extends Option[T] :
    def zip[B](p: Option[B]): Option[(T, B)] = p match
      case Some(r) => Some((v, r))
      case None => None

    def filter(f: T => Boolean): Option[T] =
      if (f(v)) Some(this.v) else None

  case object None extends Option[Nothing]:
    def zip[B](p: Option[B]): Option[(Nothing, B)] = None
    def filter(f: Nothing => Boolean): Option[Nothing] = None

  var animalOpt: Option[Animal] = None
  var intOpt: Option[Int] = ???




  /**
   *
   * Реализовать метод printIfAny, который будет печатать значение, если оно есть
   */
    def printIfAny[T](value: Option[T]): Unit = value match
      case Some(v) => println(v)
      case _ =>





 }

 object list {
   /**
    *
    * Реализовать одно связанный иммутабельный список List
    * Список имеет два случая:
    * Nil - пустой список
    * Cons - непустой, содержит первый элемент (голову) и хвост (оставшийся список)
    */


    sealed trait List[+T]{
     def ::[TT >: T](elem: TT): List[TT] = new::(elem, this)

     def ++[TT >: T](elem: List[TT]): List[TT] =
       this match
         case Nil => elem
         case head :: tail => new::(head, tail ++ elem)

     def flatmap[TT >: T](f: TT => List[TT]): List[TT] = this match
       case Nil => Nil
       case head :: tail => f(head) ++ tail.flatmap(f)

     /**
      *
      * Реализовать метод map для списка который будет применять некую ф-цию к элементам данного списка
      */

     def map[TT >: T](f: TT => TT): List[TT] = this match
       case Nil => Nil
       case head :: tail => f(head) :: tail.map(f)


     /**
      *
      * Реализовать метод filter для списка который будет фильтровать список по некому условию
      */
     def filter[TT >: T](f: TT => Boolean): List[TT] = this match
       case Nil => Nil
       case head :: tail => if (f(head)) head :: tail.filter(f) else tail.filter(f)

     def mkString(delimiter: String): String = this match
       case Nil => ""
       case head :: Nil => head.toString
       case head :: tail => head.toString + delimiter + tail.mkString(delimiter)
     }


    case class ::[A](head: A, tail: List[A]) extends List[A]
    case object Nil extends List[Nothing]

    object List {
      def apply[A](v: A*): List[A] =
        if(v.isEmpty) Nil else ::(v.head, apply(v.tail:_*))
    }






    /**
      * Конструктор, позволяющий создать список из N - го числа аргументов
      * Для этого можно воспользоваться *
      * 
      * Например, вот этот метод принимает некую последовательность аргументов с типом Int и выводит их на печать
      * def printArgs(args: Int*) = args.foreach(println(_))
      */

    /**
      *
      * Реализовать метод reverse который позволит заменить порядок элементов в списке на противоположный
      */
      def reverse[T](a : List[T]): List[T] = {
        @tailrec
        def helper(oldL : List[T], accL :  List[T] = Nil) : List[T] = {
          oldL match
            case Nil => accL
            case head :: tail => helper(tail,::(head,accL))
        }
        helper(a)
      }

    /**
      *
      * Написать функцию incList которая будет принимать список Int и возвращать список,
      * где каждый элемент будет увеличен на 1
      */
      val incList : (List[Int] => List[Int]) = a => a.map(k=>k+1)

    /**
      *
      * Написать функцию shoutString которая будет принимать список String и возвращать список,
      * где к каждому элементу будет добавлен префикс в виде '!'
      */
    val shoutString: (List[String] => List[String]) = a => a.map("!"+_)

 }