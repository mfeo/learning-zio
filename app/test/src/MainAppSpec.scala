import zio._
import zio.test._
import zio.test.Assertion._

object MainAppSpec extends ZIOSpecDefault:

  def spec = suite("ZIO Learning Tests")(
    test("ZIO.succeed returns a value") {
      for
        value <- ZIO.succeed(42)
      yield assertTrue(value == 42)
    },

    test("ZIO.fail creates a failed effect") {
      for
        exit <- ZIO.fail("error").exit
      yield assertTrue(exit == Exit.fail("error"))
    },

    test("ZIO.fold handles both success and failure") {
      val effect: ZIO[Any, String, Int] = ZIO.fail("boom")
      for
        result <- effect.fold(
          error   => s"caught: $error",
          success => s"got: $success"
        )
      yield assertTrue(result == "caught: boom")
    },

    test("ZIO fibers run concurrently") {
      for
        fiber1 <- ZIO.succeed(1).fork
        fiber2 <- ZIO.succeed(2).fork
        v1     <- fiber1.join
        v2     <- fiber2.join
      yield assertTrue(v1 + v2 == 3)
    },

    test("ZIO.collectAll runs effects in sequence") {
      val effects = List(ZIO.succeed(1), ZIO.succeed(2), ZIO.succeed(3))
      for
        results <- ZIO.collectAll(effects)
      yield assertTrue(results == List(1, 2, 3))
    }
  )
