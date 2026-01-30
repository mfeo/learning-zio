package examples

import zio._

// --- Service Definition ---

trait UserRepo:
  def getUser(id: Int): ZIO[Any, String, User]
  def allUsers: ZIO[Any, Nothing, List[User]]

trait EmailService:
  def sendEmail(to: String, body: String): ZIO[Any, String, Unit]

case class User(id: Int, name: String, email: String)

// --- Service Implementations ---

final case class UserRepoLive() extends UserRepo:
  private val users = Map(
    1 -> User(1, "Alice", "alice@example.com"),
    2 -> User(2, "Bob", "bob@example.com"),
    3 -> User(3, "Charlie", "charlie@example.com")
  )

  def getUser(id: Int): ZIO[Any, String, User] =
    ZIO.fromOption(users.get(id)).mapError(_ => s"User $id not found")

  def allUsers: ZIO[Any, Nothing, List[User]] =
    ZIO.succeed(users.values.toList)

final case class EmailServiceLive() extends EmailService:
  def sendEmail(to: String, body: String): ZIO[Any, String, Unit] =
    Console.printLine(s"  [Email] To: $to | Body: $body").orDie

// --- ZLayer definitions ---

object UserRepo:
  val live: ULayer[UserRepo] = ZLayer.succeed(UserRepoLive())

object EmailService:
  val live: ULayer[EmailService] = ZLayer.succeed(EmailServiceLive())

// --- Application logic that depends on services ---

object LayerExample extends ZIOAppDefault:

  val program: ZIO[UserRepo & EmailService, String, Unit] =
    for
      repo  <- ZIO.service[UserRepo]
      email <- ZIO.service[EmailService]
      _     <- Console.printLine("--- ZLayer Example ---").orDie
      users <- repo.allUsers
      _     <- ZIO.foreach(users)(u =>
                 email.sendEmail(u.email, s"Welcome, ${u.name}!")
               )
      _     <- Console.printLine(s"  Sent ${users.size} emails.").orDie
      // demonstrate error case
      _     <- Console.printLine("  Looking up non-existent user...").orDie
      exit  <- repo.getUser(99).exit
      _     <- Console.printLine(s"  Result: $exit").orDie
    yield ()

  def run = program.provide(
    UserRepo.live,
    EmailService.live
  )
