package examples

import zio._
import zio.test._

object ScopeExampleSpec extends ZIOSpecDefault:

  def spec = suite("Scope Tests")(
    test("acquireRelease runs release on success") {
      for
        log <- Ref.make(List.empty[String])
        _ <- ZIO.scoped {
               ZIO.acquireRelease(
                 log.update(_ :+ "acquired") *> ZIO.succeed("resource")
               )(_ => log.update(_ :+ "released")) *>
               log.update(_ :+ "used")
             }
        entries <- log.get
      yield assertTrue(entries == List("acquired", "used", "released"))
    },

    test("acquireRelease runs release on failure") {
      for
        log <- Ref.make(List.empty[String])
        _ <- ZIO.scoped {
               ZIO.acquireRelease(
                 log.update(_ :+ "acquired") *> ZIO.succeed("resource")
               )(_ => log.update(_ :+ "released")) *>
               ZIO.fail("boom")
             }.catchAll(_ => ZIO.unit)
        entries <- log.get
      yield assertTrue(entries == List("acquired", "released"))
    },

    test("multiple resources released in reverse order") {
      for
        log <- Ref.make(List.empty[String])
        _ <- ZIO.scoped {
               for
                 _ <- ZIO.acquireRelease(
                        log.update(_ :+ "acquire-A") *> ZIO.succeed("A")
                      )(_ => log.update(_ :+ "release-A"))
                 _ <- ZIO.acquireRelease(
                        log.update(_ :+ "acquire-B") *> ZIO.succeed("B")
                      )(_ => log.update(_ :+ "release-B"))
               yield ()
             }
        entries <- log.get
      yield assertTrue(entries == List("acquire-A", "acquire-B", "release-B", "release-A"))
    }
  )
