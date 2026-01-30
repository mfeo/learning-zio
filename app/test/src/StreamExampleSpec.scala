package examples

import zio._
import zio.stream._
import zio.test._

object StreamExampleSpec extends ZIOSpecDefault:

  def spec = suite("ZIO Stream Tests")(
    test("map and filter") {
      for
        result <- ZStream.fromIterable(1 to 10)
                    .map(_ * 2)
                    .filter(_ > 10)
                    .runCollect
      yield assertTrue(result.toList == List(12, 14, 16, 18, 20))
    },

    test("pipeline composition") {
      val pipeline = ZPipeline.map[Int, Int](_ + 1) >>> ZPipeline.map[Int, String](_.toString)
      for
        result <- ZStream(1, 2, 3).via(pipeline).runCollect
      yield assertTrue(result.toList == List("2", "3", "4"))
    },

    test("ZSink.sum") {
      for
        sum <- ZStream.fromIterable(1 to 10).run(ZSink.sum[Int])
      yield assertTrue(sum == 55)
    },

    test("grouped chunks") {
      for
        result <- ZStream.fromIterable(1 to 6).grouped(2).runCollect
      yield assertTrue(
        result.map(_.toList).toList == List(List(1, 2), List(3, 4), List(5, 6))
      )
    },

    test("unfold generates fibonacci") {
      for
        fibs <- ZStream.unfold((0, 1)) { case (a, b) => Some((a, (b, a + b))) }
                  .take(7)
                  .runCollect
      yield assertTrue(fibs.toList == List(0, 1, 1, 2, 3, 5, 8))
    },

    test("merge combines two streams") {
      for
        result <- ZStream(1, 2).merge(ZStream(3, 4)).runCollect
      yield assertTrue(result.toSet == Set(1, 2, 3, 4))
    }
  )
