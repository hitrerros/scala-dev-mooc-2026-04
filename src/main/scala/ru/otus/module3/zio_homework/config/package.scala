package ru.otus.module3.zio_homework

import zio.config.magnolia.*
import zio.{Config, ConfigProvider, IO}


package object config {
  case class AppConfig(host: String, port: String)
  case class EnvVarConfigValues(envKey: String, envValue: String)

  private val myConfigAutomatic: Config[AppConfig] = deriveConfig[AppConfig]

  object Configuration{
    val config: IO[Config.Error, AppConfig] = ConfigProvider.defaultProvider.load(myConfigAutomatic)
    val envConfig: Config[EnvVarConfigValues] = deriveConfig[EnvVarConfigValues]
    val default: AppConfig = AppConfig("127.0.0.1","8080")
  }
}
