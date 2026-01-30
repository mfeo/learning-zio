package examples

import zio._
import zio.test._

object LayerExampleSpec extends ZIOSpecDefault:

  // Test implementation that doesn't do real I/O
  val testEmailLayer: ULayer[EmailService] = ZLayer.succeed(
    new EmailService:
      def sendEmail(to: String, body: String): ZIO[Any, String, Unit] =
        ZIO.unit
  )

  def spec = suite("ZLayer Tests")(
    test("UserRepo.live returns all users") {
      for
        repo  <- ZIO.service[UserRepo]
        users <- repo.allUsers
      yield assertTrue(users.nonEmpty, users.size == 3)
    }.provide(UserRepo.live),

    test("UserRepo.live finds existing user") {
      for
        repo <- ZIO.service[UserRepo]
        user <- repo.getUser(1)
      yield assertTrue(user.name == "Alice")
    }.provide(UserRepo.live),

    test("UserRepo.live fails for missing user") {
      for
        repo <- ZIO.service[UserRepo]
        exit <- repo.getUser(99).exit
      yield assertTrue(exit == Exit.fail("User 99 not found"))
    }.provide(UserRepo.live),

    test("Services compose via ZLayer") {
      for
        repo  <- ZIO.service[UserRepo]
        email <- ZIO.service[EmailService]
        users <- repo.allUsers
        _     <- ZIO.foreach(users)(u => email.sendEmail(u.email, "hi"))
      yield assertTrue(users.size == 3)
    }.provide(UserRepo.live, testEmailLayer)
  )
