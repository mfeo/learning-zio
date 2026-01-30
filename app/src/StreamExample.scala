package examples

import zio._
import zio.stream._

object StreamExample extends ZIOAppDefault:

  // Basic stream: create, transform, collect
  val basicStream: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- ZIO Streams: Basics ---").orDie
      result <- ZStream.fromIterable(1 to 10)
                  .map(_ * 2)
                  .filter(_ > 10)
                  .runCollect
      _ <- Console.printLine(s"  Doubled & filtered: ${result.toList}").orDie
    yield ()

  // ZPipeline: composable transformations
  val pipelineStream: ZIO[Any, Nothing, Unit] =
    val double   = ZPipeline.map[Int, Int](_ * 2)
    val toString = ZPipeline.map[Int, String](n => s"[$n]")
    val pipeline = double >>> toString

    for
      _ <- Console.printLine("--- ZIO Streams: Pipeline ---").orDie
      result <- ZStream.fromIterable(1 to 5)
                  .via(pipeline)
                  .runCollect
      _ <- Console.printLine(s"  Pipeline result: ${result.toList}").orDie
    yield ()

  // ZSink: fold elements into a single value
  val sinkStream: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- ZIO Streams: Sink ---").orDie
      sum <- ZStream.fromIterable(1 to 100)
               .run(ZSink.sum[Int])
      _ <- Console.printLine(s"  Sum of 1..100 = $sum").orDie
    yield ()

  // Chunked processing for performance
  val chunkedStream: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- ZIO Streams: Chunked ---").orDie
      result <- ZStream.fromIterable(1 to 20)
                  .grouped(5)
                  .map(chunk => s"chunk(${chunk.toList.mkString(",")})")
                  .runCollect
      _ <- Console.printLine(s"  Chunks: ${result.toList}").orDie
    yield ()

  // Infinite stream with take
  val infiniteStream: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- ZIO Streams: Infinite ---").orDie
      fibs <- ZStream.unfold((0, 1)) { case (a, b) => Some((a, (b, a + b))) }
                .take(10)
                .runCollect
      _ <- Console.printLine(s"  First 10 Fibonacci: ${fibs.toList}").orDie
    yield ()

  // Merging concurrent streams
  val concurrentStream: ZIO[Any, Nothing, Unit] =
    for
      _ <- Console.printLine("--- ZIO Streams: Concurrent Merge ---").orDie
      result <- ZStream.fromIterable(List("a", "b", "c"))
                  .merge(ZStream.fromIterable(List("1", "2", "3")))
                  .runCollect
      _ <- Console.printLine(s"  Merged: ${result.toList.sorted}").orDie
    yield ()

  def run =
    basicStream *>
    pipelineStream *>
    sinkStream *>
    chunkedStream *>
    infiniteStream *>
    concurrentStream
