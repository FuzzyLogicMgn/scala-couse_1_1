package ru.otus.sc.greet.service

/**
 * Сервис подсчёта количества вызвов
 */
trait InvocationCounterService extends AppService {
  def incrementAndGet(name: String): Long
  def get(name: String): Option[Long]

  /**
   *
   * @return имя метода вызованного наиболее количество раз
   */
  def top(): Option[String]

  override def getServiceName: String = "InvocationCounterService"
}
