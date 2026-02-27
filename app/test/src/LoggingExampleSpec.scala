package examples

import zio._
import zio.test._

object LoggingExampleSpec extends ZIOSpecDefault:
  def spec = suite("Logging Tests")(
    test("ZIO Test captures logs") {
      for
        _    <- ZIO.logInfo("Test message")
        logs <- ZTestLogger.logOutput
      yield assertTrue(
        logs.length == 1,
        logs.head.message() == "Test message"
      )
    },
    test("Log annotations are captured") {
      for
        _    <- ZIO.logInfo("Annotated").@@(ZIOAspect.annotated("key", "value"))
        logs <- ZTestLogger.logOutput
      yield assertTrue(
        logs.head.annotations.contains("key"),
        logs.head.annotations("key") == "value"
      )
    }
  )
