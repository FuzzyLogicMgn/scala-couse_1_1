package ru.otus.sc.zio.accounting.dao

import java.util.UUID

import ru.otus.sc.accounting.dao.Account
import ru.otus.sc.zio.db.SlickContext.SlickContext
import ru.otus.sc.zio.utils.LoggingUtils.localTimed
import slick.jdbc.PostgresProfile.api._
import zio.clock.Clock
import zio.logging.Logging
import zio.macros.accessible
import zio.{Has, URIO, URLayer, ZLayer}

@accessible
object ZAccountDao {
  type ZAccountDao = Has[Service]
  type Env         = Logging with Clock

  trait Service extends ZEntityDao[Env, Account] {
    def create(ent: Account): URIO[Env, Account]
    def update(ent: Account): URIO[Env, Option[Account]]
    def read(entityId: UUID): URIO[Env, Option[Account]]
    def delete(entityId: UUID): URIO[Env, Option[Account]]
    def findAll(): URIO[Env, Seq[Account]]
  }

  val live: URLayer[SlickContext, ZAccountDao] = ZLayer.fromService(slickContext =>
    new Service {

      import ru.otus.sc.accounting.dao.impl.slick.AccountDaoSlickImpl._

      override def create(ent: Account): URIO[Env, Account] =
        localTimed("AccountDao", "CreateAccount") {
          slickContext.run { implicit ec =>
            val newClient = AccountRow.fromAccount(ent)
            val res = for {
              clientId <- accounts.returning(accounts.map(_.id)) += newClient
            } yield ent.copy(id = Some(clientId))
            res.transactionally
          }
        }

      override def update(ent: Account): URIO[Env, Option[Account]] =
        localTimed("AccountDao", "UpdateAccount") {
          slickContext.run { implicit ec =>
            ent.id match {
              case Some(clientId) =>
                val updateAct = accounts
                  .filter(cl => cl.id === clientId)
                  .map(u => (u.org_code, u.balance, u.currency))
                  .update((ent.organization, ent.amount.value, ent.amount.currency))
                val act = for {
                  upd <- accounts.filter(cl => cl.id === clientId).forUpdate.result.headOption
                  _ <- upd match {
                    case Some(_) => updateAct
                    case None    => DBIO.successful(())
                  }
                } yield upd.map(_.toAccount)
                act.transactionally
              case None => DBIO.successful(None)
            }
          }
        }

      override def read(entityId: UUID): URIO[Env, Option[Account]] =
        localTimed("AccountDao", "ReadAccount") {
          slickContext.run(implicit ec => {
            for {
              user <- accounts.filter(cl => cl.id === entityId).result.headOption
            } yield user.map(_.toAccount)
          })
        }

      override def delete(entityId: UUID): URIO[Env, Option[Account]] =
        localTimed("AccountDao", "DeleteAccount") {
          slickContext.run(implicit ec => {
            val act = for {
              del <- accounts.filter(_.id === entityId).forUpdate.result.headOption
              _ <- del match {
                case Some(_) => accounts.filter(cl => cl.id === entityId).delete
                case None    => DBIO.successful(())
              }
            } yield del.map(_.toAccount)
            act.transactionally
          })
        }

      override def findAll(): URIO[Env, Seq[Account]] =
        localTimed("AccountDao", "FindAllAccount") {
          slickContext.run(implicit ec =>
            for {
              all <- accounts.result
            } yield all.map(_.toAccount)
          )
        }
    }
  )
}
