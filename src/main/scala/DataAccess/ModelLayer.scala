package DataAccess

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import models._
import slick.jdbc.meta.MTable

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.parsing.json.JSONObject

/**
  * Created by incode51 on 15.11.16.
  */
object ModelLayer {

  val databaseService = new DatabaseService("jdbc:postgresql://localhost:5432/test2", "test_user2", "qwerty")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val userQuery = TableQuery[UserTable]
  val extUserQuery = TableQuery[ExtUserTable]
  val visitorQuery = TableQuery[VisitorTable]
  val friendshipQuery = TableQuery[FriendshipLikesTable]
  val tokenQuery = TableQuery[Tokens]

  def createTableIfNotInTables(tables: Vector[MTable]): Future[Option[String]] = {
    if (!tables.exists(_.name.name == userQuery.baseTableRow.tableName)) {
      databaseService.db.run(DBIO.seq(userQuery.schema.create)).flatMap(_ => Future(Some("Complete")))
    }

    else if (!tables.exists(_.name.name == visitorQuery.baseTableRow.tableName)) {
      databaseService.db.run(DBIO.seq(visitorQuery.schema.create)).flatMap(_ => Future(Some("Complete")))
    }

    else if (!tables.exists(_.name.name == friendshipQuery.baseTableRow.tableName)) {
      databaseService.db.run(DBIO.seq(friendshipQuery.schema.create)).flatMap(_ => Future(Some("Complete")))
    }

    else if (!tables.exists(_.name.name == tokenQuery.baseTableRow.tableName)) {
      databaseService.db.run(DBIO.seq(tokenQuery.schema.create)).flatMap(_ => Future(Some("Complete")))
    }

    else {
      Future(None)
    }
  }

  def checkTablesSchema: Future[Option[String]] = {
      databaseService.db.run(MTable.getTables).flatMap(createTableIfNotInTables)
  }

  def createNewUser(userInfo: User): Future[TokenEntity] = {
    databaseService.db.run(((userQuery returning userQuery) += userInfo).flatMap(newUser => (tokenQuery returning tokenQuery) += TokenEntity(userId = newUser.id)))
  }

  def getUserFromToken(token: String): Future[Seq[User]] = {
    val q = tokenQuery.filter(_.token === token).flatMap(currTocken => userQuery.filter(_.id === currTocken.userId)).take(1)
    databaseService.db.run(q.result)
  }
}
