package ru.otus.sc.accounting.dao.impl.slick

import java.time.LocalDateTime
import java.util.UUID

import ru.otus.sc.accounting.dao.{Transaction, TransactionDao}
import ru.otus.sc.accounting.model.Amount
import ru.otus.sc.accounting.model.Currency.Currency
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class TransactionDaoSlickImpl(db: Database)(implicit ec: ExecutionContext) extends TransactionDao {

  import TransactionDaoSlickImpl._

  override def create(ent: Transaction): Future[Transaction] = {
    val newClient = TransactionRow.fromAccount(ent)
    val act = for {
      clientId <- transactions.returning(transactions.map(_.id)) += newClient
    } yield ent.copy(id = Some(clientId))
    db.run(act.transactionally)
  }

  override def update(ent: Transaction): Future[Option[Transaction]] = {
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
        db.run(act.transactionally)
      case None => Future.successful(None)
    }
  }

  override def read(entityId: UUID): Future[Option[Transaction]] = {
    val act = for {
      user <- transactions.filter(cl => cl.id === entityId).result.headOption
    } yield user.map(_.toTransaction)
    db.run(act)
  }

  override def delete(entityId: UUID): Future[Option[Transaction]] = {
    val act = for {
      del <- transactions.filter(_.id === entityId).forUpdate.result.headOption
      _ <- del match {
        case Some(_) => transactions.filter(cl => cl.id === entityId).delete
        case None    => DBIO.successful(())
      }
    } yield del.map(_.toTransaction)
    db.run(act.transactionally)
  }

  override def findAll(): Future[Seq[Transaction]] = {
    val act = for {
      all <- transactions.result
    } yield all.map(_.toTransaction)
    db.run(act)
  }

  object TransactionDaoSlickImpl {

    case class TransactionRow(
        id: Option[UUID],
        accountId: UUID,
        balance: Double,
        currency: Currency,
        date: LocalDateTime
    ) {
      def toTransaction: Transaction = Transaction(id, accountId, Amount(balance, currency), date)
    }

    object TransactionRow
        extends ((Option[UUID], UUID, Double, Currency, LocalDateTime) => TransactionRow) {
      def fromAccount(tran: Transaction): TransactionRow =
        TransactionRow(tran.id, tran.accountId, tran.amount.value, tran.amount.currency, tran.date)
    }

    class Transactions(tag: Tag) extends Table[TransactionRow](tag, "account") {
      val id        = column[UUID]("id", O.PrimaryKey, O.AutoInc)
      val accountId = column[UUID]("account_id")
      val balance   = column[Double]("balance")
      val currency  = column[Currency]("currency")
      val date      = column[LocalDateTime]("date")

      def accountFK = foreignKey("account_fk", accountId, AccountDaoSlickImpl.accounts)(_.id)

      override def * = (id.?, accountId, balance, currency, date).mapTo[TransactionRow]
    }

    val transactions = TableQuery[Transactions]
  }

}
