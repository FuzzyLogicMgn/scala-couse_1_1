package ru.otus.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.sc.accounting.dao.{Client, ClientDao}
import ru.otus.sc.accounting.model.{
  ClientCreateRequest,
  ClientCreateResponse,
  ClientDeleteRequest,
  ClientDeleteResponse,
  ClientReadRequest,
  ClientReadResponse,
  ClientUpdateRequest,
  ClientUpdateResponse
}
import ru.otus.sc.accounting.service.impl.ClientServiceImpl

class ClientServiceTest extends AnyFreeSpec with MockFactory {

  val client1: Client = Client(Some(UUID.randomUUID()), "SomeBody")
  val client2: Client = Client(Some(UUID.randomUUID()), "SomeBody")

  "ClientServiceTests " - {
    "Should be able to create user " in {
      val dao = mock[ClientDao]
      val cs  = new ClientServiceImpl(dao)

      (dao.create _).expects(client1).returns(client2)

      cs.create(ClientCreateRequest(client1)) shouldBe ClientCreateResponse(client2)
    }

    "getUser" - {
      "should return user" in {
        val dao = mock[ClientDao]
        val srv = new ClientServiceImpl(dao)
        val id  = UUID.randomUUID()

        (dao.read _).expects(id).returns(Some(client1))

        srv.read(ClientReadRequest(id)) shouldBe ClientReadResponse.Success(client1)
      }

      "should return NotFound on unknown user" in {
        val dao = mock[ClientDao]
        val srv = new ClientServiceImpl(dao)
        val id  = UUID.randomUUID()

        (dao.read _).expects(id).returns(None)

        srv.read(ClientReadRequest(id)) shouldBe ClientReadResponse.NotFound(id)
      }
    }

    "updateUser" - {
      "should update existing user" in {
        val dao = mock[ClientDao]
        val srv = new ClientServiceImpl(dao)

        (dao.update _).expects(client1).returns(Some(client2))

        srv.update(ClientUpdateRequest(client1)) shouldBe ClientUpdateResponse.Success(client2)
      }

      "should return NotFound on unknown user" in {
        val dao = mock[ClientDao]
        val srv = new ClientServiceImpl(dao)

        (dao.update _).expects(client1).returns(None)

        srv.update(ClientUpdateRequest(client1)) shouldBe ClientUpdateResponse.NotFound(
          client1.id.get
        )
      }

      "should return CantUpdateUserWithoutId on user without id" in {
        val dao    = mock[ClientDao]
        val srv    = new ClientServiceImpl(dao)
        val client = client1.copy(id = None)

        srv.update(ClientUpdateRequest(client)) shouldBe ClientUpdateResponse.ClientWithoutId
      }
    }

    "deleteUser" - {
      "should delete user" in {
        val dao = mock[ClientDao]
        val srv = new ClientServiceImpl(dao)
        val id  = UUID.randomUUID()

        (dao.delete _).expects(id).returns(Some(client1))

        srv.delete(ClientDeleteRequest(id)) shouldBe ClientDeleteResponse.Success(client1)
      }

      "should return NotFound on unknown user" in {
        val dao = mock[ClientDao]
        val srv = new ClientServiceImpl(dao)
        val id  = UUID.randomUUID()

        (dao.delete _).expects(id).returns(None)

        srv.delete(ClientDeleteRequest(id)) shouldBe ClientDeleteResponse.NotFound(id)
      }
    }

  }
}
