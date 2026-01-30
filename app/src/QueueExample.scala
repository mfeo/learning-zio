package examples

import zio._

object QueueExample extends ZIOAppDefault:

  // Basic offer and take
  val basicQueue: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Queue: Basic ---").orDie
      queue <- Queue.bounded[String](10)
      _     <- queue.offer("hello")
      _     <- queue.offer("world")
      v1    <- queue.take
      v2    <- queue.take
      _     <- Console.printLine(s"  Took: $v1, $v2").orDie
    yield ()

  // Producer-consumer pattern with fibers
  val producerConsumer: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Queue: Producer-Consumer ---").orDie
      queue <- Queue.bounded[Int](5)
      // producer
      producer <- ZIO.foreach(1 to 10)(i =>
                    queue.offer(i) *> Console.printLine(s"  Produced: $i").orDie
                  ).fork
      // consumer
      consumer <- ZIO.foreach(1 to 10)(_ =>
                    queue.take.tap(i => Console.printLine(s"  Consumed: $i").orDie)
                  ).fork
      _ <- producer.join
      _ <- consumer.join
    yield ()

  // takeAll and takeUpTo
  val batchOps: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Queue: Batch Operations ---").orDie
      queue <- Queue.unbounded[Int]
      _     <- queue.offerAll(List(1, 2, 3, 4, 5))
      batch <- queue.takeUpTo(3)
      _     <- Console.printLine(s"  takeUpTo(3): ${batch.toList}").orDie
      rest  <- queue.takeAll
      _     <- Console.printLine(s"  takeAll: ${rest.toList}").orDie
    yield ()

  // poll: non-blocking take
  val pollExample: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Queue: Poll (non-blocking) ---").orDie
      queue <- Queue.bounded[Int](10)
      empty <- queue.poll
      _     <- Console.printLine(s"  Poll empty queue: $empty").orDie
      _     <- queue.offer(42)
      some  <- queue.poll
      _     <- Console.printLine(s"  Poll after offer: $some").orDie
    yield ()

  // Back-pressure: bounded queue suspends offer when full
  val backPressure: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- Queue: Back-pressure ---").orDie
      queue <- Queue.bounded[Int](1)
      _     <- queue.offer(1)
      // second offer will suspend; fork it
      fiber <- queue.offer(2).fork
      _     <- Console.printLine("  offer(2) is suspended (queue full)").orDie
      v     <- queue.take
      _     <- Console.printLine(s"  Took $v, offer(2) can now proceed").orDie
      _     <- fiber.join
      v2    <- queue.take
      _     <- Console.printLine(s"  Took $v2").orDie
    yield ()

  def run =
    basicQueue *>
    producerConsumer *>
    batchOps *>
    pollExample *>
    backPressure
