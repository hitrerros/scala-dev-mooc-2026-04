package ru.otus.module4.homework.dao.repository

import io.getquill.*
import io.getquill.context.ZioJdbc.QIO
import ru.otus.module4.homework.Ctx
import ru.otus.module4.homework.dao.entity.*
import zio.{ULayer, ZIO, ZLayer}

trait UserRepository {
  def findUser(userId: UserId): QIO[Option[User]]

  def createUser(user: User): QIO[User]

  def createUsers(users: List[User]): QIO[List[User]]

  def updateUser(user: User): QIO[Unit]

  def deleteUser(user: User): QIO[Unit]

  def findByLastName(lastName: String): QIO[List[User]]

  def list(): QIO[List[User]]

  def userRoles(userId: UserId): QIO[List[Role]]

  def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit]

  def listUsersWithRole(roleCode: RoleCode): QIO[List[User]]

  def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]]
}


class UserRepositoryImpl extends UserRepository {
  val dc = Ctx

  import dc.*

    inline def userSchema: Quoted[EntityQuery[User]] = quote {
        querySchema[User]("userok")
    }

    inline def roleSchema: Quoted[EntityQuery[Role]] = quote {
        querySchema[Role]("role")
    }

    inline def userToRoleSchema: Quoted[EntityQuery[UserToRole]] = quote {
        querySchema[UserToRole]("user_to_role")
    }

    override def findUser(userId: UserId): QIO[Option[User]] =
        dc.run(userSchema.filter(_.id == lift(userId.id))).map(_.headOption)

    override def createUser(user: User): QIO[User] = dc.run(userSchema.insertValue(lift(user))).as(user)

    override def createUsers(users: List[User]): QIO[List[User]] =
        ZIO.foreach(users) { user => createUser(user) }

    override def updateUser(user: User): QIO[Unit] = dc.run(
        userSchema.filter(_.id == lift(user.id)).updateValue(lift(user))).unit

    override def deleteUser(user: User): QIO[Unit] = dc.run(userSchema.filter(_.id == lift(user.id)).delete).unit

    override def findByLastName(lastName: String): QIO[List[User]] = dc.run(
        userSchema.filter(_.lastName == lift(lastName)))

    override def list(): QIO[List[User]] = dc.run(userSchema)


    override def userRoles(userId: UserId): QIO[List[Role]] = dc.run {
        userToRoleSchema.join(roleSchema).on((utr, role) => utr.userId == lift(userId.id) && utr.roleId == role.code).map((_, r) => r)
    }

  override def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit] =
      dc.run(userToRoleSchema.insertValue(lift(UserToRole(roleCode.code, userId.id)))).unit

  override def listUsersWithRole(roleCode: RoleCode): QIO[List[User]] =  dc.run {
      userSchema.join(userToRoleSchema).on((user, utr) => user.id == utr.userId && utr.roleId == lift(roleCode.code))
        .map((u,_) => u)
  }

  override def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]] =
        dc.run(roleSchema.filter(_.code == lift(roleCode.code))).map(_.headOption)

}

object UserRepository {

  val layer: ULayer[UserRepository] = ZLayer.succeed(new UserRepositoryImpl)
}