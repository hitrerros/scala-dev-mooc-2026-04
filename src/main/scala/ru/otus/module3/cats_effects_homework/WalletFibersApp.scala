package ru.otus.module3.cats_effects_homework

import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

import scala.concurrent.duration.*

object WalletFibersApp  extends  IOApp.Simple {

  private def topupForever (acc : Ref[IO,Account], n : Int) : IO[Unit] =
    for
      _ <- IO.sleep(n.millis)
      eff <- acc.update(v=>Account(v.name,v.balance+100)) *> IO.defer(topupForever(acc, n))
    yield ()

  private def showBalance(acc : Seq[Ref[IO,Account]]) : IO[Unit] =
    (acc.traverse_(_.get.flatMap(res => IO.println(res))) *> IO.sleep(100.millis)).foreverM

  private def startFibers : IO[Unit] =
      for {
        ref <-  initialAccounts.traverse(v => Ref.of[IO,Account](v))
        _ <- ref.traverse_(v => topupForever(v,100).start)
        info <- showBalance(ref).start
        _ <- IO.blocking(scala.io.StdIn.readLine())
        _ <- info.cancel
        _ <- IO.println("Exit program")

      } yield ()

  def run: IO[Unit] = startFibers
}

