package examples

import zio._

object HubExample extends ZIOAppDefault:

  // Basic pub/sub: one publisher, multiple subscribers
  val basicHub =
    for
      _ <- Console.printLine("--- Hub: Basic Pub/Sub ---")
      _ <- Hub.bounded[String](4).flatMap { hub =>
             ZIO.scoped {
               hub.subscribe.zip(hub.subscribe).flatMap { case (sub1, sub2) =>
                 for
                   _ <- hub.publish("Hello")
                   _ <- hub.publish("World")
                   a <- sub1.takeAll
                   b <- sub2.takeAll
                   _ <- Console.printLine(s"  Subscriber 1: ${a.toList}")
                   _ <- Console.printLine(s"  Subscriber 2: ${b.toList}")
                 yield ()
               }
             }
           }
    yield ()

  // Fan-out: subscribers process messages differently
  val fanOut =
    for
      _ <- Console.printLine("--- Hub: Fan-out Processing ---")
      results <- Hub.bounded[Int](8).flatMap { hub =>
                   ZIO.scoped {
                     hub.subscribe.zip(hub.subscribe).flatMap { case (sub1, sub2) =>
                       for
                         _ <- ZIO.foreach(1 to 5)(hub.publish)
                         items1 <- sub1.takeAll
                         items2 <- sub2.takeAll
                         sum     = items1.map(_ * 10).sum // subscriber 1: multiply by 10
                         doubled = items2.map(_ * 2).toList // subscriber 2: double
                       yield (sum, doubled)
                     }
                   }
                 }
      _ <- Console.printLine(s"  Sub1 sum(*10): ${results._1}")
      _ <- Console.printLine(s"  Sub2 doubled:  ${results._2}")
    yield ()

  // Concurrent publisher and subscribers with fibers
  val concurrentHub =
    for
      _ <- Console.printLine("--- Hub: Concurrent with Fibers ---")
      collected <- Ref.make(Map.empty[String, List[String]])
      _ <- Hub.bounded[String](16).flatMap { hub =>
             ZIO.scoped {
               hub.subscribe.zip(hub.subscribe).flatMap { case (sub1, sub2) =>
                 val publisher = ZIO.foreach(List("event-A", "event-B", "event-C"))(hub.publish)

                 val consumer1 = ZIO.foreach(1 to 3) { _ =>
                   sub1.take.tap(msg =>
                     collected.update(m => m.updated("c1", m.getOrElse("c1", Nil) :+ msg))
                   )
                 }

                 val consumer2 = ZIO.foreach(1 to 3) { _ =>
                   sub2.take.tap(msg =>
                     collected.update(m => m.updated("c2", m.getOrElse("c2", Nil) :+ msg))
                   )
                 }

                 for
                   pFib  <- publisher.fork
                   c1Fib <- consumer1.fork
                   c2Fib <- consumer2.fork
                   _     <- pFib.join
                   _     <- c1Fib.join
                   _     <- c2Fib.join
                 yield ()
               }
             }
           }
      result <- collected.get
      _ <- Console.printLine(s"  Consumer 1 received: ${result.getOrElse("c1", Nil)}")
      _ <- Console.printLine(s"  Consumer 2 received: ${result.getOrElse("c2", Nil)}")
    yield ()

  def run =
    (basicHub *> fanOut *> concurrentHub).exitCode
