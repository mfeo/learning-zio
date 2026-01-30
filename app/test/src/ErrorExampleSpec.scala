package examples

import zio._
import zio.test._

object ErrorExampleSpec extends ZIOSpecDefault:

  sealed trait AppError
  case class NotFound(id: String)        extends AppError
  case class ValidationError(msg: String) extends AppError

  def findUser(id: String): ZIO[Any, AppError, String] =
    if id == "1" then ZIO.succeed("Alice")
    else ZIO.fail(NotFound(id))

  def spec = suite("Error Handling Tests")(

    // --- catchAll ---
    test("catchAll handles all errors") {
      for
        result <- findUser("99").catchAll {
                    case NotFound(id) => ZIO.succeed(s"default-$id")
                    case _            => ZIO.succeed("other")
                  }
      yield assertTrue(result == "default-99")
    },

    // --- catchSome ---
    test("catchSome handles matching errors") {
      for
        result <- ZIO.fail(NotFound("x"))
                    .catchSome { case NotFound(id) => ZIO.succeed(s"caught-$id") }
      yield assertTrue(result == "caught-x")
    },

    test("catchSome lets non-matching errors propagate") {
      val effect: ZIO[Any, AppError, String] =
        ZIO.fail(ValidationError("bad"))
      for
        exit <- effect
                  .catchSome { case NotFound(_) => ZIO.succeed("caught") }
                  .exit
      yield assertTrue(exit == Exit.fail(ValidationError("bad")))
    },

    // --- mapError ---
    test("mapError transforms error type") {
      for
        exit <- findUser("99")
                  .mapError { case NotFound(id) => s"missing:$id"; case e => e.toString }
                  .exit
      yield assertTrue(exit == Exit.fail("missing:99"))
    },

    // --- orElse ---
    test("orElse uses fallback on failure") {
      for
        result <- findUser("99").orElse(findUser("1"))
      yield assertTrue(result == "Alice")
    },

    // --- fold ---
    test("fold handles success") {
      for
        result <- findUser("1").fold(_ => "err", identity)
      yield assertTrue(result == "Alice")
    },

    test("fold handles failure") {
      for
        result <- findUser("99").fold(_.toString, identity)
      yield assertTrue(result == "NotFound(99)")
    },

    // --- foldZIO ---
    test("foldZIO runs effectful fallback") {
      for
        result <- findUser("99").foldZIO(
                    _ => findUser("1"),
                    u => ZIO.succeed(u)
                  )
      yield assertTrue(result == "Alice")
    },

    // --- Defect vs Failure ---
    test("ZIO.fail produces a Failure exit") {
      for
        exit <- ZIO.fail("oops").exit
      yield assertTrue(exit.isFailure)
    },

    test("ZIO.die produces a Die exit") {
      for
        exit <- ZIO.die(new RuntimeException("bug")).exit
      yield assert(exit)(Assertion.dies(Assertion.hasMessage(Assertion.equalTo("bug"))))
    },

    test("ZIO.attempt converts thrown exceptions to failures") {
      for
        exit <- ZIO.attempt(throw new RuntimeException("err")).exit
      yield assertTrue(exit.isFailure)
    },

    // --- Cause ---
    test("cause captures failure details") {
      for
        cause <- ZIO.fail("boom").cause
      yield assertTrue(cause.failureOption == Some("boom"))
    },

    test("cause captures defect details") {
      val ex = new RuntimeException("bug")
      for
        cause <- ZIO.die(ex).cause
      yield assertTrue(cause.dieOption == Some(ex))
    },

    // --- catchAllCause ---
    test("catchAllCause catches defects") {
      for
        result <- ZIO.die(new RuntimeException("bug")).catchAllCause { cause =>
                    ZIO.succeed(s"recovered: ${cause.isDie}")
                  }
      yield assertTrue(result == "recovered: true")
    },

    // --- refineToOrDie ---
    test("refineToOrDie narrows error type") {
      for
        exit <- ZIO.attempt(Integer.parseInt("abc"))
                  .refineToOrDie[NumberFormatException]
                  .exit
      yield assertTrue(exit.isFailure)
    }
  )
