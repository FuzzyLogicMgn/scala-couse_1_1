package dao.map

import dao.ClientServiceDaoTest
import ru.otus.sc.accounting.dao.ClientDao
import ru.otus.sc.accounting.dao.impl.map.ClientDaoImpl

class ClientServiceMapDaoTest extends ClientServiceDaoTest {
  override def createDao(): ClientDao = new ClientDaoImpl
}
