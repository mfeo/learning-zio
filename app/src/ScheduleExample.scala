package examples

import zio._

object ScheduleExample extends ZIOAppDefault:

  // repeat: run a successful effect multiple times
  val repeatExample: ZIO[Any, Nothing, Unit] =
    for
      _       <- Console.printLine("--- Schedule: Repeat ---").orDie
      counter <- Ref.make(0)
      _ <- counter.updateAndGet(_ + 1)
             .tap(n => Console.printLine(s"  tick $n").orDie)
             .repeat(Schedule.recurs(4))
      total <- counter.get
      _     <- Console.printLine(s"  Total ticks: $total").orDie
    yield ()

  // retry: retry a failing effect
  val retryExample: ZIO[Any, Nothing, Unit] =
    for
      _       <- Console.printLine("--- Schedule: Retry ---").orDie
      attempt <- Ref.make(0)
      result <- {
        val flakyEffect =
          attempt.updateAndGet(_ + 1).flatMap { n =>
            if n < 4 then
              Console.printLine(s"  Attempt $n failed").orDie *>
                ZIO.fail(new RuntimeException(s"Error on attempt $n"))
            else
              Console.printLine(s"  Attempt $n succeeded!").orDie *>
                ZIO.succeed(s"OK after $n attempts")
          }
        flakyEffect.retry(Schedule.recurs(5)).orDie
      }
      _ <- Console.printLine(s"  Result: $result").orDie
    yield ()

  // compose schedules with && and ||
  val composedSchedule: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Schedule: Composition ---").orDie
      // && means both must continue; || means either can continue
      counter <- Ref.make(0)
      _ <- counter.updateAndGet(_ + 1)
             .tap(n => Console.printLine(s"  composed tick $n").orDie)
             .repeat(Schedule.recurs(3) && Schedule.spaced(100.millis))
      _ <- Console.printLine("  Done (recurs(3) && spaced(100ms))").orDie
    yield ()

  // collecting outputs from a schedule
  val collectExample: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Schedule: Collect Outputs ---").orDie
      collected <- ZIO.succeed("ping")
                     .repeat(Schedule.recurs(4).collectAll)
      _ <- Console.printLine(s"  Collected: ${collected.toList}").orDie
    yield ()

  def run =
    repeatExample *>
    retryExample *>
    composedSchedule *>
    collectExample
