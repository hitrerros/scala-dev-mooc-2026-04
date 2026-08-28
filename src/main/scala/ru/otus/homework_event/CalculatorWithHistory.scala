package ru.otus.homework_event


import ru.otus.homework_event.CalcOperator.{ADD, DIVIDE}

import java.time.Instant
import java.util.UUID

enum CalculatorStatus { case ON, OFF }

enum CalcOperator {
  case ADD, DEDUCT, MULTIPLY, DIVIDE
}

sealed trait CalculationEvent {
  def eventType(): String
}

final case class DeviceTurnedOn(calculatorId: String) extends CalculationEvent {
  override def eventType(): String = "device turned on"
}

final case class CalculatedSuccess[+T <: Number](calculatorId: String, leftOp: T, rightOp: T, operator:  CalcOperator)
  extends CalculationEvent {
  override def eventType(): String = "calculated successfully"
}

final case class CalculatedError[+T <: Number](calculatorId: String, leftOp: T, rightOp: T, operator: CalcOperator)
  extends CalculationEvent {
  override def eventType(): String = "calculated with error"
}

final case class DeviceTurnedOff(calculatorId: String) extends CalculationEvent {
  override def eventType(): String = "device turned off"
}


final case class EventEnvelope[E <: CalculationEvent](
                                                       eventId: UUID,
                                                       aggregateId: UUID,
                                                       eventType: String,
                                                       version: Int,
                                                       timeStamp: Instant,
                                                       correlationId: Option[UUID],
                                                       payload: E
                                                     )

final class InMemoryEventLog:
  private var events: Vector[EventEnvelope[? <: CalculationEvent]] = Vector.empty

  def append[E <: CalculationEvent](event: EventEnvelope[E]): Unit = events = events :+ event

  def all: Vector[EventEnvelope[? <: CalculationEvent]] = events

  def byAggregateId(aggregateId: UUID): Vector[EventEnvelope[? <: CalculationEvent]] =
    events.filter(_.aggregateId == aggregateId)

// ---------------------------------------------------
final case class CalculatorState( calculatorId: String, status: CalculatorStatus )

object Calculator {

   def evolve(state: Option[CalculatorState],event: CalculationEvent): Option[CalculatorState] =
      event match {
        case DeviceTurnedOn(calculatorId) =>   Some(CalculatorState(calculatorId, CalculatorStatus.ON))
        case DeviceTurnedOff(calculatorId)  =>  Some(CalculatorState(calculatorId, CalculatorStatus.OFF))
        case _  =>
          state match {
            case Some(current) if (current.status == CalculatorStatus.ON) => Some(current)
            case _ => state
          }
      }

   def replay(
              events: Seq[EventEnvelope[? <: CalculationEvent]]
            ): Option[CalculatorState] =
    events.foldLeft(Option.empty[CalculatorState]) {
      case (state, envelope) =>
        evolve(state, envelope.payload)
    }
 }


object CalculatorWithHistory extends App {
   val (calculatorId,sessionId) = ("EASY_CALC",UUID.randomUUID())
   val eventLog = new InMemoryEventLog

   val calcFlow: Seq[CalculationEvent] = DeviceTurnedOn(calculatorId) ::
                  CalculatedSuccess(calculatorId = calculatorId,leftOp = 1, rightOp = 2, operator = ADD) ::
                  CalculatedError(calculatorId = calculatorId, leftOp = 1, rightOp = 0, operator = DIVIDE) ::
                  DeviceTurnedOff(calculatorId) :: Nil


   calcFlow.foreach(v => eventLog.append(EventEnvelope(eventId = UUID.randomUUID(),
                         aggregateId = sessionId, eventType = v.eventType(), version = 1,
                         timeStamp = Instant.now(), correlationId = None, payload = v)) )


   val currentFlow = eventLog.byAggregateId(sessionId)

    val state = Calculator.replay(currentFlow)
    println(state)

}
