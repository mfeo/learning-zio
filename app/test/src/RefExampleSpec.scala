package examples

import zio._
import zio.test._

object RefExampleSpec extends ZIOSpecDefault:

  def spec = suite("Ref Tests")(
    test("make, set, get") {
      for
        ref <- Ref.make(0)
        _   <- ref.set(42)
        v   <- ref.get
      yield assertTrue(v == 42)
    },

    test("update applies function") {
      for
        ref <- Ref.make(10)
        _   <- ref.update(_ + 5)
        v   <- ref.get
      yield assertTrue(v == 15)
    },

    test("updateAndGet returns new value") {
      for
        ref <- Ref.make(3)
        v   <- ref.updateAndGet(_ * 4)
      yield assertTrue(v == 12)
    },

    test("modify atomically updates and returns derived value") {
      for
        ref <- Ref.make(100)
        withdrawn <- ref.modify { balance =>
          if balance >= 30 then (30, balance - 30) else (0, balance)
        }
        remaining <- ref.get
      yield assertTrue(withdrawn == 30, remaining == 70)
    },

    test("modify returns 0 when insufficient balance") {
      for
        ref <- Ref.make(10)
        withdrawn <- ref.modify { balance =>
          if balance >= 30 then (30, balance - 30) else (0, balance)
        }
        remaining <- ref.get
      yield assertTrue(withdrawn == 0, remaining == 10)
    },

    test("concurrent updates are safe") {
      for
        counter <- Ref.make(0)
        _       <- ZIO.foreachParDiscard(1 to 1000)(_ => counter.update(_ + 1))
        value   <- counter.get
      yield assertTrue(value == 1000)
    },

    test("state machine transitions") {
      sealed trait Light
      case object Red    extends Light
      case object Yellow extends Light
      case object Green  extends Light

      def next(l: Light): Light = l match
        case Red    => Green
        case Green  => Yellow
        case Yellow => Red

      for
        state  <- Ref.make[Light](Red)
        result <- ZIO.foreach(1 to 3)(_ => state.updateAndGet(next))
      yield assertTrue(result == List(Green, Yellow, Red))
    }
  )
