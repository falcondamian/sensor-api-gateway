package com.falcon.garden.db

import java.net.URI

import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DataSource {

  def transactor()(implicit contextShift: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] = {

    val dbUri = new URI(
      if (System.getenv("DATABASE_URL") != null) System.getenv("DATABASE_URL")
      else
        "postgres://rodcomgkcquvql:e2b35f7a1f4a4a804c891458ee6d01405bab5dffeb7f66d86eee2a538906341a@ec2-54-217-204-34.eu-west-1.compute.amazonaws.com:5432/da511bd9ka0a7l")
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
