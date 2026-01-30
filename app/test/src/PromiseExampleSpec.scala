package examples

import zio._
import zio.test._

object PromiseExampleSpec extends ZIOSpecDefault:

  def spec = suite("Promise Tests")(
    test("await returns the succeeded value") {
      for
        p <- Promise.make[Nothing, Int]
        _ <- p.succeed(42)
        v <- p.await
      yield assertTrue(v == 42)
    },

    test("await returns failure") {
      for
        p    <- Promise.make[String, Int]
        _    <- p.fail("boom")
        exit <- p.await.exit
      yield assertTrue(exit == Exit.fail("boom"))
    },

    test("isDone reflects completion state") {
      for
        p      <- Promise.make[Nothing, Unit]
        before <- p.isDone
        _      <- p.succeed(())
        after  <- p.isDone
      yield assertTrue(!before, after)
    },

    test("multiple fibers can await the same promise") {
      for
        p  <- Promise.make[Nothing, String]
        f1 <- p.await.fork
        f2 <- p.await.fork
        f3 <- p.await.fork
        _  <- p.succeed("go")
        v1 <- f1.join
        v2 <- f2.join
        v3 <- f3.join
      yield assertTrue(v1 == "go", v2 == "go", v3 == "go")
    },

    test("succeed is idempotent — second succeed is ignored") {
      for
        p  <- Promise.make[Nothing, Int]
        r1 <- p.succeed(1)
        r2 <- p.succeed(2)
        v  <- p.await
      yield assertTrue(r1, !r2, v == 1)
    },

    test("gate pattern: fibers wait until promise completes") {
      for
        gate <- Promise.make[Nothing, Unit]
        log  <- Ref.make(List.empty[Int])
        fibers <- ZIO.foreach(1 to 3)(id =>
                    (gate.await *> log.update(_ :+ id)).fork
                  )
        _      <- gate.succeed(())
        _      <- ZIO.foreach(fibers)(_.join)
        result <- log.get
      yield assertTrue(result.sorted == List(1, 2, 3))
    }
  )
