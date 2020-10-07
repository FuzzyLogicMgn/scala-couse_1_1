package ru.otus.sc.accounting.dao.impl.map

import java.util.UUID

import ru.otus.sc.accounting.dao.{EntityDao, EntityWithId}

import scala.concurrent.Future

trait MapStoreDao[T <: EntityWithId[T]] extends EntityDao[T] {
  var entityById: Map[UUID, T] = Map.empty
  def create(ent: T): Future[T] = {
    val id     = UUID.randomUUID()
    val entity = ent.copyWithId(id)
    entityById += (id -> entity)
    Future.successful(entity)
  }

  def update(ent: T): Future[Option[T]] = {
    val res = for {
      id <- ent.id
      _ <- entityById.get(id)
    } yield {
      entityById += (id -> ent)
      ent
    }
    Future.successful(res)
  }

  def read(entityId: UUID): Future[Option[T]] = Future.successful(entityById.get(entityId))

  def delete(entityId: UUID): Future[Option[T]] = {
    val res = entityById.get(entityId) match {
      case Some(value) =>
        entityById -= entityId
        Some(value)
      case None => None
    }
    Future.successful(res)
  }

  def findAll(): Future[Seq[T]] = Future.successful(entityById.values.toVector)
}
