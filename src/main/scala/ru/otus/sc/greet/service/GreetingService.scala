package ru.otus.sc.greet.service

import ru.otus.sc.greet.model.{GreetRequest, GreetResponse}

trait GreetingService extends AppService {
  def greet(request: GreetRequest): GreetResponse

  override def getServiceName: String = "GreetingService"
}
