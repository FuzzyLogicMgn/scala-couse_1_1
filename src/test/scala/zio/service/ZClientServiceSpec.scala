package zio.service

import java.util.UUID

import ru.otus.sc.accounting.dao.Client
import ru.otus.sc.accounting.model.ClientReadResponse.Success
import ru.otus.sc.accounting.model.{
  ClientCreateRequest,
  ClientReadRequest,
  ClientReadResponse,
  ClientUpdateRequest,
  ClientUpdateResponse
}
import ru.otus.sc.zio.accounting.dao.ZClientDao
import ru.otus.sc.zio.accounting.service.ZClientService
import zio._
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.test.Assertion._
import zio.test.mock.Expectation._
import zio.test.mock._
import zio.test.{testM, _}

@mockable[ZClientDao.Service]
object ClientDaoMock

object ZClientServiceSpec extends DefaultRunnableSpec {

  private val client1 = Client(Some(UUID.randomUUID()), "SomeName1")
  private val client2 = Client(Some(UUID.randomUUID()), "SomeName2")

  type TestEnv = Environment with Logging

  override def spec: Spec[_root_.zio.test.environment.TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("ClientServiceTests")(
      testM("createClient") {
        val env    = ClientDaoMock.Create(equalTo(client1), value(client2))
        val action = ZClientService.create(ClientCreateRequest(client1))
        assertM(
          action.provideSomeLayer[TestEnv](env >>> ZClientService.live)
        )(hasField("client", _.client, equalTo(client2)))
      },
      testM("updateClient") {
        val updateClient = client1.copy(name = "SomeOtherName")
        val env          = ClientDaoMock.Update(equalTo(updateClient), value(Some(updateClient)))
        val action       = ZClientService.update(ClientUpdateRequest(updateClient))
        assertM(
          action.provideSomeLayer[TestEnv](env >>> ZClientService.live) map {
            case ClientUpdateResponse.Success(client) => client
          }
        )(equalTo(updateClient))
      },
      testM("updateNotExistingClient") {
        val client1 = Client(None, "SomeName1")
        val env     = ClientDaoMock.Update(equalTo(client1), value(Some(client2))).atMost(0)
        val action  = ZClientService.update(ClientUpdateRequest(client1))
        assertM(
          action.provideSomeLayer[TestEnv](env >>> ZClientService.live)
        )(anything)
      },
      testM("readClient") {
        val clientId = UUID.randomUUID()
        val env      = ClientDaoMock.Read(equalTo(clientId), value(Some(client2)))
        val action   = ZClientService.read(ClientReadRequest(clientId))
        val act: ZIO[TestEnv, Nothing, ClientReadResponse] =
          action.provideSomeLayer[TestEnv](env >>> ZClientService.live)
        assertM(act.map {
          case Success(client) => client
        })(equalTo(client2))
      }
    ).provideSomeLayerShared[Environment](Slf4jLogger.makeWithAnnotationsAsMdc(Nil))
}
