package ru.otus.sc.accounting.dao

import java.util.UUID

trait ClientDao extends EntityDao[Client] {
}

case class Client(id: Option[UUID], name: String) extends EntityWithId[Client] {
  override def copyWithId(id: UUID): Client = copy(Some(id))
}