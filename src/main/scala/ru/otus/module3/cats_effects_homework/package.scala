package ru.otus.module3

package object cats_effects_homework {
  case class Account(name: String, balance: Int)
  val initialAccounts: Seq[Account] = Account("Ivanov", 0) :: Account("Petrov", 0) :: Account("Sidorov", 0) :: Nil

}
