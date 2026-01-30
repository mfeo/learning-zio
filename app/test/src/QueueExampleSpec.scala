package examples

import zio._
import zio.test._

object QueueExampleSpec extends ZIOSpecDefault:

  def spec = suite("Queue Tests")(
    test("offer and take") {
      for
        q <- Queue.bounded[Int](10)
        _ <- q.offer(1)
        _ <- q.offer(2)
        v1 <- q.take
        v2 <- q.take
      yield assertTrue(v1 == 1, v2 == 2)
    },

    test("takeAll returns all items") {
      for
        q <- Queue.unbounded[Int]
        _ <- q.offerAll(List(1, 2, 3))
        all <- q.takeAll
      yield assertTrue(all.toList == List(1, 2, 3))
    },

    test("takeUpTo returns partial") {
      for
        q <- Queue.unbounded[Int]
        _ <- q.offerAll(List(1, 2, 3, 4, 5))
        batch <- q.takeUpTo(3)
      yield assertTrue(batch.toList == List(1, 2, 3))
    },

    test("poll returns None on empty queue") {
      for
        q <- Queue.bounded[Int](10)
        v <- q.poll
      yield assertTrue(v.isEmpty)
    },

    test("poll returns Some when non-empty") {
      for
        q <- Queue.bounded[Int](10)
        _ <- q.offer(42)
        v <- q.poll
      yield assertTrue(v == Some(42))
    },

    test("bounded queue back-pressures") {
      for
        q     <- Queue.bounded[Int](1)
        _     <- q.offer(1)
        fiber <- q.offer(2).fork // suspended
        _     <- q.take          // unblock
        _     <- fiber.join
        v     <- q.take
      yield assertTrue(v == 2)
    },

    test("producer-consumer with fibers") {
      for
        q <- Queue.bounded[Int](5)
        producer <- ZIO.foreach(1 to 10)(q.offer).fork
        results  <- ZIO.foreach(1 to 10)(_ => q.take)
        _        <- producer.join
      yield assertTrue(results.toSet == (1 to 10).toSet)
    }
  )
