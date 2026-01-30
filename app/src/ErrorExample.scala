package examples

import zio._

object ErrorExample extends ZIOAppDefault:

  // --- Typed Error Hierarchy ---

  sealed trait AppError
  case class NotFound(id: String)       extends AppError
  case class ValidationError(msg: String) extends AppError
  case class Unauthorized(user: String) extends AppError

  // --- Simulated services ---

  def findUser(id: String): ZIO[Any, AppError, String] =
    if id == "1" then ZIO.succeed("Alice")
    else if id == "bad" then ZIO.fail(ValidationError("Invalid ID format"))
    else ZIO.fail(NotFound(id))

  // --- Examples ---

  // catchAll: handle all typed errors
  val catchAllExample =
    for
      _ <- Console.printLine("--- Error: catchAll ---")
      result <- findUser("99").catchAll {
                  case NotFound(id)        => ZIO.succeed(s"(default user for $id)")
                  case ValidationError(m)  => ZIO.succeed(s"(invalid: $m)")
                  case Unauthorized(user)  => ZIO.succeed(s"(unauthorized: $user)")
                }
      _ <- Console.printLine(s"  Result: $result")
    yield ()

  // catchSome: handle only specific errors, let others propagate
  val catchSomeExample =
    for
      _ <- Console.printLine("--- Error: catchSome ---")
      result <- findUser("99")
                  .catchSome {
                    case NotFound(id) => ZIO.succeed(s"(fallback for $id)")
                  }
                  .catchAll(e => ZIO.succeed(s"(catch-all: $e)"))
      _ <- Console.printLine(s"  Result: $result")
    yield ()

  // mapError: transform the error type
  val mapErrorExample =
    for
      _ <- Console.printLine("--- Error: mapError ---")
      result <- findUser("99")
                  .mapError {
                    case NotFound(id) => s"User $id does not exist"
                    case other        => other.toString
                  }
                  .fold(
                    err  => s"  Error: $err",
                    user => s"  User: $user"
                  )
      _ <- Console.printLine(result)
    yield ()

  // orElse: try an alternative on failure
  val orElseExample =
    for
      _ <- Console.printLine("--- Error: orElse ---")
      result <- findUser("99")
                  .orElse(findUser("1"))
                  .orElse(ZIO.succeed("(last resort)"))
      _ <- Console.printLine(s"  Result: $result")
    yield ()

  // fold / foldZIO: handle both success and failure
  val foldExample =
    for
      _ <- Console.printLine("--- Error: fold / foldZIO ---")
      msg1 <- findUser("1").fold(
                err  => s"  Failed: $err",
                user => s"  Found: $user"
              )
      _ <- Console.printLine(msg1)
      msg2 <- findUser("99").foldZIO(
                _    => Console.printLine("  foldZIO: primary failed, trying backup").orDie *>
                        findUser("1"),
                user => ZIO.succeed(user)
              ).fold(err => s"  Both failed: $err", user => s"  Backup found: $user")
      _ <- Console.printLine(msg2)
    yield ()

  // Defects vs Failures
  // Failure = expected error in the E channel (business logic)
  // Defect  = unexpected error (bug, null pointer) — crashes the fiber
  val defectExample =
    for
      _ <- Console.printLine("--- Error: Defect vs Failure ---")
      // ZIO.fail = typed failure (in E channel)
      exit1 <- ZIO.fail("expected error").exit
      _ <- Console.printLine(s"  Failure exit: $exit1")
      // ZIO.die = defect (not in E channel, uncatchable by catchAll)
      exit2 <- ZIO.die(new RuntimeException("unexpected bug")).exit
      _ <- Console.printLine(s"  Defect exit: $exit2")
      // ZIO.attempt converts exceptions to failures
      exit3 <- ZIO.attempt(throw new RuntimeException("oops")).exit
      _ <- Console.printLine(s"  ZIO.attempt exit: $exit3")
    yield ()

  // Cause: the full story of why an effect failed
  val causeExample =
    for
      _ <- Console.printLine("--- Error: Cause ---")
      cause <- ZIO.fail("boom").cause
      _ <- Console.printLine(s"  Cause of failure: $cause")
      cause2 <- ZIO.die(new RuntimeException("bug")).cause
      _ <- Console.printLine(s"  Cause of defect: $cause2")
    yield ()

  // catchAllCause: catch both failures and defects
  val catchCauseExample =
    for
      _ <- Console.printLine("--- Error: catchAllCause ---")
      result <- ZIO.die(new RuntimeException("bug")).catchAllCause { cause =>
                  if cause.isDie then ZIO.succeed("recovered from defect")
                  else ZIO.succeed("recovered from failure")
                }
      _ <- Console.printLine(s"  Result: $result")
    yield ()

  // refineToOrDie: narrow error type, turning unexpected errors into defects
  val refineExample =
    for
      _ <- Console.printLine("--- Error: refineToOrDie ---")
      // ZIO.attempt gives ZIO[Any, Throwable, A]
      // refineToOrDie narrows to only the errors you expect
      result <- ZIO.attempt(Integer.parseInt("abc"))
                  .refineToOrDie[NumberFormatException]
                  .fold(
                    e    => s"  Caught NumberFormatException: ${e.getMessage}",
                    n    => s"  Parsed: $n"
                  )
      _ <- Console.printLine(result)
    yield ()

  def run =
    (catchAllExample *>
     catchSomeExample *>
     mapErrorExample *>
     orElseExample *>
     foldExample *>
     defectExample *>
     causeExample *>
     catchCauseExample *>
     refineExample).exitCode
