package ru.otus.module2

import cats.Functor

import scala.util.{Failure, Try}


package object catsHomework {

  /**
   * Простое бинарное дерево
   * @tparam A
   */
  sealed trait Tree[+A]
  final case class Branch[A](left: Tree[A], right: Tree[A])
    extends Tree[A]
  final case class Leaf[A](value: A) extends Tree[A]

  /**
   * Напишите instance Functor для объявленного выше бинарного дерева.
   * Проверьте, что код работает корректно для Branch и Leaf
   */
 // 1. вариант с type lambda
//  given Functor[[A] =>> Tree[A]] with
//      override def map[A,B](fa: Tree[A])(f: A => B): Tree[B] =
//        fa match
//          case Branch(left, right) => Branch(map(left)(f),map(right)(f))
//          case Leaf(v) => Leaf(f(v))
  // вариант scala 2.13
  implicit val treeFunctor : Functor[Tree] = new Functor[Tree]:
    def map[A, B](fa: Tree[A])(f: A => B): Tree[B] =
      fa match
        case Branch(left, right) => Branch(map(left)(f),map(right)(f))
        case Leaf(v) => Leaf(f(v))

  /**
   * Monad абстракция для последовательной
   * комбинации вычислений в контексте F
   *
   * @tparam F
   */
  trait Monad[F[_]]{
    def flatMap[A,B](fa: F[A])(f: A => F[B]): F[B]
    def pure[A](v: A): F[A]
  }


  /**
   * MonadError расширяет возможность Monad
   * кроме последовательного применения функций, позволяет обрабатывать ошибки
   * @tparam F
   * @tparam E
   */
  trait MonadError[F[_], E] extends Monad[F]{
    // Поднимаем ошибку в контекст `F`:
    def raiseError[A](e: E): F[A]

    // Обработка ошибки, потенциальное восстановление:
    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]

    // Обработка ошибок, восстановление от них:
    def handleError[A](fa: F[A])(f: E => A): F[A]

    // Test an instance of `F`,
    // failing if the predicate is not satisfied:
    def ensure[A](fa: F[A])(e: E)(f: A => Boolean): F[A]
  }

  /**
   * Напишите instance MonadError для Try
   */
   implicit val tryMonad : MonadError[Try,Throwable] = new MonadError[Try,Throwable] {
     def pure[A](v: A): Try[A] = Try(v)
     def flatMap[A, B](fa: Try[A])(f: A => Try[B]): Try[B] = fa.flatMap(v => f(v))
     def raiseError[A](e: Throwable): Try[A] = Failure(e)
     def handleErrorWith[A](fa: Try[A])(f: Throwable => Try[A]): Try[A] = fa.recoverWith(v => f(v))
     def handleError[A](fa: Try[A])(f: Throwable => A): Try[A] = fa.recover(v=>f(v))
     def ensure[A](fa: Try[A])(e: Throwable)(f: A => Boolean): Try[A] = fa.filter(f(_)).orElse(Failure(e))
   }


  /**
   * Напишите instance MonadError для Either,
   * где в качестве типа ошибки будет String
   */
   private type EitherString[A] = Either[String,A]

   implicit val eitherMonad: MonadError[EitherString, String] = new MonadError[EitherString, String] {
     def pure[A](v: A): EitherString[A] = Right(v)
     def flatMap[A, B](fa: EitherString[A])(f: A => EitherString[B]): EitherString[B] = fa.flatMap(v => f(v))
     def raiseError[A](e: String): EitherString[A] = Left(e)
     def handleErrorWith[A](fa: EitherString[A])(f: String => EitherString[A]): EitherString[A] =
       fa match {
         case Right(_) => fa
         case Left(value) => f(value)
       }
     def handleError[A](fa: EitherString[A])(f: String => A): EitherString[A] = handleErrorWith(fa)(v => Right(f(v)))
     def ensure[A](fa: EitherString[A])(e: String)(f: A => Boolean): EitherString[A] = fa.filterOrElse(f,e)
  }

  }
