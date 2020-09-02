package ru.otus.sc.accounting.dao

import java.util.UUID

trait EntityDao[T <: EntityWithId[T]] {
  def create(ent: T): T
  def update(ent: T): Option[T]
  def read(entityId: UUID): Option[T]
  def delete(entityId: UUID): Option[T]
  def findAll(): Seq[T]
}

trait EntityWithId[T] {
  def id: Option[UUID]
  def copyWithId(id: UUID): T
}
