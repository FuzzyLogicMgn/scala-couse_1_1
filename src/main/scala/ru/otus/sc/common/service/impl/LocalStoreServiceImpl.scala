package ru.otus.sc.common.service.impl

import ru.otus.sc.common.service.LocalStoreService

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

//TODO: использовать immutable отображение
class LocalStoreServiceImpl(keys: Set[String]) extends LocalStoreService {

  val keyValueStore: mutable.Map[String, Any] =
    collection.mutable.Map.newBuilder.addAll(keys.map(k => (k, None.asInstanceOf[Any]))).result()

  override def get(key: String): Option[Any] = keyValueStore.get(key)

  override def contains(key: String): Boolean = keyValueStore.contains(key)

  override def put(key: String, value: Any): Try[Any] = {
    if (keys.contains(key)) {
      keyValueStore.put(key, value)
      Success(value)
    } else {
      Failure(new IllegalArgumentException(s"Unable to put value '$value' by key '$key'. Key is not registered"))
    }
  }
}
