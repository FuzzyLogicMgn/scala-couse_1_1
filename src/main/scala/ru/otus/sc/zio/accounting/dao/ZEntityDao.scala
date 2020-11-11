package ru.otus.sc.zio.accounting.dao

import java.util.UUID

import ru.otus.sc.accounting.dao.EntityWithId
import zio.URIO

trait ZEntityDao[E, T <: EntityWithId[T]] {
  def create(ent: T): URIO[E, T]
  def update(ent: T): URIO[E, Option[T]]
  def read(entityId: UUID): URIO[E, Option[T]]
  def delete(entityId: UUID): URIO[E, Option[T]]
  def findAll(): URIO[E, Seq[T]]
}