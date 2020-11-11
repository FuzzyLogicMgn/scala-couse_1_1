package zio.dao

import java.util.UUID

import org.testcontainers.containers.PostgreSQLContainer
import ru.otus.sc.accounting.dao.Client
import ru.otus.sc.zio.accounting.dao.ZClientDao
import ru.otus.sc.zio.db.{DbConfig, SlickContext}
import zio.blocking.{Blocking, effectBlocking}
import zio.clock.Clock
import zio.logging.slf4j.Slf4jLogger
import zio.test.Assertion._
import zio.test.TestAspect.after
import zio.test.magnolia.DeriveGen
import zio.test._
import zio.{Has, URLayer, ZIO, ZLayer}

object ZClientDaoSpec extends DefaultRunnableSpec {

  private val container = ZLayer.fromAcquireRelease(effectBlocking {
    val res = new PostgreSQLContainer()
    res.start()
    res
  }.orDie)(cont => effectBlocking(cont.stop()).orDie)

  val config: URLayer[Has[PostgreSQLContainer[Nothing]], Has[DbConfig]] =
    ZLayer.fromService(container =>
      DbConfig(container.getJdbcUrl, container.getUsername, container.getPassword)
    )

  private val loggingLayer = Slf4jLogger.makeWithAnnotationsAsMdc(Nil)

  private val env = ZLayer.requires[Blocking with Clock] ++ loggingLayer >+>
    (container >>> config) >+>
    SlickContext.live >>>
    (ZClientDao.live ++ loggingLayer)

  private val pgString =
    Gen.string(Gen.oneOf(Gen.char('\u0001', '\uD7FF'), Gen.char('\uE000', '\uFFFD')))

  implicit val deriveGenString: DeriveGen[String] = DeriveGen.instance(pgString)
  private val genClient                           = DeriveGen[Client]

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    suite("ClientDaoSpec")(
      testM("Create any number of clients") {
        checkM(Gen.vectorOf(genClient), genClient) { (clients, client) =>
          for {
            _             <- ZIO.foreach_(clients)(ZClientDao.create)
            createdClient <- ZClientDao.create(client)
          } yield assert(createdClient)(
            hasField[Client, Option[UUID]](
              "id",
              _.id,
              isSome(anything) && not(equalTo(client.id))
            ) && equalTo(client.copy(id = createdClient.id))
          )
        }
      },
      suite("getClient")(
        testM("get unknown client") {
          checkM(Gen.vectorOf(genClient), Gen.anyUUID) { (clients, clientId) =>
            ZIO.foreach_(clients)(ZClientDao.create) *>
              assertM(ZClientDao.read(clientId))(isNone)
          }
        },
        testM("get known client") {
          checkM(Gen.vectorOf(genClient), genClient) { (clients1, client) =>
            for {
              _           <- ZIO.foreach_(clients1)(ZClientDao.create)
              createdUser <- ZClientDao.create(client)
              res         <- assertM(ZClientDao.read(createdUser.id.get))(isSome(equalTo(createdUser)))
            } yield res
          }
        }
      )
    ).@@(after(ZClientDao.deleteAll()))
      .provideSomeLayerShared[Environment](env)
}
