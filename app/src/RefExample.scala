package examples

import zio._

object RefExample extends ZIOAppDefault:

  // Basic Ref operations: make, get, set, update
  val basicRef: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Ref: Basics ---").orDie
      ref <- Ref.make(0)
      _   <- ref.set(10)
      v1  <- ref.get
      _   <- Console.printLine(s"  After set(10): $v1").orDie
      _   <- ref.update(_ + 5)
      v2  <- ref.get
      _   <- Console.printLine(s"  After update(_ + 5): $v2").orDie
      v3  <- ref.updateAndGet(_ * 2)
      _   <- Console.printLine(s"  After updateAndGet(_ * 2): $v3").orDie
    yield ()

  // modify: atomically update state and return a derived value
  val modifyRef: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Ref: Modify ---").orDie
      ref <- Ref.make(100)
      withdrawn <- ref.modify { balance =>
        if balance >= 30 then (30, balance - 30)
        else (0, balance)
      }
      remaining <- ref.get
      _ <- Console.printLine(s"  Withdrew: $withdrawn, remaining: $remaining").orDie
    yield ()

  // Concurrent counter: prove Ref is thread-safe
  val concurrentCounter: ZIO[Any, Nothing, Unit] =
    for
      _       <- Console.printLine("--- Ref: Concurrent Counter ---").orDie
      counter <- Ref.make(0)
      _       <- ZIO.foreachParDiscard(1 to 1000)(_ => counter.update(_ + 1))
      value   <- counter.get
      _       <- Console.printLine(s"  After 1000 parallel increments: $value").orDie
    yield ()

  // State machine with Ref
  val stateMachine: ZIO[Any, Nothing, Unit] =
    sealed trait Light
    case object Red    extends Light
    case object Yellow extends Light
    case object Green  extends Light

    def next(l: Light): Light = l match
      case Red    => Green
      case Green  => Yellow
      case Yellow => Red

    for
      _     <- Console.printLine("--- Ref: State Machine ---").orDie
      state <- Ref.make[Light](Red)
      transitions <- ZIO.foreach(1 to 6) { _ =>
        state.updateAndGet(next)
      }
      _ <- Console.printLine(s"  Transitions: ${transitions.toList}").orDie
    yield ()

  def run =
    basicRef *>
    modifyRef *>
    concurrentCounter *>
    stateMachine
