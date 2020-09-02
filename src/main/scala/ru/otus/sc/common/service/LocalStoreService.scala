package ru.otus.sc.common.service

import scala.util.Try

/**
  * Сервис хранилище значений по заранее заданному списку ключей (расширение списка ключей не предусмотрено)
  */
trait LocalStoreService extends AppService {
  def get(key: String): Option[Any]
  def contains(key: String): Boolean
  def put(key: String, value: Any): Try[Any]

  override def getServiceName: String = "LocalStoreService"
}
