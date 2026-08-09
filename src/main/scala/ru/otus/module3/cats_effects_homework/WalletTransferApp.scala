package ru.otus.module3.cats_effects_homework

import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp}

trait WalletRef[F[_]]:
  def topupBalance(storage: Ref[IO, Seq[Account]], topFunction: Int => Int): F[Unit]
  def readBalance(storage: Ref[IO, Seq[Account]]): F[Unit]

given WalletRef[IO] with
  def topupBalance(storage: Ref[IO, Seq[Account]], topFunction: Int => Int): IO[Unit]  =
      storage.update(_.map(rec => Account(rec.name,topFunction(rec.balance))))
  
  def readBalance(storage: Ref[IO, Seq[Account]]): IO[Unit]
  =  storage.get.flatMap(rec => IO.println(rec))


object WalletTransferApp  extends  IOApp.Simple :

  private val walletRef = summon[WalletRef[IO]]
  
  def run: IO[Unit] = for {
     init <- Ref.of[IO,Seq[Account]](initialAccounts)
     _ <- walletRef.topupBalance(init,_+100)
     _ <- walletRef.readBalance(init)
    
  } yield()

