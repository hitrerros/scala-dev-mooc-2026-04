package ru.otus.homework_event

import ru.otus.homework_event.CalcOperator.*
import ru.otus.homework_event.CalcStatus.*
import zio.{
  IO,
  Ref,
  Scope,
  UIO,
  ULayer,
  URLayer,
  ZIO,
  ZIOAppArgs,
  ZIOAppDefault,
  ZLayer
}

import java.time.Instant
import java.util.UUID

sealed trait CalcEvent
final case class CalcPerformed(calcId: String, bufferedValue: BigDecimal) extends CalcEvent
final case class CalcResetted(calcId: String) extends CalcEvent
final case class CalcTurnedOn(calcId: String) extends CalcEvent
final case class CalcTurnedOff(calcId: String) extends CalcEvent


sealed trait CalcCommand {
   def calcId: String
}
final case class CalcOn(calcId: String) extends CalcCommand
final case class CalcOff(calcId: String) extends CalcCommand
final case class CalcReset(calcId: String) extends CalcCommand
final case class CalcPerform(calcId: String, operand: BigDecimal, operator: CalcOperator) extends CalcCommand

sealed trait CalcError
final case class DivideByZero(calcId: String, description: String) extends CalcError
final case class DeviceAlreadyOff(calcId: String, description: String) extends CalcError
final case class DeviceAlreadyOn(calcId: String, description: String) extends CalcError
final case class UnknownError(description : String) extends CalcError



final case class CalcEventEnvelope[E <: CalcEvent](
                                                       eventId: UUID,
                                                       aggregateId: String,
                                                       eventType: String,
                                                       version: Int,
                                                       timeStamp: Instant,
                                                       correlationId: Option[UUID],
                                                       producer : String,
                                                       payload: E
                                                     )


type EnvelopeT = CalcEventEnvelope[? <: CalcEvent]


trait CalcEventLog {
   def append(event : EnvelopeT) : UIO[Unit]
   def byAggregateId(aggregateId : String) : UIO[Vector[EnvelopeT]]
}

private final case class CalcInMemoryEventLog(ref : Ref[Vector[EnvelopeT]]) extends CalcEventLog {
  override def append(event: EnvelopeT): UIO[Unit]
          = ref.update(_ :+ event)

  override def byAggregateId(aggregateId : String): UIO[Vector[EnvelopeT]]
           = ref.get.map(_.filter(_.aggregateId==aggregateId))
}


object CalcInMemoryEventLog {
   val layer : ULayer[CalcEventLog] =
     ZLayer.fromZIO(
       Ref
          .make(Vector.empty[EnvelopeT])
          .map(v => new CalcInMemoryEventLog(v))
     )
}

enum CalcStatus {
   case READY_TO_CALC,  SHUTDOWN
}

final case class CalculatorState( calculatorId : String, bufferedValue : BigDecimal, status : CalcStatus)


object CalculatorActor {

  def evolve(state: Option[CalculatorState], event: CalcEvent) : Option[CalculatorState] = event match {
    case res @ c : CalcPerformed => state.map(v => CalculatorState(v.calculatorId,res.bufferedValue,READY_TO_CALC))
    case c : CalcResetted =>  state.map(v => CalculatorState(v.calculatorId,0,READY_TO_CALC))
    case c : CalcTurnedOn => state.map(v => CalculatorState(v.calculatorId,0,READY_TO_CALC))
    case c : CalcTurnedOff => state.map(v => CalculatorState(v.calculatorId,0,CalcStatus.SHUTDOWN))
  }

  def replay(events : Seq[EnvelopeT]) : Option[CalculatorState] = {
    events.foldLeft(Option.empty[CalculatorState]) {
      case (state,envelope) => evolve(state,envelope.payload)
    }
  }

  def decide(
              state: Option[CalculatorState],
              command: CalcCommand
            ): Either[CalcError, List[CalcEvent]] = {
    state match {
      case Some(st) =>
        command match {
          case cmd: CalcOn => validateAndExecuteOn(st,cmd)
          case cmd: CalcOff => validateAndExecuteOff(st,cmd)
          case cmd: CalcReset => validateAndExecuteReset(st,cmd)
          case v @ cmd: CalcPerform => validateAndExecutePerform(st,v)
        }
      case None => Left(UnknownError("unknown error"))
    }
  }


  private def validateAndExecuteOn(
                                    state: CalculatorState,
                                    command: CalcCommand): Either[CalcError, List[CalcEvent]] = {
    if (state.status != SHUTDOWN) Left(DeviceAlreadyOn(state.calculatorId, "device is not shutdown"))
    else Right(List(CalcTurnedOn(state.calculatorId)))
  }

  private def validateAndExecuteOff(state: CalculatorState, command: CalcCommand) :  Either[CalcError, List[CalcEvent]] = {
    if (state.status == SHUTDOWN) Left(DeviceAlreadyOff(state.calculatorId, "device is already shutdown"))
    else Right(List(CalcTurnedOff(state.calculatorId)))
  }

  private def validateAndExecuteReset(state: CalculatorState, command: CalcCommand) :  Either[CalcError, List[CalcEvent]] = {
    if (state.status == SHUTDOWN) Left(DeviceAlreadyOff(state.calculatorId, "device is already shutdown"))
    else Right(List(CalcResetted(state.calculatorId)))
  }


  private def validateAndExecutePerform(state: CalculatorState, command: CalcPerform) :  Either[CalcError, List[CalcEvent]] = {
    if (state.status != READY_TO_CALC) Left(DeviceAlreadyOff(state.calculatorId, "device is not ready to work and maybe shutdown"))
    else if  (command.operator==DIVIDE && command.operand==0) Left(DivideByZero(state.calculatorId, "divide by zero"))
    else {
      val newValue =
      command.operator match {
        case ADD => state.bufferedValue + command.operand
        case MULTIPLY => state.bufferedValue * command.operand
        case DIVIDE => state.bufferedValue / command.operand
        case DEDUCT => state.bufferedValue - command.operand
      }
      Right(List(CalcPerformed(state.calculatorId,newValue)))
    }
  }
 }


final class CalculatorCommandHandler(eventLog: CalcEventLog) {
  def handle(
              command: CalcCommand,
              correlationId: UUID
            ): IO[CalcError, List[CalcEvent]] = {
    for {
      history <- eventLog.byAggregateId(command.calcId)
      state = CalculatorActor.replay(history)
      newEvents <- ZIO.fromEither(CalculatorActor.decide(state, command))

      _ <- ZIO.foreachDiscard(newEvents) {
        event =>
          eventLog.append(CalcEventEnvelope(
            eventId = UUID.randomUUID(), aggregateId = "EASY_CALC", eventType = event.getClass.getSimpleName, version = 1,
            timeStamp = Instant.now(), correlationId = Some(correlationId), producer = "calculator-serice", payload = event
          ))
      }
    } yield newEvents
  }
}

object CalculatorCommandHandler {
   val layer : URLayer[CalcEventLog,CalculatorCommandHandler] =  ZLayer.fromFunction(v => new CalculatorCommandHandler(v))

}

object CalcCQRS extends ZIOAppDefault {

   val program = for {
       eventLog <- ZIO.service[CalcEventLog]
       handler <- ZIO.service[CalculatorCommandHandler]
       correlationId = UUID.randomUUID()



   } yield ()

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = program.provide(CalcInMemoryEventLog.layer,CalculatorCommandHandler.layer)
}
