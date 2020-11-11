package ru.otus.sc.zio.accounting.dao

import java.util.UUID

import ru.otus.sc.accounting.dao.Client
import ru.otus.sc.accounting.dao.impl.slick.ClientDaoSlickImpl.{ClientRow, clients}
import ru.otus.sc.zio.accounting.service.ZClientService.Env
import ru.otus.sc.zio.db.SlickContext.SlickContext
import ru.otus.sc.zio.utils.LoggingUtils.localTimed
import slick.jdbc.PostgresProfile.api._
import zio.clock.Clock
import zio.logging.Logging
import zio.macros.accessible
import zio.{Has, URIO, URLayer, ZIO, ZLayer}

@accessible
object ZClientDao {
  type ZClientDao = Has[Service]
  type Env        = Logging with Clock

  // Can't inherit ZEntityDao here, accessible macro will not work at this case...
  trait Service extends ZEntityDao[Env, Client] {
    def create(ent: Client): URIO[Env, Client]
    def update(ent: Client): URIO[Env, Option[Client]]
    def read(entityId: UUID): URIO[Env, Option[Client]]
    def delete(entityId: UUID): URIO[Env, Option[Client]]
    def findAll(): URIO[Env, Seq[Client]]

    private[accounting] def deleteAll(): URIO[Env, Unit]
  }

  val live: URLayer[SlickContext, ZClientDao] = ZLayer.fromService(slickContext =>
    new Service {

      override def create(ent: Client): URIO[Env, Client] =
        localTimed("ClientDao", "Create") {
          slickContext.run { implicit ec =>
            {
              val newClient = ClientRow.fromClient(ent)
              val act = for {
                clientId <- clients.returning(clients.map(_.id)) += newClient
              } yield ent.copy(id = Some(clientId))
              act.transactionally
            }
          }
        }

      override def update(ent: Client): URIO[Env, Option[Client]] =
        localTimed("ClientDao", "Update") {
          slickContext.run { implicit ec =>
            ent.id match {
              case Some(clientId) =>
                val updateAct =
                  clients.filter(cl => cl.id === clientId).map(u => u.name).update(ent.name)
                val act = for {
                  upd <- clients.filter(cl => cl.id === clientId).forUpdate.result.headOption
                  _ <- upd match {
                    case Some(_) => updateAct
                    case None    => DBIO.successful(())
                  }
                } yield upd.map(_.toClient)
                act.transactionally
              case None => DBIO.successful(None)
            }
          }
        }

      override def read(entityId: UUID): URIO[Env, Option[Client]] =
        localTimed("ClientDao", "Read") {
          slickContext.run { implicit ec =>
            for {
              user <- clients.filter(cl => cl.id === entityId).result.headOption
            } yield user.map(_.toClient)
          }
        }

      override def delete(entityId: UUID): URIO[Env, Option[Client]] =
        localTimed("ClientDao", "Delete") {
          slickContext.run { implicit ec =>
            {
              val act = for {
                del <- clients.filter(_.id === entityId).forUpdate.result.headOption
                _ <- del match {
                  case Some(_) => clients.filter(cl => cl.id === entityId).delete
                  case None    => DBIO.successful(())
                }
              } yield del.map(_.toClient)
              act.transactionally
            }
          }
        }

      override def findAll(): URIO[Env, Seq[Client]] =
        localTimed("ClientDao", "FindAll") {
          slickContext.run { implicit ec =>
            for {
              all <- clients.result
            } yield all.map(_.toClient)
          }
        }

      private[accounting] def deleteAll(): URIO[Env, Unit] =
        localTimed("ClientDao", "DeleteAll") {
          slickContext.run(_ => clients.delete).unit
        }
    }
  )
}
