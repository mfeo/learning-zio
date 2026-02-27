package examples

import zio._
import zio.stm._

object STMExample extends ZIOAppDefault:

  // Atomic money transfer between two accounts (Atomicity)
  // STM guarantees that both the withdrawal and the deposit happen together.
  // It will never happen that account A is debited, but the program crashes
  // before account B receives the money. It's either all or nothing.
  def transfer(from: TRef[Long], to: TRef[Long], amount: Long): STM[String, Unit] =
    for
      balance <- from.get
      _       <- if balance < amount then STM.fail("Insufficient funds")
                 else STM.unit
      _       <- from.update(_ - amount)
      _       <- to.update(_ + amount)
    yield ()

  val transferExample =
    for
      _ <- Console.printLine("--- STM: Atomic Transfer ---")
      alice <- TRef.makeCommit(1000L)
      bob   <- TRef.makeCommit(200L)
      // successful transfer
      _ <- transfer(alice, bob, 300L).commit
      a1 <- alice.get.commit
      b1 <- bob.get.commit
      _ <- Console.printLine(s"  After transfer 300: Alice=$a1, Bob=$b1")
      // failed transfer (insufficient funds)
      exit <- transfer(alice, bob, 9999L).commit.exit
      _ <- Console.printLine(s"  Transfer 9999: $exit")
    yield ()

  // Multiple concurrent transfers remain consistent
  val concurrentTransfers =
    for
      _ <- Console.printLine("--- STM: Concurrent Transfers ---")
      a <- TRef.makeCommit(10000L)
      b <- TRef.makeCommit(10000L)
      // 100 concurrent transfers of 10 each way — net should be zero
      _ <- ZIO.foreachParDiscard(1 to 100) { _ =>
             transfer(a, b, 10L).commit *> transfer(b, a, 10L).commit
           }
      fa <- a.get.commit
      fb <- b.get.commit
      _ <- Console.printLine(s"  After 100 round-trips: A=$fa, B=$fb (total=${fa + fb})")
    yield ()

  // TMap: transactional map
  val tmapExample =
    for
      _ <- Console.printLine("--- STM: TMap ---")
      inventory <- TMap.empty[String, Int].commit
      _ <- STM.atomically {
             for
               _ <- inventory.put("apple", 50)
               _ <- inventory.put("banana", 30)
               _ <- inventory.merge("apple", 10)(_ + _) // 50 + 10 = 60
             yield ()
           }
      items <- inventory.toList.commit
      _ <- Console.printLine(s"  Inventory: ${items.sortBy(_._1)}")
    yield ()

  def run =
    (transferExample *> concurrentTransfers *> tmapExample).exitCode
