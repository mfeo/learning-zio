package examples

import zio._
import java.util.concurrent.TimeUnit

object AspectExample extends ZIOAppDefault:

  // --- Built-in Aspects ---
  // Aspects modify the behavior of an effect without changing its core logic.
  // We apply them using the `@@` operator.
  val retryAspect =
    for
      _ <- Console.printLine("--- Aspect: Retry ---")
      ref <- Ref.make(0)
      // This effect fails twice, then succeeds
      flakyTask = ref.updateAndGet(_ + 1).flatMap { count =>
                    if count < 3 then 
                      Console.printLine(s"  Task failed on attempt $count") *> ZIO.fail("Error")
                    else 
                      Console.printLine(s"  Task succeeded on attempt $count") *> ZIO.succeed("Success")
                  }
      // Apply the retry aspect directly to the effect
      result <- flakyTask @@ ZIOAspect.retry(Schedule.recurs(3))
      _ <- Console.printLine(s"  Final result: $result")
    yield ()

  // --- Custom Aspect ---
  // You can define your own aspects to reuse common logic (e.g., logging execution time)
  val logExecutionAspect = new ZIOAspect[Nothing, Any, Nothing, Any, Nothing, Any]:
    def apply[R, E, A](effect: ZIO[R, E, A])(implicit trace: Trace): ZIO[R, E, A] =
      for
        start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
        result <- effect
        end    <- Clock.currentTime(TimeUnit.MILLISECONDS)
        _      <- Console.printLine(s"  [Aspect] Execution took ${end - start} ms").orDie
      yield result

  val customAspectExample =
    for
      _ <- Console.printLine("--- Aspect: Custom ---")
      _ <- (ZIO.sleep(150.millis) *> Console.printLine("  Doing some work...")) @@ logExecutionAspect
    yield ()

  def run =
    (retryAspect *> customAspectExample).exitCode
