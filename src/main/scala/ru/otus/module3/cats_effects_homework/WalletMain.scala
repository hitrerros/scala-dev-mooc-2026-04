package ru.otus.module3.cats_effects_homework

import cats.effect.kernel.{Ref, Resource}
import cats.effect.{IO, IOApp}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.io.{BufferedSource, Source}


trait Wallet[F[_]] :
   def topupBalance(path : Path,topFunction : Int => Int) : F[Unit]
   def readBalance(path : Path) : F[Unit]

given Wallet[IO] with
   private def readBalanceFile(path : Path): Resource[IO, BufferedSource] =
      Resource.make(IO.blocking {
        Source.fromFile(path.toFile)
      })(res => IO.blocking(res.close()))

   private def parseCSVFile(buff : BufferedSource) : List[Account] =
     buff.getLines().map(v => { val parts = v.split(",")
     Account(parts(0),parts(1).toInt)}).toList

   override def readBalance(path : Path): IO[Unit] =
    for {
      _ <- readBalanceFile(path).use{
              reader =>
                for {
                  lines <- IO.blocking(parseCSVFile(reader))
                  _ <- IO.println(lines.map(_._2).mkString(" "))
                } yield ()
      }

    } yield()

   override def topupBalance(path : Path,topFunction : Int => Int) : IO[Unit] =     for {
     wallets <- Ref.of[IO,List[Account]](List.empty)
     _ <- readBalanceFile(path).use{
       reader =>
         for {
           lines <- IO.blocking(parseCSVFile(reader))
           _ <- wallets.update(_ => lines.map(r => Account(r._1,topFunction(r._2))))
         } yield ()
     }

     res <- wallets.get
     _ <-  IO.blocking(Files.writeString(path,res.map(v => s"${v.name},${v.balance}").mkString("\n"),StandardCharsets.UTF_8))
   } yield()



object WalletMain extends  IOApp.Simple :
    private val wallet = summon[Wallet[IO]]
    private val path  = Path.of("balance1.csv")
  
    override val run: IO[Unit] =  for {
      _ <- wallet.readBalance(path)
      _ <- wallet.topupBalance(path,_ + 50)
    } yield ()

