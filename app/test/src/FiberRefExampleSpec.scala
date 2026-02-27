package examples

import zio._
import zio.test._

object FiberRefExampleSpec extends ZIOSpecDefault:
  def spec = suite("FiberRef Tests")(
    test("locally temporarily changes the value") {
      for
        ref <- FiberRef.make(0)
        v1  <- ref.locally(10)(ref.get)
        v2  <- ref.get
      yield assertTrue(v1 == 10, v2 == 0)
    },
    test("child fibers inherit parent's FiberRef value") {
      for
        ref   <- FiberRef.make("init")
        _     <- ref.set("parent-value")
        child <- ref.get.fork
        v     <- child.join
      yield assertTrue(v == "parent-value")
    }
  )
