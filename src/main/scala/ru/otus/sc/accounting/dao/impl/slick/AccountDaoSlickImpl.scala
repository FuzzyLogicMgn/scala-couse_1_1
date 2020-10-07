package ru.otus.sc.accounting.dao.impl.slick

import java.util.UUID

import ru.otus.sc.accounting.dao.{Account, AccountDao}
import ru.otus.sc.accounting.model.Currency.Currency
import ru.otus.sc.accounting.model.{Amount, Currency}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class AccountDaoSlickImpl(db: Database)(implicit ec: ExecutionContext) extends AccountDao {

  import AccountDaoSlickImpl._

  override def create(ent: Account): Future[Account] = {
    val newClient = AccountRow.fromAccount(ent)
    val act = for {
      clientId <- accounts.returning(accounts.map(_.id)) += newClient
    } yield ent.copy(id = Some(clientId))
    db.run(act.transactionally)
  }

  override def update(ent: Account): Future[Option[Account]] = {
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
        db.run(act.transactionally)
      case None => Future.successful(None)
    }
  }

  override def read(entityId: UUID): Future[Option[Account]] = {
    val act = for {
      user <- accounts.filter(cl => cl.id === entityId).result.headOption
    } yield user.map(_.toAccount)
    db.run(act)
  }

  override def delete(entityId: UUID): Future[Option[Account]] = {
    val act = for {
      del <- accounts.filter(_.id === entityId).forUpdate.result.headOption
      _ <- del match {
        case Some(_) => accounts.filter(cl => cl.id === entityId).delete
        case None    => DBIO.successful(())
      }
    } yield del.map(_.toAccount)
    db.run(act.transactionally)
  }

  override def findAll(): Future[Seq[Account]] = {
    val act = for {
      all <- accounts.result
    } yield all.map(_.toAccount)
    db.run(act)
  }

}

object AccountDaoSlickImpl {

  case class AccountRow(
      id: Option[UUID],
      clientId: UUID,
      org: String,
      balance: Double,
      currency: Currency
  ) {
    def toAccount: Account = Account(id, clientId, Amount(balance, currency))
  }

  object AccountRow extends ((Option[UUID], UUID, String, Double, Currency) => AccountRow) {
    def fromAccount(acc: Account): AccountRow =
      AccountRow(acc.id, acc.clientId, acc.organization, acc.amount.value, acc.amount.currency)
  }

  class Accounts(tag: Tag) extends Table[AccountRow](tag, "account") {
    val id       = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    val clientId = column[UUID]("client_id")
    val org_code = column[String]("org_code")
    val balance  = column[Double]("balance")
    val currency = column[Currency]("currency")
    def clientFK = foreignKey("client_fk", clientId, ClientDaoSlickImpl.clients)(_.id)

    override def * = (id.?, clientId, org_code, balance, currency).mapTo[AccountRow]
  }

  val accounts = TableQuery[Accounts]
}
