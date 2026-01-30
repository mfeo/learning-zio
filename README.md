# Learning ZIO

A hands-on project for learning [ZIO 2](https://zio.dev/) — a type-safe, composable library for async and concurrent programming in Scala.

## Tech Stack

- **Scala** 3.6.4
- **ZIO** 2.1.16 (zio, zio-streams, zio-test)
- **Mill** 1.0.6

## Project Structure

```
app/
├── src/
│   ├── MainApp.scala          # Entry point, basic ZIO effects & fibers
│   ├── LayerExample.scala     # ZLayer & dependency injection
│   ├── StreamExample.scala    # ZIO Streams, ZPipeline, ZSink
│   ├── ScheduleExample.scala  # Repeat, retry & schedule composition
│   └── RefExample.scala       # Atomic mutable state with Ref
└── test/src/
    ├── MainAppSpec.scala
    ├── LayerExampleSpec.scala
    ├── StreamExampleSpec.scala
    ├── ScheduleExampleSpec.scala
    └── RefExampleSpec.scala
```

## Topics Covered

| Module | Concepts |
|---|---|
| **MainApp** | `ZIOAppDefault`, `Console`, for-comprehension, `Fiber` (fork/join), error handling with `fold` |
| **LayerExample** | Service trait pattern, `ZLayer.succeed`, `ZIO.service`, `.provide()`, test layer swapping |
| **StreamExample** | `ZStream`, `map`/`filter`, `ZPipeline` composition (`>>>`), `ZSink.sum`, `.grouped`, `unfold` (Fibonacci), `.merge` |
| **ScheduleExample** | `Schedule.recurs`, `Schedule.spaced`, `&&` composition, `retry`, `retryOrElse`, `collectAll` |
| **RefExample** | `Ref.make`, `get`/`set`/`update`/`updateAndGet`, `modify`, concurrent counter, state machine |

## Getting Started

### Prerequisites

- JDK 17+
- [Mill](https://mill-build.org/) 1.0.6+

### Commands

```bash
# Compile
mill app.compile

# Run tests
mill app.test

# Run examples
mill app.runMain MainApp
mill app.runMain examples.LayerExample
mill app.runMain examples.StreamExample
mill app.runMain examples.ScheduleExample
mill app.runMain examples.RefExample

# Open Scala REPL with project classpath
mill app.console
```

## Resources

- [ZIO Documentation](https://zio.dev/overview/getting-started)
- [ZIO GitHub](https://github.com/zio/zio)
- [Mill Build Tool](https://mill-build.org/)
