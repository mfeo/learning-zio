# Learning ZIO

A hands-on project for learning [ZIO 2](https://zio.dev/) — a type-safe, composable library for async and concurrent programming in Scala.

Each example is a standalone runnable app with corresponding tests. Read the code, run it, modify it, and experiment.

## Tech Stack

- **Scala** 3.6.4
- **ZIO** 2.1.16 (zio, zio-streams, zio-test)
- **Mill** 1.0.6

## Getting Started

### Prerequisites

- JDK 17+
- [Mill](https://mill-build.org/) 1.0.6+

### Commands

```bash
# Compile
mill app.compile

# Run all tests
mill app.test

# Run a specific example
mill app.runMain MainApp
mill app.runMain examples.LayerExample
mill app.runMain examples.StreamExample
mill app.runMain examples.ScheduleExample
mill app.runMain examples.RefExample
mill app.runMain examples.STMExample
mill app.runMain examples.ScopeExample
mill app.runMain examples.QueueExample
mill app.runMain examples.HubExample
mill app.runMain examples.PromiseExample

# Open Scala REPL with project classpath
mill app.console
```

## Project Structure

```
app/
├── src/
│   ├── MainApp.scala          # Entry point, basic ZIO effects & fibers
│   ├── LayerExample.scala     # ZLayer & dependency injection
│   ├── StreamExample.scala    # ZIO Streams, ZPipeline, ZSink
│   ├── ScheduleExample.scala  # Repeat, retry & schedule composition
│   ├── RefExample.scala       # Atomic mutable state with Ref
│   ├── STMExample.scala       # Software Transactional Memory
│   ├── ScopeExample.scala     # Resource management with Scope
│   ├── QueueExample.scala     # Fiber communication with Queue
│   ├── HubExample.scala       # Pub/sub broadcast with Hub
│   └── PromiseExample.scala   # One-time fiber synchronization
└── test/src/
    ├── MainAppSpec.scala
    ├── LayerExampleSpec.scala
    ├── StreamExampleSpec.scala
    ├── ScheduleExampleSpec.scala
    ├── RefExampleSpec.scala
    ├── STMExampleSpec.scala
    ├── ScopeExampleSpec.scala
    ├── QueueExampleSpec.scala
    ├── HubExampleSpec.scala
    └── PromiseExampleSpec.scala
```

## Recommended Learning Order

If you are new to ZIO, follow this order. Each topic builds on concepts from the previous one.

1. **MainApp** — understand what a ZIO effect is and how to run it
2. **RefExample** — learn how ZIO handles mutable state safely
3. **ScheduleExample** — repeat and retry effects with policies
4. **LayerExample** — structure your app with dependency injection
5. **ScopeExample** — manage resources (files, connections) safely
6. **StreamExample** — process sequences of data
7. **QueueExample** — send messages between fibers
8. **HubExample** — broadcast messages to multiple consumers
9. **PromiseExample** — synchronize fibers with one-time signals
10. **STMExample** — coordinate complex shared state atomically

---

## Example Guide

### 1. MainApp — ZIO Basics

> **Core idea:** A `ZIO[R, E, A]` is a description of a program that needs an environment `R`, may fail with `E`, or succeed with `A`. Nothing runs until you provide it to a runtime.

```scala
object MainApp extends ZIOAppDefault:
  def run = myAppLogic
```

`ZIOAppDefault` is the simplest way to run a ZIO program. You define a `run` method that returns a ZIO effect, and the framework handles the rest.

**What you will learn:**

- **Console I/O** — `Console.printLine` and `Console.readLine` are ZIO effects, not side effects. They return `ZIO` values that describe what to do, and the runtime executes them.
- **for-comprehension** — chain multiple effects sequentially. Each `<-` runs an effect and binds its result. This is how you compose ZIO programs step by step.
- **Fiber** — lightweight virtual threads managed by ZIO. Use `.fork` to start a fiber and `.join` to wait for its result. Two fibers forked at the same time run concurrently.
- **Error handling** — `ZIO.fail` creates a failed effect. `.fold` lets you handle both success and failure in one expression, converting the effect into a value that always succeeds.

**Key type:**

```
ZIO[Any, Throwable, Unit]
     │       │        └─ success type: produces Unit (nothing)
     │       └────────── error type: may throw
     └────────────────── environment: needs nothing
```

---

### 2. Ref — Thread-safe Mutable State

> **Core idea:** `Ref` is an atomic mutable reference. All operations are thread-safe — you never need locks.

**Why not just use `var`?** In concurrent programs, multiple fibers reading and writing a `var` causes race conditions. `Ref` guarantees that every `update` is atomic, even with thousands of fibers running in parallel.

**What you will learn:**

- **`Ref.make(0)`** — create a Ref with an initial value
- **`get` / `set`** — read and write the value
- **`update(_ + 1)`** — atomically apply a function to the current value
- **`updateAndGet`** — same as `update`, but also returns the new value
- **`modify`** — atomically update the state AND return a derived value. This is the most powerful operation:

```scala
// Withdraw money: return the withdrawn amount AND update the balance
ref.modify { balance =>
  if balance >= 30 then (30, balance - 30)  // (return value, new state)
  else (0, balance)                          // insufficient funds
}
```

- **Concurrent safety** — the example runs 1000 parallel increments and always gets exactly 1000
- **State machine** — model state transitions (Red → Green → Yellow → Red) with `Ref` + ADT

---

### 3. Schedule — Repeat and Retry

> **Core idea:** A `Schedule` is a reusable policy that describes when and how often to repeat or retry an effect.

**What you will learn:**

- **`repeat`** — run a successful effect multiple times. `effect.repeat(Schedule.recurs(4))` runs it 1 + 4 = 5 times total (1 initial + 4 repeats).
- **`retry`** — re-run a failed effect. The effect runs once, and if it fails, the schedule determines if and when to retry.
- **Schedule composition:**
  - `&&` (intersection) — both schedules must agree to continue. Useful for "retry up to 3 times with 100ms spacing".
  - `||` (union) — either schedule can continue. Useful for "retry for 10 seconds OR up to 5 times".
- **`retryOrElse`** — retry with a fallback value when all retries are exhausted
- **`collectAll`** — collect every schedule output (the recurrence index) into a `Chunk`

**Common patterns:**

```scala
// Retry 5 times
effect.retry(Schedule.recurs(5))

// Retry with exponential backoff, up to 10 times
effect.retry(Schedule.exponential(100.millis) && Schedule.recurs(10))

// Retry, or fall back to a default
effect.retryOrElse(Schedule.recurs(3), (err, _) => ZIO.succeed(default))
```

---

### 4. ZLayer — Dependency Injection

> **Core idea:** `ZLayer` is ZIO's built-in dependency injection. You define services as traits, implement them as classes, and wire them together with layers.

**The three-step pattern:**

```
1. Define a trait        →  trait UserRepo { def getUser(id: Int): ... }
2. Implement it          →  case class UserRepoLive() extends UserRepo { ... }
3. Create a ZLayer       →  val live: ULayer[UserRepo] = ZLayer.succeed(UserRepoLive())
```

**What you will learn:**

- **`ZIO.service[UserRepo]`** — request a service from the environment. Your business logic declares what it needs, not how to get it.
- **`.provide(layer1, layer2)`** — supply the required layers to a ZIO effect. ZIO checks at compile time that all dependencies are satisfied.
- **Test layer swapping** — in tests, replace `EmailServiceLive` with a mock that does nothing. This is why separating trait from implementation matters.

**Why this matters:** Instead of passing dependencies manually through constructors, ZIO resolves them automatically. If your program needs `UserRepo & EmailService`, you just `.provide` both layers and ZIO wires them up.

---

### 5. Scope — Resource Management

> **Core idea:** `ZIO.acquireRelease` ensures that a resource is always released, even if the program fails or is interrupted. You never forget to close a file or connection.

**The problem:** Opening a file and then crashing before closing it leaks the resource. Try/finally works but doesn't compose well with concurrent code.

**What you will learn:**

- **`ZIO.acquireRelease(acquire)(release)`** — pair acquisition with guaranteed cleanup
- **`ZIO.scoped { ... }`** — define a region where scoped resources live. When the block ends, all resources are released automatically.
- **Reverse release order** — if you acquire A then B, they are released in order B then A. This is important for dependent resources (close the cache before the database).
- **Safety on failure** — even if the effect fails with an error, the release still runs
- **`ZLayer.scoped`** — create a ZLayer from a scoped resource. The resource lives as long as the layer is in use.

```scala
// Resource is acquired, used, and automatically released
ZIO.scoped {
  for
    conn <- ZIO.acquireRelease(openConnection)(_.close)
    _    <- useConnection(conn)
  yield ()
}
// conn is guaranteed to be closed here
```

---

### 6. ZIO Streams — Processing Data Sequences

> **Core idea:** `ZStream` is a lazy, pull-based sequence of values that can be transformed and consumed efficiently. Think of it as a functional, effectful iterator.

**Three core types:**

| Type | Role | Analogy |
|---|---|---|
| `ZStream` | Produces elements | A garden hose |
| `ZPipeline` | Transforms elements | A filter attached to the hose |
| `ZSink` | Consumes elements | A bucket at the end |

**What you will learn:**

- **`ZStream.fromIterable`** — create a stream from a collection
- **`.map` / `.filter`** — transform and filter elements (just like on a `List`)
- **`ZPipeline`** — composable transformations. Chain them with `>>>`:
  ```scala
  val pipeline = ZPipeline.map[Int, Int](_ * 2) >>> ZPipeline.map[Int, String](_.toString)
  stream.via(pipeline)
  ```
- **`ZSink.sum`** — consume a stream and return the sum of all elements
- **`.grouped(5)`** — batch elements into chunks of 5
- **`ZStream.unfold`** — generate an infinite stream from a seed (the example generates Fibonacci numbers)
- **`.merge`** — run two streams concurrently and interleave their elements

---

### 7. Queue — Communication Between Fibers

> **Core idea:** `Queue` is a concurrent, back-pressured channel for sending values between fibers. One fiber produces, another consumes.

**Queue vs Hub:** Queue distributes each message to exactly one consumer (load balancing). Hub broadcasts each message to all subscribers.

**What you will learn:**

- **`Queue.bounded(n)`** — create a queue with a maximum capacity. When full, `offer` suspends until space is available (back-pressure).
- **`Queue.unbounded`** — no capacity limit (use with caution)
- **`offer` / `take`** — put an element in / take an element out. `take` suspends if the queue is empty.
- **Producer-consumer** — a classic pattern: producer and consumer run as separate fibers, communicating through the queue.
- **`takeAll` / `takeUpTo(n)`** — batch operations for consuming multiple elements at once
- **`poll`** — non-blocking take. Returns `None` if the queue is empty instead of suspending.
- **Back-pressure** — the example shows how `offer` on a full bounded queue suspends the calling fiber, which naturally slows down the producer to match the consumer's speed.

```
Producer fiber                     Consumer fiber
     │                                  │
     ├── offer(1) ──► [ Queue ] ──► take ──┤
     ├── offer(2) ──►           ──► take ──┤
     └── offer(3) ──►           ──► take ──┘
```

---

### 8. Hub — Broadcasting to Multiple Consumers

> **Core idea:** `Hub` is a concurrent pub/sub primitive. Every message published to the Hub is delivered to **all** subscribers.

**Queue vs Hub:**

```
Queue:  publish("A") → only ONE consumer gets "A"
Hub:    publish("A") → ALL subscribers get "A"
```

**What you will learn:**

- **`Hub.bounded(n)`** — create a hub with a buffer capacity
- **`hub.subscribe`** — returns a scoped `Dequeue` (read-only queue). Each subscription receives its own copy of every published message. Must be used inside `ZIO.scoped`.
- **`hub.publish`** — send a message to all current subscribers
- **Fan-out processing** — different subscribers process the same data differently (e.g., one calculates a sum, another logs values)
- **Concurrent pub/sub** — publisher and consumers run as separate fibers

```scala
Hub.bounded[String](4).flatMap { hub =>
  ZIO.scoped {
    hub.subscribe.zip(hub.subscribe).flatMap { case (sub1, sub2) =>
      for
        _ <- hub.publish("Hello")
        a <- sub1.take  // gets "Hello"
        b <- sub2.take  // also gets "Hello"
      yield ()
    }
  }
}
```

---

### 9. Promise — One-time Fiber Synchronization

> **Core idea:** `Promise` is a single-value container that starts empty and can be completed exactly once. Any fiber that calls `await` on an incomplete Promise will suspend until it is completed.

**Promise vs Ref:** `Ref` can be updated many times. `Promise` is write-once — after `succeed` or `fail`, the value never changes.

**What you will learn:**

- **`Promise.make[E, A]`** — create an empty promise with error type `E` and value type `A`
- **`succeed` / `fail`** — complete the promise with a value or an error. The second call is ignored (idempotent).
- **`await`** — suspend the current fiber until the promise is completed. Multiple fibers can `await` the same promise.
- **`isDone`** — check whether the promise has been completed, without blocking
- **Gate pattern** — a common coordination pattern: multiple worker fibers all `await` the same promise. When you call `gate.succeed(())`, all workers start simultaneously.
- **Handoff** — one fiber computes a result and passes it to another fiber through a promise

```scala
// Gate pattern: 3 workers wait for a signal
val program = for
  gate   <- Promise.make[Nothing, Unit]
  fibers <- ZIO.foreach(1 to 3)(id => (gate.await *> doWork(id)).fork)
  _      <- gate.succeed(())  // open the gate — all 3 workers start
  _      <- ZIO.foreach(fibers)(_.join)
yield ()
```

---

### 10. STM — Software Transactional Memory

> **Core idea:** STM lets you compose multiple state changes into a single atomic transaction. Either all changes apply, or none do — even under concurrency.

**Why not just use Ref?** `Ref` makes individual operations atomic, but composing multiple `Ref` updates is not atomic. For example, transferring money between two accounts requires updating both — if the program crashes between the two updates, the money disappears. STM solves this.

**What you will learn:**

- **`TRef`** — a transactional reference, like `Ref` but for use inside STM transactions
- **`STM` monad** — compose multiple reads and writes into a single transaction using for-comprehensions
- **`.commit`** — submit the transaction to be executed atomically as a `ZIO` effect
- **`STM.fail`** — abort the transaction (like a rollback)
- **`TMap`** — a transactional map with atomic `put`, `get`, and `merge` operations
- **Concurrent consistency** — the example runs 100 concurrent round-trip transfers and proves the total balance never changes

```scala
// Atomic money transfer — both accounts update together or not at all
def transfer(from: TRef[Long], to: TRef[Long], amount: Long): STM[String, Unit] =
  for
    balance <- from.get
    _       <- if balance < amount then STM.fail("Insufficient funds") else STM.unit
    _       <- from.update(_ - amount)
    _       <- to.update(_ + amount)
  yield ()

// Execute the transaction
transfer(alice, bob, 300L).commit
```

---

## How ZIO Concurrency Primitives Compare

| Primitive | Mutability | Writers | Readers | Use case |
|---|---|---|---|---|
| **Ref** | Many updates | Any fiber | Any fiber | Shared counter, config flag |
| **Promise** | Write once | One fiber | Many fibers | Signal, handoff, gate |
| **Queue** | Ongoing | Producers | One consumer per message | Work distribution |
| **Hub** | Ongoing | Publishers | All subscribers | Event broadcast |
| **STM/TRef** | Many updates | Transactional | Transactional | Multi-variable atomic updates |

## Resources

- [ZIO Documentation](https://zio.dev/overview/getting-started)
- [ZIO GitHub](https://github.com/zio/zio)
- [Mill Build Tool](https://mill-build.org/)
