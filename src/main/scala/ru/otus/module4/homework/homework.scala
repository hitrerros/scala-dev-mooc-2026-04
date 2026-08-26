package ru.otus.module4

import io.getquill.{Escape, Literal, NamingStrategy, PostgresZioJdbcContext, SnakeCase}

package object homework {
    object Ctx extends PostgresZioJdbcContext(NamingStrategy(SnakeCase, Escape, Literal))
}
