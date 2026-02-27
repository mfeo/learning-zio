package examples

import zio._

object FiberRefExample extends ZIOAppDefault:

  // --- FiberRef Basics ---
  // FiberRef is like ThreadLocal in Java, but designed for lightweight fibers.
  // It allows you to maintain context (like User ID or Request ID) that is automatically
  // propagated across asynchronous boundaries within the same fiber.
  val basicFiberRef =
    for
      _ <- Console.printLine("--- FiberRef: Basics ---")
      // Create a FiberRef with an initial value
      requestId <- FiberRef.make("unknown-req")
      
      // 'locally' temporarily changes the value only within the provided scope
      _ <- requestId.locally("req-123") {
             for
               v1 <- requestId.get
               _  <- Console.printLine(s"  Inside locally: $v1")
             yield ()
           }
      
      // Once outside the 'locally' block, it reverts to the original value
      v2 <- requestId.get
      _  <- Console.printLine(s"  Outside locally: $v2")
    yield ()

  // --- FiberRef Propagation ---
  // Child fibers inherit the initial values from their parent fiber.
  val propagationExample =
    for
      _ <- Console.printLine("--- FiberRef: Propagation to Child Fibers ---")
      userId <- FiberRef.make("guest")
      
      _ <- userId.set("admin-user")
      
      // The forked fiber automatically inherits "admin-user"
      fiber <- (ZIO.sleep(100.millis) *> 
                userId.get.flatMap(id => Console.printLine(s"  Child fiber sees: $id"))).fork
      
      _ <- fiber.join
    yield ()

  def run =
    (basicFiberRef *> propagationExample).exitCode
