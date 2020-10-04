package ru.otus.sc.common.service.impl

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import ru.otus.sc.App
import ru.otus.sc.common.service.InvocationCounterService

class InvocationCounterServiceImpl extends InvocationCounterService {

  val map: ConcurrentHashMap[String, AtomicLong] = new ConcurrentHashMap[String, AtomicLong]()

  override def incrementAndGet(name: String): Long = {
    val cnt = map.computeIfAbsent(name, _ => new AtomicLong()).incrementAndGet()
    App.log(s"$name invoked: $cnt times")
    cnt
  }

  override def get(name: String): Option[Long] = Option(map.get(name)).collect(_.get())

  override def top(): Option[String] =
    Option(
      map.reduceEntries(
        1,
        { (first, second) => if (first.getValue.get() > second.getValue.get()) first else second }
      )
    ).collect(_.getKey)
}
