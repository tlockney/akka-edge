package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Akka

import com.typesafe.config._
import akka.cluster._
import akka.cluster.ClusterEvent._
import akka.actor._
import scala.concurrent.duration._
import collection.mutable.HashSet
import scala.reflect.ClassTag
import akka.pattern.ask
import akka.util.Timeout
import scala.reflect.runtime.universe._

object Application extends Controller {

    import play.api.Play.current
    import pi_cluster._

    def loadClusterConfiguration() : Config = ConfigFactory.load("ClusterSystem.conf")

    def createActorSystem(configuration: Config) = ActorSystem.create("ClusterSystem", configuration)

    def createFrontendService(actorSystem: ActorSystem) = actorSystem.actorOf(Props[PiFrontend], "frontend")

    def createClusterAndWatch(actorSystem: ActorSystem)(service: ActorRef) = Cluster(actorSystem).subscribe(service, classOf[ClusterDomainEvent])

    val actorSystem = createActorSystem(loadClusterConfiguration)
    
    val frontend = createFrontendService(actorSystem)

    createClusterAndWatch(actorSystem)(frontend)
    
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    implicit val timeout = Timeout(5 seconds)

    def index() = Action {
        Ok(views.html.index("Developing an Akka Edge using Play 2.2.1"))
    }
    def calculatePi() = Action.async {
        import scala.util.Random._
        val limit = 10000
        val catchAll = play.api.libs.concurrent.Promise.timeout("doh!", 5 seconds)
        import scala.concurrent.Future
        val f = frontend ? pi_cluster.CalculatePi(nextInt(limit), nextInt(limit))
        Future.firstCompletedOf(Seq(f, catchAll)) map {
            case msg : String ⇒ NotFound
            case f: pi_cluster.CalculatePiFailed ⇒ Ok(f.reason)
            case data: pi_cluster.ApproximatedPi   ⇒ Ok(data.pi.toString)
        }
    }
}

