package examples

import zio._
import zio.test._

object SemaphoreExampleSpec extends ZIOSpecDefault:

  def spec = suite("Semaphore Tests")(
    test("withPermit limits concurrency") {
      for
        sem     <- Semaphore.make(permits = 2)
        running <- Ref.make(0)
        maxSeen <- Ref.make(0)
        _ <- ZIO.foreachPar(1 to 10) { _ =>
               sem.withPermit {
                 for
                   cur <- running.updateAndGet(_ + 1)
                   _   <- maxSeen.update(m => math.max(m, cur))
                   _   <- ZIO.yieldNow
                   _   <- running.update(_ - 1)
                 yield ()
               }
             }
        max <- maxSeen.get
      yield assertTrue(max <= 2)
    },

    test("mutex ensures exclusive access") {
      for
        mutex   <- Semaphore.make(permits = 1)
        log     <- Ref.make(List.empty[String])
        _ <- ZIO.foreachPar(1 to 3) { id =>
               mutex.withPermit {
                 log.update(_ :+ s"enter-$id") *>
                 ZIO.yieldNow *>
                 log.update(_ :+ s"exit-$id")
               }
             }
        entries <- log.get
        // verify no interleaving: entries come in enter/exit pairs
        pairs = entries.grouped(2).toList
        allPaired = pairs.forall { pair =>
          pair.size == 2 &&
          pair(0).startsWith("enter-") &&
          pair(1).startsWith("exit-") &&
          pair(0).drop(6) == pair(1).drop(5)
        }
      yield assertTrue(allPaired)
    },

    test("withPermits acquires multiple permits") {
      for
        sem <- Semaphore.make(permits = 5)
        _   <- sem.withPermits(3)(ZIO.unit)
        a   <- sem.available
      yield assertTrue(a == 5L) // all released after withPermits
    },

    test("available reflects current permit count") {
      for
        sem   <- Semaphore.make(permits = 3)
        a0    <- sem.available
        latch <- Promise.make[Nothing, Unit]
        fiber <- sem.withPermits(2)(latch.succeed(()) *> ZIO.never).fork
        _     <- latch.await // wait until permits are acquired
        a1    <- sem.available
        _     <- fiber.interrupt
        a2    <- sem.available
      yield assertTrue(a0 == 3L, a1 == 1L, a2 == 3L)
    }
  )
