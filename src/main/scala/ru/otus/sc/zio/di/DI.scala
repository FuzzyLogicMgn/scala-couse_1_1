package ru.otus.sc.zio.di

import ru.otus.sc.zio.accounting.dao.{ZAccountDao, ZClientDao, ZExchangeRatesDao, ZTransactionDao}
import ru.otus.sc.zio.accounting.route.{ZAccountRouter, ZClientRouter, ZDocRouter}
import ru.otus.sc.zio.accounting.service.{ZAccountService, ZClientService, ZExchangeRatesService}
import ru.otus.sc.zio.config.{HttpConfig, RootConfig}
import ru.otus.sc.zio.db.SlickContext
import ru.otus.sc.zio.route.ZAppRouter.ZAppRouter
import ru.otus.sc.zio.route.{ZAppRouter, ZDirectives}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging
import zio.{Has, URLayer, ZLayer}

object DI {

  val live: URLayer[Blocking with Clock with Logging, ZAppRouter with Has[HttpConfig]] =
    (RootConfig.allConfigs ++ ZLayer.requires[Blocking with Clock with Logging]) >+>
      SlickContext.live >+>
      (ZClientDao.live ++ ZAccountDao.live ++ ZTransactionDao.live ++ ZExchangeRatesDao.live) >+>
      ZExchangeRatesService.live >>>
      (ZClientService.live ++ ZAccountService.live ++ ZDirectives.live) >>>
      (ZClientRouter.live ++ ZAccountRouter.live) >+>
      ZDocRouter.live >>>
      (ZAppRouter.live ++ RootConfig.allConfigs)
}
