package ru.otus.sc.accounting.dao

import java.util.UUID

import scala.concurrent.Future

trait EntityDao[T <: EntityWithId[T]] {
  def create(ent: T): Future[T]
  def update(ent: T): Future[Option[T]]
  def read(entityId: UUID): Future[Option[T]]
  def delete(entityId: UUID): Future[Option[T]]
  def findAll(): Future[Seq[T]]
}

trait EntityWithId[T] {
  def id: Option[UUID]
  def copyWithId(id: UUID): T
}
