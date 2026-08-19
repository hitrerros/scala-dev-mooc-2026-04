package ru.otus.module3.http4s_homework

import cats.effect.Temporal
import fs2.{Chunk, Stream}

import scala.concurrent.duration.*

final class SlowdownService [F[_] : Temporal] {

  val stream: Stream[F, Byte] = Stream.emits("A".getBytes).repeat

  def getData(chunk : Int,total  : Long, time  : Int) : Stream[F, Byte] =
     stream
      .take(total)
      .chunkN(chunk)
      .metered(time.seconds)
      .flatMap(Stream.chunk)
     
      
}
