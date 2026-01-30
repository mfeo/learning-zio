package examples

import zio._
import zio.test._

object HubExampleSpec extends ZIOSpecDefault:

  def spec = suite("Hub Tests")(
    test("all subscribers receive every published message") {
      Hub.bounded[Int](4).flatMap { hub =>
        ZIO.scoped {
          hub.subscribe.zip(hub.subscribe).flatMap { case (sub1, sub2) =>
            for
              _ <- hub.publish(1)
              _ <- hub.publish(2)
              a <- sub1.takeAll
              b <- sub2.takeAll
            yield assertTrue(
              a.toList == List(1, 2),
              b.toList == List(1, 2)
            )
          }
        }
      }
    },

    test("subscribers are independent") {
      Hub.bounded[String](4).flatMap { hub =>
        ZIO.scoped {
          hub.subscribe.zip(hub.subscribe).flatMap { case (sub1, sub2) =>
            for
              _  <- hub.publish("msg")
              v1 <- sub1.take
              v2 <- sub2.take
            yield assertTrue(v1 == "msg", v2 == "msg")
          }
        }
      }
    },

    test("concurrent publish and consume") {
      Hub.bounded[Int](16).flatMap { hub =>
        ZIO.scoped {
          hub.subscribe.flatMap { sub =>
            for
              _      <- ZIO.foreach(1 to 5)(hub.publish).fork
              result <- ZIO.foreach(1 to 5)(_ => sub.take)
            yield assertTrue(result.toSet == (1 to 5).toSet)
          }
        }
      }
    }
  )
