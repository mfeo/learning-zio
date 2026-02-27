package examples

import zio._
import zio.test._

object ConcurrencyExampleSpec extends ZIOSpecDefault:
  def spec = suite("Concurrency Tests")(
    test("timeout interrupts slow effects") {
      for
        fiber  <- ZIO.sleep(2.seconds).as("done").timeout(1.second).fork
        _      <- TestClock.adjust(2.seconds)
        result <- fiber.join
      yield assertTrue(result == None)
    },
    test("race returns the first successful effect") {
      val t1 = ZIO.sleep(2.seconds) *> ZIO.succeed("1")
      val t2 = ZIO.sleep(1.second) *> ZIO.succeed("2")
      for
        fiber  <- t1.race(t2).fork
        _      <- TestClock.adjust(2.seconds)
        winner <- fiber.join
      yield assertTrue(winner == "2")
    },
    test("foreachPar runs in parallel") {
      for
        ref <- Ref.make(0)
        fiber <- ZIO.foreachPar(1 to 5)(_ => ZIO.sleep(1.second) *> ref.update(_ + 1)).fork
        _ <- TestClock.adjust(1.second)
        _ <- fiber.join
        count <- ref.get
      yield assertTrue(count == 5) // All 5 completed in 1 second
    }
  )
