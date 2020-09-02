package ru.otus.sc.greet.service

/**
  * Родитель для всех сервисов приложения.
  */
trait AppService {
  def getServiceName: String
}
