package examples

import zio._
import zio.test._
import zio.test.Assertion._

// --- Test Environment Magic ---
// ZIO provides built-in Test environments (TestClock, TestConsole, TestRandom, TestSystem)
// so you don't need external mocking libraries for common system interactions.
object TestEnvExampleSpec extends ZIOSpecDefault:

  val businessLogic =
    for
      _    <- Console.printLine("Please enter your name:")
      name <- Console.readLine
      _    <- ZIO.sleep(5.hours) // Simulating a very long process
      _    <- Console.printLine(s"Hello $name, processing finished!")
      num  <- Random.nextIntBetween(1, 10)
    yield num

  def spec = suite("Test Environment Tests")(
    test("TestConsole, TestClock, and TestRandom all working together") {
      for
        // 1. Setup TestRandom to always return '7' when nextInt is called
        _ <- TestRandom.feedInts(7)
        
        // 2. Setup TestConsole with simulated user input
        _ <- TestConsole.feedLines("Alice")
        
        // 3. Start the business logic in the background (fork)
        fiber <- businessLogic.fork
        
        // 4. Time travel! Advance the clock by 5 hours instantly
        _ <- TestClock.adjust(5.hours)
        
        // 5. Get the result
        result <- fiber.join
        
        // 6. Verify the console output
        output <- TestConsole.output
        
      yield assertTrue(
        result == 7,
        output(0) == "Please enter your name:\n",
        output(1) == "Hello Alice, processing finished!\n"
      )
    }
  )

