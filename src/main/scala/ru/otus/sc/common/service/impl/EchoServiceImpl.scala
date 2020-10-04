package ru.otus.sc.common.service.impl

import ru.otus.sc.common.model.{EchoRequest, EchoResponse}
import ru.otus.sc.common.service.EchoService

class EchoServiceImpl extends EchoService {

  override def echo[T](echoRequest: EchoRequest[T]): EchoResponse[T] = EchoResponse(echoRequest.content)

}
