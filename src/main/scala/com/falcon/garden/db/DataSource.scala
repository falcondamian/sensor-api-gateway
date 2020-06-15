package com.falcon.garden.db

import java.net.URI

import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DataSource {

  def transactor()(implicit contextShift: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] = {

    val dbUri    = new URI(System.getenv("DATABASE_URL"))
    val username = dbUri.getUserInfo.split(":")(0)
    val password = dbUri.getUserInfo.split(":")(1)
    val dbUrl    = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath + "?sslmode=require"

    for {
      connectEc  <- ExecutionContexts.fixedThreadPool[IO](32)
      transactEc <- ExecutionContexts.cachedThreadPool[IO]
      xa <- HikariTransactor
        .newHikariTransactor[IO]("org.postgresql.Driver",
                                 dbUrl,
                                 username,
                                 password,
                                 connectEc,
                                 Blocker.liftExecutionContext(transactEc))
      _ <- Resource.liftF(xa.configure { ds =>
        IO.apply {
          ds.setMaximumPoolSize(10)
          ds.setConnectionTimeout(30000)
          ds.setMaxLifetime(1800000)
          ds.addDataSourceProperty("useAffectedRows", "true")
        }
      })
    } yield xa
  }
}
