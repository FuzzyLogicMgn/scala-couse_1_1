package ru.otus.sc.accounting.dao.impl.map

import java.util.UUID

import ru.otus.sc.accounting.dao.{EntityDao, EntityWithId}

trait MapStoreDao[T <: EntityWithId[T]] extends EntityDao[T] {
  var entityById: Map[UUID, T] = Map.empty
  def create(ent: T): T = {
    val id     = UUID.randomUUID()
    val entity = ent.copyWithId(id)
    entityById += (id -> entity)
    entity
  }

  def update(ent: T): Option[T] = {
    for {
      id <- ent.id
      _  <- entityById.get(id)
    } yield {
      entityById += (id -> ent)
      ent
    }
  }

  def read(entityId: UUID): Option[T] = entityById.get(entityId)

  def delete(entityId: UUID): Option[T] = {
    entityById.get(entityId) match {
      case Some(value) =>
        entityById -= entityId
        Some(value)
      case None => None
    }
  }

  def findAll(): Seq[T] = entityById.values.toVector
}
