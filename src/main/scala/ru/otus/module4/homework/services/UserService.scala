package ru.otus.module4.homework.services

import io.getquill.context.ZioJdbc.QIO
import ru.otus.module4.homework.Ctx
import ru.otus.module4.homework.dao.entity.{Role, RoleCode, User, UserId}
import ru.otus.module4.homework.dao.repository.*
import zio.{ZIO, ZLayer}

trait UserService{
    def listUsers(): QIO[List[User]]
    def listUsersDTO(): QIO[List[UserDTO]]
    def addUserWithRole(user: User, roleCode: RoleCode): QIO[UserDTO]
    def listUsersWithRole(roleCode: RoleCode): QIO[List[UserDTO]]
}
class Impl(userRepo: UserRepository) extends UserService {
    val dc = Ctx
    
    def listUsers(): QIO[List[User]] =
        userRepo.list()


    def listUsersDTO(): QIO[List[UserDTO]] =
        listUsers().flatMap { users =>
            ZIO.foreach(users) { user =>
                userRepo.userRoles(UserId(user.id))
                  .map(roles => UserDTO(user, roles.toSet))
            }
        }

    def addUserWithRole(user: User, roleCode: RoleCode): QIO[UserDTO] = {
      //  dc.transaction  { // пытался здесь обернуть таким образом, но почему-то
     // постоянно ругался на несоответствие типов     
            for {
                usr <- userRepo.createUser(user)
                _ <- userRepo.insertRoleToUser(roleCode, UserId(usr.id))
                roles <- userRepo.userRoles(UserId(usr.id))

            } yield UserDTO(usr, roles.toSet)
        }
   // }

    def listUsersWithRole(roleCode: RoleCode): QIO[List[UserDTO]] = {
        for {
            usersWithRole <- userRepo.listUsersWithRole(roleCode)
            v <- ZIO.foreach(usersWithRole) {
                user => userRepo.userRoles(UserId(user.id)).map(v => UserDTO(user, v.toSet))
            }
        } yield (v)
    }


}
object UserService{
    val layer: ZLayer[UserRepository, Nothing, UserService] =  ZLayer.fromZIO {
        for {
            repo        <- ZIO.service[UserRepository]
        } yield new Impl(repo)
    }
}

case class UserDTO(user: User, roles: Set[Role])