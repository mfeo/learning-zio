import zio._

object MainApp extends ZIOAppDefault:

  val myAppLogic: ZIO[Any, Throwable, Unit] =
    for
      _    <- Console.printLine("Hello! What is your name?")
      name <- Console.readLine
      _    <- Console.printLine(s"Hello, $name, welcome to ZIO!")
      _    <- Console.printLine("Let me show you some ZIO features...")
      _    <- demonstrateFibers
      _    <- demonstrateErrorHandling
    yield ()

  // Demonstrate ZIO fibers (lightweight concurrency)
  def demonstrateFibers: ZIO[Any, Throwable, Unit] =
    for
      _      <- Console.printLine("\n--- Fibers (Concurrency) ---")
      fiber1 <- ZIO.succeed("Task A completed").delay(1.second).fork
      fiber2 <- ZIO.succeed("Task B completed").delay(500.millis).fork
      resultA <- fiber1.join
      resultB <- fiber2.join
      _ <- Console.printLine(s"  $resultA")
      _ <- Console.printLine(s"  $resultB")
    yield ()

  // Demonstrate ZIO error handling
  def demonstrateErrorHandling: ZIO[Any, Throwable, Unit] =
    val failingEffect: ZIO[Any, String, Int] =
      ZIO.fail("Something went wrong!")

    val recovered: ZIO[Any, Nothing, String] =
      failingEffect.fold(
        error   => s"  Recovered from error: $error",
        success => s"  Got value: $success"
      )

    for
      _   <- Console.printLine("\n--- Error Handling ---")
      msg <- recovered
      _   <- Console.printLine(msg)
    yield ()

  def run = myAppLogic
