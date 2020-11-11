package ru.otus.sc.zio.accounting.dao

import java.util.UUID

import ru.otus.sc.accounting.dao.Transaction
import ru.otus.sc.zio.db.SlickContext.SlickContext
import ru.otus.sc.zio.utils.LoggingUtils.localTimed
import slick.jdbc.PostgresProfile.api._
import zio.clock.Clock
import zio.logging.Logging
import zio.macros.accessible
import zio.{Has, URIO, URLayer, ZLayer}

@accessible
object ZTransactionDao {
  type ZTransactionDao = Has[Service]
  type Env             = Logging with Clock

  trait Service extends ZEntityDao[Env, Transaction] {
    def create(ent: Transaction): URIO[Env, Transaction]
    def update(ent: Transaction): URIO[Env, Option[Transaction]]
    def read(entityId: UUID): URIO[Env, Option[Transaction]]
    def delete(entityId: UUID): URIO[Env, Option[Transaction]]
    def findAll(): URIO[Env, Seq[Transaction]]
  }

  val live: URLayer[SlickContext, ZTransactionDao] = ZLayer.fromService(slickContext =>
    new Service {

      import ru.otus.sc.accounting.dao.impl.slick.TransactionDaoSlickImpl._

      override def create(ent: Transaction): URIO[Env, Transaction] =
        localTimed("TransactionDao", "Create") {
          slickContext.run { implicit ec =>
            {
              val newClient = TransactionRow.fromAccount(ent)
              val act = for {
                clientId <- transactions.returning(transactions.map(_.id)) += newClient
              } yield ent.copy(id = Some(clientId))
              act.transactionally
            }
          }
        }

      override def update(ent: Transaction): URIO[Env, Option[Transaction]] =
        localTimed("TransactionDao", "Update") {
          slickContext.run { implicit ec =>
            ent.id match {
              case Some(clientId) =>
                val updateAct = transactions
                  .filter(cl => cl.id === clientId)
                  .map(u => (u.balance, u.currency))
                  .update((ent.amount.value, ent.amount.currency))
                val act = for {
                  upd <- transactions.filter(cl => cl.id === clientId).forUpdate.result.headOption
                  _ <- upd match {
                    case Some(_) => updateAct
                    case None    => DBIO.successful(())
                  }
                } yield upd.map(_.toTransaction)
                act.transactionally
              case None => DBIO.successful(None)
            }
          }
        }

      override def read(entityId: UUID): URIO[Env, Option[Transaction]] =
        localTimed("TransactionDao", "Read") {
          slickContext.run { implicit ec =>
            for {
              user <- transactions.filter(cl => cl.id === entityId).result.headOption
            } yield user.map(_.toTransaction)
          }
        }

      override def delete(entityId: UUID): URIO[Env, Option[Transaction]] =
        localTimed("TransactionDao", "Delete") {
          slickContext.run { implicit ec =>
            {
              val act = for {
                del <- transactions.filter(_.id === entityId).forUpdate.result.headOption
                _ <- del match {
                  case Some(_) => transactions.filter(cl => cl.id === entityId).delete
                  case None    => DBIO.successful(())
                }
              } yield del.map(_.toTransaction)
              act.transactionally
            }
          }
        }

      override def findAll(): URIO[Env, Seq[Transaction]] =
        localTimed("TransactionDao", "FindAll") {
          slickContext.run { implicit ec =>
            for {
              all <- transactions.result
            } yield all.map(_.toTransaction)
          }
        }
    }
  )
}
