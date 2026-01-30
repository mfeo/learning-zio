package examples

import zio._

object PromiseExample extends ZIOAppDefault:

  // Basic: one fiber completes, another awaits
  val basicPromise =
    for
      _ <- Console.printLine("--- Promise: Basic ---")
      p <- Promise.make[Nothing, String]
      _ <- (ZIO.sleep(100.millis) *> p.succeed("hello from fiber A")).fork
      v <- p.await
      _ <- Console.printLine(s"  Received: $v")
    yield ()

  // Failing a promise
  val failedPromise =
    for
      _ <- Console.printLine("--- Promise: Failure ---")
      p <- Promise.make[String, Int]
      _ <- p.fail("something went wrong")
      exit <- p.await.exit
      _ <- Console.printLine(s"  Awaited failed promise: $exit")
    yield ()

  // Coordination: gate pattern — multiple fibers wait for a signal
  val gatePattern =
    for
      _ <- Console.printLine("--- Promise: Gate Pattern ---")
      gate <- Promise.make[Nothing, Unit]
      log  <- Ref.make(List.empty[String])
      // launch 3 workers that wait for the gate to open
      fibers <- ZIO.foreach(1 to 3) { id =>
                  (gate.await *> log.update(_ :+ s"worker-$id started")).fork
                }
      _ <- Console.printLine("  Workers waiting...")
      _ <- ZIO.sleep(50.millis)
      _ <- gate.succeed(()) // open the gate
      _ <- ZIO.foreach(fibers)(_.join)
      entries <- log.get
      _ <- Console.printLine(s"  After gate opened: ${entries.sorted}")
    yield ()

  // Handoff: pass a computed result between fibers
  val handoff =
    for
      _ <- Console.printLine("--- Promise: Handoff ---")
      p <- Promise.make[Nothing, Int]
      // producer computes and hands off
      _ <- (ZIO.succeed(21 * 2).flatMap(p.succeed)).fork
      // consumer awaits the result
      result <- p.await
      _ <- Console.printLine(s"  Handoff result: $result")
    yield ()

  // isDone: check if promise is completed without blocking
  val isDoneExample =
    for
      _ <- Console.printLine("--- Promise: isDone ---")
      p <- Promise.make[Nothing, String]
      before <- p.isDone
      _ <- Console.printLine(s"  Before complete: isDone=$before")
      _ <- p.succeed("done")
      after <- p.isDone
      _ <- Console.printLine(s"  After complete:  isDone=$after")
    yield ()

  def run =
    (basicPromise *> failedPromise *> gatePattern *> handoff *> isDoneExample).exitCode
