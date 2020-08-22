package ru.otus.sc.greet.service

import ru.otus.sc.greet.model.{EchoRequest, EchoResponse}

/**
 * Сервис эхо. Возвращает значение принятое на вход
 */
trait EchoService extends AppService {
  def echo[T](echoRequest: EchoRequest[T]): EchoResponse[T]

  override def getServiceName: String = "EchoService"
}
