package dao

import java.util.UUID

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.sc.accounting.dao.{Client, ClientDao}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class ClientServiceDaoTest
    extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks {

  def createDao(): ClientDao

  implicit val genClient: Gen[Client] = for {
    id   <- Gen.option(UUID.randomUUID())
    name <- Arbitrary.arbitrary[String]
  } yield Client(id, name)

  implicit val arbitraryClient: Arbitrary[Client] = Arbitrary(genClient)

  "ClientDaoTest" - {
    "CreateTest" - {
      "create any number of users" in {
        forAll { (users: Seq[Client], client: Client) =>
          val dao = createDao()
          users.foreach(dao.create)

          val createdUser = dao.create(client).futureValue
          createdUser.id shouldNot be(client.id)
          createdUser.id shouldNot be(None)

          createdUser shouldBe client.copy(id = createdUser.id)
        }
      }
    }
  }

}
