package examples

import zio._
import zio.test._

object AspectExampleSpec extends ZIOSpecDefault:
  def spec = suite("Aspect Tests")(
    test("retry aspect retries the effect") {
      for
        ref   <- Ref.make(0)
        task  = ref.updateAndGet(_ + 1).flatMap(n => if n < 3 then ZIO.fail("fail") else ZIO.succeed("ok"))
        res   <- task @@ ZIOAspect.retry(Schedule.recurs(3))
        count <- ref.get
      yield assertTrue(res == "ok", count == 3)
    }
  )
