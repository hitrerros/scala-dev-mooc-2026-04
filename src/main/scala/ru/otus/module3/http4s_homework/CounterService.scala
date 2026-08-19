package ru.otus.module3.http4s_homework

import cats.effect.*
import cats.effect.kernel.*
import cats.syntax.all.*
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

case class Counter(counter: Int)
object Counter {
   implicit val encodeCounter : Encoder[Counter] = deriveEncoder[Counter]
}

final class CounterService[F[_] : Concurrent] {
   def returnCounter(counter : Ref[F,Int]) : F[Json] =
     for {
       v <- counter.modify { curr =>
         val next = curr + 1
         (next, Counter(next).asJson)
       }
     } yield v
}

