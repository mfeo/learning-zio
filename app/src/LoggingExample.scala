package examples

import zio._

object LoggingExample extends ZIOAppDefault:

  // --- Basic Logging ---
  val basicLogging =
    for
      _ <- Console.printLine("--- Logging: Basics ---")
      _ <- ZIO.logInfo("This is an info message")
      _ <- ZIO.logWarning("This is a warning message")
      _ <- ZIO.logError("This is an error message")
    yield ()

  // --- Log Annotations ---
  // Annotations add key-value pairs to the log output, useful for tracking requests
  // across multiple function calls (like MDC in Java).
  val annotatedLogging =
    for
      _ <- Console.printLine("--- Logging: Annotations ---")
      _ <- ZIO.logInfo("Processing user request") @@ ZIOAspect.annotated("userId", "u-123")
      // You can also apply annotations to a block of code using ZIO.logAnnotate
      _ <- ZIO.logAnnotate("requestId", "req-999") {
             ZIO.logInfo("Connecting to database...") *>
             ZIO.sleep(100.millis) *>
             ZIO.logInfo("Database query successful")
           }
    yield ()

  // --- Log Spans ---
  // Spans measure the time taken to execute a specific block of code and log it.
  val logSpans =
    for
      _ <- Console.printLine("--- Logging: Spans ---")
      _ <- ZIO.logSpan("DatabaseTransaction") {
             ZIO.logInfo("Starting transaction...") *>
             ZIO.sleep(200.millis) *>
             ZIO.logInfo("Transaction committed")
           }
    yield ()

  def run =
    (basicLogging *> annotatedLogging *> logSpans).exitCode
