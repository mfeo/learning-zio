package examples

import zio._
import zio.stm._
import zio.test._

object STMExampleSpec extends ZIOSpecDefault:

  def transfer(from: TRef[Long], to: TRef[Long], amount: Long): STM[String, Unit] =
    for
      balance <- from.get
      _       <- if balance < amount then STM.fail("Insufficient funds") else STM.unit
      _       <- from.update(_ - amount)
      _       <- to.update(_ + amount)
    yield ()

  def spec = suite("STM Tests")(
    test("transfer moves money atomically") {
      for
        a <- TRef.makeCommit(1000L)
        b <- TRef.makeCommit(0L)
        _ <- transfer(a, b, 400L).commit
        va <- a.get.commit
        vb <- b.get.commit
      yield assertTrue(va == 600L, vb == 400L)
    },

    test("transfer fails on insufficient funds") {
      for
        a    <- TRef.makeCommit(100L)
        b    <- TRef.makeCommit(0L)
        exit <- transfer(a, b, 999L).commit.exit
        va   <- a.get.commit
      yield assertTrue(
        exit == Exit.fail("Insufficient funds"),
        va == 100L // unchanged
      )
    },

    test("concurrent transfers preserve total") {
      for
        a <- TRef.makeCommit(5000L)
        b <- TRef.makeCommit(5000L)
        _ <- ZIO.foreachParDiscard(1 to 50) { _ =>
               transfer(a, b, 10L).commit *> transfer(b, a, 10L).commit
             }
        va <- a.get.commit
        vb <- b.get.commit
      yield assertTrue(va + vb == 10000L)
    },

    test("TMap put and get") {
      for
        map <- TMap.empty[String, Int].commit
        _   <- map.put("a", 1).commit
        _   <- map.put("b", 2).commit
        va  <- map.get("a").commit
        vb  <- map.get("b").commit
      yield assertTrue(va == Some(1), vb == Some(2))
    },

    test("TMap merge combines values") {
      for
        map <- TMap.empty[String, Int].commit
        _   <- map.put("x", 10).commit
        _   <- map.merge("x", 5)(_ + _).commit
        v   <- map.get("x").commit
      yield assertTrue(v == Some(15))
    }
  )
