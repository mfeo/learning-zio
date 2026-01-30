package examples

import zio._

object SemaphoreExample extends ZIOAppDefault:

  // Basic: limit concurrency to N
  val basicSemaphore =
    for
      _ <- Console.printLine("--- Semaphore: Basic (permits = 2) ---")
      sem <- Semaphore.make(permits = 2)
      _ <- ZIO.foreachPar(1 to 5) { id =>
             sem.withPermit {
               for
                 _ <- Console.printLine(s"  Task $id started")
                 _ <- ZIO.sleep(200.millis)
                 _ <- Console.printLine(s"  Task $id finished")
               yield ()
             }
           }
    yield ()

  // Mutex: semaphore with 1 permit = exclusive access
  val mutexExample =
    for
      _ <- Console.printLine("--- Semaphore: Mutex (permits = 1) ---")
      mutex <- Semaphore.make(permits = 1)
      log   <- Ref.make(List.empty[String])
      _ <- ZIO.foreachPar(1 to 4) { id =>
             mutex.withPermit {
               log.update(_ :+ s"task-$id-enter") *>
               ZIO.sleep(50.millis) *>
               log.update(_ :+ s"task-$id-exit")
             }
           }
      entries <- log.get
      _ <- Console.printLine(s"  Execution order: $entries")
      // verify no overlapping: every enter is followed by its own exit
      pairs = entries.grouped(2).toList
      _ <- Console.printLine(s"  Pairs (should be enter/exit): $pairs")
    yield ()

  // Rate limiter pattern: withPermits for bulk operations
  val bulkPermits =
    for
      _ <- Console.printLine("--- Semaphore: Bulk Permits ---")
      sem <- Semaphore.make(permits = 5)
      // this task needs 3 permits at once
      _ <- ZIO.foreachPar(List("heavy", "light-1", "light-2")) {
             case "heavy" =>
               sem.withPermits(3) {
                 Console.printLine("  Heavy task (3 permits) started") *>
                 ZIO.sleep(100.millis) *>
                 Console.printLine("  Heavy task (3 permits) finished")
               }
             case name =>
               sem.withPermit {
                 Console.printLine(s"  $name (1 permit) started") *>
                 ZIO.sleep(50.millis) *>
                 Console.printLine(s"  $name (1 permit) finished")
               }
           }
    yield ()

  // Available permits inspection
  val availablePermits =
    for
      _ <- Console.printLine("--- Semaphore: Available Permits ---")
      sem <- Semaphore.make(permits = 3)
      a0 <- sem.available
      _ <- Console.printLine(s"  Initial: $a0 permits")
      fiber <- sem.withPermits(2)(ZIO.sleep(100.millis)).fork
      _ <- ZIO.sleep(10.millis) // let fiber acquire
      a1 <- sem.available
      _ <- Console.printLine(s"  During withPermits(2): $a1 permits")
      _ <- fiber.join
      a2 <- sem.available
      _ <- Console.printLine(s"  After release: $a2 permits")
    yield ()

  def run =
    (basicSemaphore *> mutexExample *> bulkPermits *> availablePermits).exitCode
