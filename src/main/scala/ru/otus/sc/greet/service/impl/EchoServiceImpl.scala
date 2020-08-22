package ru.otus.sc.greet.service.impl

import ru.otus.sc.greet.model.{EchoRequest, EchoResponse}
import ru.otus.sc.greet.service.EchoService

class EchoServiceImpl extends EchoService {

  override def echo[T](echoRequest: EchoRequest[T]): EchoResponse[T] = EchoResponse(echoRequest.content)

}
