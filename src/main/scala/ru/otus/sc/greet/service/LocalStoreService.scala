package ru.otus.sc.greet.service

/**
  * Сервис хранилище значений по заранее заданному списку ключей (расширение списка ключей не предусмотрено)
  */
trait LocalStoreService extends AppService {
  def get(key: String): Option[Any]
  def contains(key: String): Boolean
  def put(key: String, value: Any): Option[IllegalArgumentException]

  override def getServiceName: String = "LocalStoreService"
}
