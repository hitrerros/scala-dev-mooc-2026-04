package ru.otus.module2

object higher_kinded_types extends App {

  trait TupleF[F[_]] {
    def tupleImplicit[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  }

  object TupleF {
    implicit val optionT: TupleF[Option] = new TupleF[Option] {
      def tupleImplicit[A,B](a: Option[A], b : Option[B]): Option[(A, B)] = tuple(a,b)
    }
    implicit val listT: TupleF[List] = new TupleF[List] {
      def tupleImplicit[A,B](a: List[A], b : List[B]): List[(A, B)] = tuple(a,b)
    }
  }

  def tuple[A, B](a: List[A], b: List[B]): List[(A, B)] =
    a.flatMap{ a => b.map((a, _))}

  def tuple[A, B](a: Option[A], b: Option[B]): Option[(A, B)] =
    a.flatMap{ a => b.map((a, _))}

  def tuple[E, A, B](a: Either[E, A], b: Either[E, B]): Either[E, (A, B)] =
    a.flatMap{ a => b.map((a, _))}

  trait Bindable[F[_], A] {
    def map[B](f: A => B): F[B]
    def flatMap[B](f: A => F[B]): F[B]
  }
  
  def tupleF[F[_] : TupleF , A, B](fa: F[A], fb: F[B]): F[(A, B)] =
     implicitly[TupleF[F]].tupleImplicit(fa,fb)

  def optBindable[A](opt: Option[A]): Bindable[Option, A] = new Bindable[Option, A] {
    override def map[B](f: A => B): Option[B] = opt.map(f)

    override def flatMap[B](f: A => Option[B]): Option[B] = opt.flatMap(f)
  }

  def listBindable[A](opt: List[A]): Bindable[List, A] = new Bindable[List, A] {
    override def map[B](f: A => B): List[B] = opt.map(f)

    override def flatMap[B](f: A => List[B]): List[B] = opt.flatMap(f)
  }



  val optA: Option[Int] = Some(1)
  val optB: Option[Int] = Some(2)

  val list1 = List(1, 2, 3)
  val list2 = List(4, 5, 6)
  
  val r5 = tupleF(Option(1), Option(2))
  val r6 = tupleF(List(1), List(2))


}