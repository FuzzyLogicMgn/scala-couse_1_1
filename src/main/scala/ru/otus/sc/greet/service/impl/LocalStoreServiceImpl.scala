package ru.otus.sc.greet.service.impl

import ru.otus.sc.greet.service.LocalStoreService

import scala.collection.mutable

class LocalStoreServiceImpl(keys: Set[String]) extends LocalStoreService {

  val keyValueStore: mutable.Map[String, Any] =
    collection.mutable.Map.newBuilder.addAll(keys.map(k => (k, None.asInstanceOf[Any]))).result()

  override def get(key: String): Option[Any] = keyValueStore.get(key)

  override def contains(key: String): Boolean = keyValueStore.contains(key)

  override def put(key: String, value: Any): Option[IllegalArgumentException] = {
    if (keys.contains(key)) {
      keyValueStore.put(key, value)
      None
    } else {
      Some(new IllegalArgumentException(s"Unable to put value '$value' by key '$key'. Key is not registered"))
    }
  }
}
