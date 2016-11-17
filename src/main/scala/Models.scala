package models

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._
import java.util.UUID

case class User(id: Option[Int] = None, username:String, email: String, location:String, gender:Char, orientation:Char, birthday:DateTime, isActive:Boolean)
case class ExtUser(id:Int = 0, userId:Int = 0, photo:String, language: String, baseInfo:String)
case class FriendshipLikes(id:Int = 0, created:DateTime, creator:Int, friend:Int, accepted:Boolean)
case class Visitor(id:Int = 0, visitDate:DateTime, visitor:Int, reqUser:Int)
case class TokenEntity(id: Option[Int] = None, userId: Option[Int] = None, token: String = UUID.randomUUID().toString.replaceAll("-", ""))


class UserTable(tag: Tag) extends Table[User](tag, "USER_TABLE") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def email = column[String]("email")
  def location = column[String]("location")
  def gender = column[Char]("gender")
  def orientation = column[Char]("orientation")
  def birthday = column[DateTime]("birthday")
  def isActive = column[Boolean]("active")

  def * = (id, username, email, location, gender, orientation, birthday, isActive) <> (User.tupled, User.unapply)
}

class ExtUserTable(tag: Tag) extends Table[ExtUser](tag, "EXT_USER_TABLE") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def photo = column[String]("path_to_photo")
  def language = column[String]("language")
  def baseInfo = column[String]("base_information")
  def userId = column[Int]("USER_ID")

  def user_fk = foreignKey("USER_FK", userId, TableQuery[UserTable])(_.id.get, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def * = (id, userId, photo, language, baseInfo) <> (ExtUser.tupled, ExtUser.unapply)
}

class FriendshipLikesTable(tag: Tag) extends Table[FriendshipLikes](tag, "USERS_FRIENDS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def created = column[DateTime]("birthday")
  def creator = column[Int]("FRIENDSHIP_CREATOR_ID")
  def friend = column[Int]("FRIENDSHIP_FRIEND_ID")
  def accepted = column[Boolean]("ACCEPTED")

  def creator_fk = foreignKey("USER_CREATOR_FK", creator, TableQuery[UserTable])(_.id.get, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def friend_fk = foreignKey("USER_FRIEND_FK", friend, TableQuery[UserTable])(_.id.get, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def * = (id, created, creator, friend, accepted) <> (FriendshipLikes.tupled, FriendshipLikes.unapply)
}

class VisitorTable(tag: Tag) extends Table[Visitor](tag, "USERS_FRIENDS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def visitDate = column[DateTime]("VISIT_DATE")
  def visitor = column[Int]("VISIT_VISITOR")
  def reqUser = column[Int]("VISIT_FRIEND")

  def visitor_fk = foreignKey("USER_CREATOR_FK", visitor, TableQuery[UserTable])(_.id.get, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def reqUser_fk = foreignKey("USER_FRIEND_FK", reqUser, TableQuery[UserTable])(_.id.get, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def * = (id, visitDate, visitor, reqUser) <> (Visitor.tupled, Visitor.unapply)
}

class Tokens(tag: Tag) extends Table[TokenEntity](tag, "TOKENS") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Option[Int]]("user_id")
  def token = column[String]("token")

  def user_fk = foreignKey("USER_FK", userId, TableQuery[UserTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def * = (id, userId, token) <> (TokenEntity.tupled, TokenEntity.unapply)
}
