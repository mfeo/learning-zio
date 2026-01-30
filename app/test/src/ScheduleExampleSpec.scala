package examples

import zio._
import zio.test._

object ScheduleExampleSpec extends ZIOSpecDefault:

  def spec = suite("Schedule Tests")(
    test("repeat runs effect n+1 times") {
      for
        counter <- Ref.make(0)
        _       <- counter.update(_ + 1).repeat(Schedule.recurs(4))
        value   <- counter.get
      yield assertTrue(value == 5)
    },

    test("retry succeeds after transient failures") {
      for
        attempt <- Ref.make(0)
        result <- {
          val flaky = attempt.updateAndGet(_ + 1).flatMap { n =>
            if n < 3 then ZIO.fail("fail") else ZIO.succeed(n)
          }
          flaky.retry(Schedule.recurs(5))
        }
      yield assertTrue(result == 3)
    },

    test("retryOrElse returns fallback on exhausted retries") {
      val alwaysFails = ZIO.fail("boom")
      for
        result <- alwaysFails.retryOrElse(
                    Schedule.recurs(2),
                    (err: String, _: Long) => ZIO.succeed(s"fallback: $err")
                  )
      yield assertTrue(result == "fallback: boom")
    },

    test("Schedule.collectAll gathers schedule outputs") {
      for
        collected <- ZIO.succeed("x")
                       .repeat(Schedule.recurs(3).collectAll)
      yield assertTrue(collected.size == 4)
    },

    test("Schedule.recurs && Schedule.spaced composes") {
      for
        counter <- Ref.make(0)
        fiber <- counter.update(_ + 1)
                   .repeat(Schedule.recurs(2) && Schedule.spaced(10.millis))
                   .fork
        _ <- TestClock.adjust(30.millis)
        _ <- fiber.join
        value <- counter.get
      yield assertTrue(value == 3)
    }
  )
