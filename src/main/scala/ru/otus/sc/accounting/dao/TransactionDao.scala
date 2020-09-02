package ru.otus.sc.accounting.dao

import java.time.LocalDateTime
import java.util.UUID

import ru.otus.sc.accounting.model.Amount
import ru.otus.sc.accounting.service.ExchangeRatesService

trait TransactionDao extends EntityDao[Transaction] {}

case class Transaction(
    id: Option[UUID],
    accountId: UUID,
    amount: Amount,
    date: LocalDateTime = LocalDateTime.now()
) extends EntityWithId[Transaction] {
  override def copyWithId(id: UUID): Transaction = copy(Some(id))
}

class AmountOrdering(exchangeRatesService: ExchangeRatesService) extends Ordering[Transaction] {
  override def compare(x: Transaction, y: Transaction): Int = (x.amount.value - 1.toInt).toInt
}