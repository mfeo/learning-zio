package examples

import zio._

object ConcurrencyExample extends ZIOAppDefault:

  // --- Timeout ---
  // Limits the execution time of an effect. If it takes too long, it fails or returns None.
  val timeoutExample =
    for
      _ <- Console.printLine("--- Concurrency: Timeout ---")
      slowTask = ZIO.sleep(2.seconds) *> Console.printLine("Finished slow task")
      // timeout returns Option[A]. None means it timed out.
      result <- slowTask.timeout(1.second)
      _ <- Console.printLine(s"  Result: $result (None means timeout)")
    yield ()

  // --- Race ---
  // Runs multiple effects concurrently and returns the result of the first one to succeed.
  val raceExample =
    for
      _ <- Console.printLine("--- Concurrency: Race ---")
      task1 = ZIO.sleep(2.seconds) *> ZIO.succeed("Task 1 won!")
      task2 = ZIO.sleep(1.second)  *> ZIO.succeed("Task 2 won!")
      winner <- task1.race(task2)
      _ <- Console.printLine(s"  Winner: $winner")
    yield ()

  // --- foreachPar / collectAllPar ---
  // Process a collection of items concurrently.
  val parallelExample =
    for
      _ <- Console.printLine("--- Concurrency: foreachPar ---")
      items = List("apple", "banana", "cherry")
      // Use withParallelism to limit how many tasks run at the exact same time
      results <- ZIO.foreachPar(items) { item =>
                   ZIO.sleep(500.millis) *> 
                   Console.printLine(s"  Processed $item") *>
                   ZIO.succeed(item.toUpperCase)
                 }.withParallelism(2)
      _ <- Console.printLine(s"  Results: $results")
    yield ()

  def run =
    (timeoutExample *> raceExample *> parallelExample).exitCode
