package dao

import java.util.UUID

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.sc.accounting.dao.{Client, ClientDao}
import org.scalatest.matchers.should.Matchers._

abstract class ClientServiceDaoTest(createDao: () => ClientDao)
    extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks {

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

          val createdUser = dao.create(client)
          createdUser.id shouldNot be(client.id)
          createdUser.id shouldNot be(None)

          createdUser shouldBe client.copy(id = createdUser.id)
        }
      }
    }
  }

}
