package ru.otus.module3

import ru.otus.module3.zioConcurrency.printEffectRunningTime
import ru.otus.module3.zio_homework.config.Configuration
import ru.otus.module3.zio_homework.config.Configuration.envConfig
import zio.*

import java.io.IOException
import scala.language.postfixOps

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в консоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */

  private val randomizer: UIO[Int] = Random.nextIntBetween(1, 3)

  def input(expected: Int): Task[Int] = Console.readLine("guess the digit: ")
    .map(_.toInt)
    .filterOrFail(_ == expected)(Exception(s"you've missed, expected $expected"))

  lazy val guessProgram: ZIO[Any, IOException, Unit] = for {
    n <- randomizer
    _ <- input(n).foldZIO(failure => Console.printLine(failure.getMessage), _ => Console.printLine("you're right"))
  } yield ()

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   * 
   */

  def doWhile(eff : Task[Boolean]): Task[Boolean] = eff.repeatUntil(res => res)

    /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из переменных окружения, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "Configuration.config" из пакета config
   */

    def loadConfigOrDefault: Task[Any] = ZIO
      .config(envConfig)
      .catchAll { _ => Console.printLine(Configuration.default) }


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайным образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff: UIO[Int] = ZIO.sleep(1.seconds) *> Random.nextIntBetween(0,10)

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  private lazy val effects: Seq[UIO[Int]] = List.fill(10)(eff)

  
  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекции "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  private lazy val outputWriter : Seq[Int] => Task[Unit] = r => Console.printLine(s"total ${r.sum}")
  private lazy val consecutiveCollect: Task[Unit] =  ZIO.collectAll(effects).flatMap(outputWriter)
  private lazy val parCollect: Task[Unit] =  ZIO.collectAllPar(effects).flatMap(outputWriter)

  lazy val app: Task[Unit] = consecutiveCollect

  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */
  lazy val appSpeedUp : Task[Unit] = printEffectRunningTime(parCollect)

  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * можно было использовать аналогично zio.Console.printLine например
   */

    trait RunningTimeService {
      def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A]
    }

    val layer : ULayer[RunningTimeService] = ZLayer.succeed(new RunningTimeService(){
      override def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] =
        zioConcurrency.printEffectRunningTime(zio)
    })

   /**
     * 6.
     * Воспользуйтесь написанным сервисом, чтобы создать эффект, который будет логировать время выполнения программы из пункта 4.3
     *
     * 
     */

  private lazy val  appWithTimeLog: ZIO[RunningTimeService, Throwable, Unit] = for {
          srv <- ZIO.service[RunningTimeService]
          _  <- srv.printEffectRunningTime(app)
    } yield  ()

  /**
    * 
    * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
    */

  lazy val runApp: ZIO[Any, Throwable, Unit] = appWithTimeLog.provide(layer)

}
