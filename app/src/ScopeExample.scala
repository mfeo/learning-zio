package examples

import zio._

object ScopeExample extends ZIOAppDefault:

  // Simulated resource with acquire/release logging
  // Similar to try-catch-finally, but ZIO.acquireRelease guarantees the release
  // action will always run, even if unexpected errors occur or the fiber is interrupted.
  case class DatabaseConn(id: String)

  def acquireConn(id: String): ZIO[Scope, Nothing, DatabaseConn] =
    ZIO.acquireRelease(
      Console.printLine(s"  [$id] Acquired").orDie *> ZIO.succeed(DatabaseConn(id))
    )(conn =>
      Console.printLine(s"  [$id] Released").orDie
    )

  // Basic scoped resource
  val basicScope: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Scope: Basic ---").orDie
      _ <- ZIO.scoped {
             for
               conn <- acquireConn("db-1")
               _    <- Console.printLine(s"  Using ${conn.id}").orDie
             yield ()
           }
      _ <- Console.printLine("  (resource has been released)").orDie
    yield ()

  // Multiple resources composed — released in reverse order
  val composedScope: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Scope: Composed (reverse release order) ---").orDie
      _ <- ZIO.scoped {
             for
               db    <- acquireConn("database")
               cache <- acquireConn("cache")
               _     <- Console.printLine(s"  Using ${db.id} and ${cache.id}").orDie
             yield ()
           }
    yield ()

  // Resource safety: release runs even on failure
  val safeOnFailure: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Scope: Safe on Failure ---").orDie
      exit <- ZIO.scoped {
                for
                  _ <- acquireConn("will-release")
                  _ <- ZIO.fail("boom!")
                yield ()
              }.exit
      _ <- Console.printLine(s"  Effect failed but resource was released: $exit").orDie
    yield ()

  // Building a ZLayer from a scoped resource
  trait AppConfig:
    def get(key: String): UIO[String]

  val configLayer: ZLayer[Any, Nothing, AppConfig] =
    ZLayer.scoped {
      ZIO.acquireRelease(
        Console.printLine("  [Config] Loaded").orDie *>
          ZIO.succeed(new AppConfig:
            def get(key: String) = ZIO.succeed(s"value-of-$key")
          )
      )(_ => Console.printLine("  [Config] Unloaded").orDie)
    }

  val layerFromScope: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Scope: ZLayer from Scoped Resource ---").orDie
      _ <- ZIO.serviceWithZIO[AppConfig](_.get("db.url"))
             .tap(v => Console.printLine(s"  Config value: $v").orDie)
             .provide(configLayer)
    yield ()

  def run =
    basicScope *>
    composedScope *>
    safeOnFailure *>
    layerFromScope
