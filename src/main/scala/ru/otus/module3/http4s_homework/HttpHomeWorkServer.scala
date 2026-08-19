package ru.otus.module3.http4s_homework

import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp, Resource}
import cats.implicits.toSemigroupKOps
import com.comcast.ip4s.{Host, Port}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*
import org.http4s.{HttpRoutes, Response}


object HttpHomeWorkServer extends IOApp.Simple {

  def routes(counter : Ref[IO,Int]): HttpRoutes[IO] =  {
    val counterRoute : HttpRoutes[IO]  = HttpRoutes.of {
      case GET -> Root / "counter" => CounterService[IO].returnCounter(counter).flatMap(v => Ok(v.noSpaces))
    }
    val slowdownRoute : HttpRoutes[IO]  = HttpRoutes.of {
      case GET -> Root / "slow" / chunk / total / time =>
           Ok(SlowdownService[IO].getData(chunk.toInt,total.toLong,time.toInt))
    }
    counterRoute <+> validateParams(slowdownRoute)
  }

  private def validateParams(routes : HttpRoutes[IO]) : HttpRoutes[IO] = Kleisli {
     req =>
       req.pathInfo.segments.drop(1).map(_.toString.forall(_.isDigit)).find(!_) match {
          case Some(_) => OptionT.liftF(BadRequest("wrong characters in request params"))
          case None =>   routes(req)
        }
  }


  val server: Resource[IO, Server] =  for {
    initCounter <- Resource.eval(Ref.of[IO,Int](0))
    httpApp = Router("/" -> routes(initCounter)).orNotFound

    server <- EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8081).get)
      .withHttpApp(httpApp).build
  } yield server

  def run: IO[Unit] = server.use(_ => IO.never)
}
