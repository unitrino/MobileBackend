import DataAccess.{DatabaseService, ModelLayer}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, Multipart, StatusCodes}
import akka.http.scaladsl.model.FormData
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import models.{TokenEntity, User}
import slick.jdbc.meta.MTable

import scala.io.StdIn
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import DataAccess.ModelLayer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshal, Unmarshaller}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json._



trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {

    private val parserISO : DateTimeFormatter = ISODateTimeFormat.yearMonthDay()

    override def write(obj: DateTime) = JsString(parserISO.print(obj))

    override def read(json: JsValue) : DateTime = json match {
      case JsString(s) => parserISO.parseDateTime(s)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit val userFormat = jsonFormat8(User)

}

case class ourFunc(elem: String => String)
case class testF(e1:Int, e2:Int)


class MyActor extends Actor {
  
  def receive = {
    case "test" => println("test"); sender ! "test"
    case fin:ourFunc => println(fin.elem("qwe"))
    case _      => {
      sender ! "Another"
    }
  }
}

object Main extends App with JsonSupport {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    implicit val orderUM: FromRequestUnmarshaller[testF] = new Unmarshaller[HttpRequest, testF] {
      override def apply(value: HttpRequest)(implicit ec: ExecutionContext, materializer: Materializer): Future[testF] = {
        val d = Unmarshal(value.entity).to[FormData]
        println(d.value.get.get.fields.get("e1"))
        Future(testF(d.value.get.get.fields.get("e1").get.toInt + 1, d.value.get.get.fields.get("e1").get.toInt + 2))
      }
    }

    val route: Route =
      post {
        path("new_user") {
          entity(as[User]) { userInfo =>
            onSuccess(ModelLayer.createNewUser(userInfo)) { item: TokenEntity => complete(item.token) }
          }
        }
      } ~
      post {
        path("get_info") { req =>
          val token: String = req.request.getHeader("HTTP_TOCKEN").get().value()
          req.complete(ModelLayer.getUserFromToken(token).flatMap{ answ => Future(answ.toJson) })
        }
      } ~
      post {
        path("test_path") {
          {
            entity(as[testF]) { userInfo =>
              println(userInfo)
              complete("qwe")
            }
          }
        }
      } ~
      get {
        pathPrefix("func" / LongNumber) { id =>
          val newAcc = system.actorOf(Props[MyActor], name=s"myactor$id")
          val answ = newAcc ! ourFunc((elem: String) => elem + " LOG")
          complete("READY")
        }
      } ~
      get {
        path("buildTables") {
          onSuccess(ModelLayer.checkTablesSchema) { answ => complete(answ) }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

}
