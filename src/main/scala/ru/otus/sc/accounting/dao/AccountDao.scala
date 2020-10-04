package ru.otus.sc.accounting.dao

import java.util.UUID

import ru.otus.sc.accounting.model.{Amount, Currency}

trait AccountDao extends EntityDao[Account] {
}

case class Account(id: Option[UUID], clientId: UUID, amount: Amount) extends EntityWithId[Account] {
  override def copyWithId(id: UUID): Account = copy(Some(id))
}
