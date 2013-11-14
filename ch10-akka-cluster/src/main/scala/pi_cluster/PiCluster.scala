package pi_cluster

//#imports
import language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import akka.routing.RoundRobinRouter
import akka.cluster._
import akka.cluster.ClusterEvent._
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.annotation.tailrec
//#imports

//#messages 
sealed trait PiMessage
case object Calculate extends PiMessage
case class Work(start: Int, numberOfElements: Int) extends PiMessage
case class Result(value: Double) extends PiMessage
case class ApproximatedPi(pi: Double, duration: Duration)

sealed trait State
case class Idle() extends State
case class Busy() extends State
case class CalculatePi(numberOfMessages: Int, numberOfElements:Int)
case class CalculatePiFailed(reason: String, job: CalculatePi)
case object BackendRegistration
//#messages

object PiFrontend {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val config =
      (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}")
      else ConfigFactory.empty).withFallback(
        ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
        withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[PiFrontend], name = "frontend")
    val cluster = Cluster(system)
    cluster.subscribe(frontend, classOf[ClusterDomainEvent])
 
    cluster.registerOnMemberUp {
	    import scala.util.Random._
	    import system.dispatcher
	    implicit val timeout = Timeout(10 seconds)
	    // Depending on how fast your cluster computes each computation, not all jobs may get to 
	    // run on the cluster. For purpose of illustration, we'll vary the computation length
	    for (n ← 1 to 5) {
	      (frontend ? CalculatePi(nextInt(10000), nextInt(10000))) onSuccess {
	        case result ⇒ println(result)
	      }
	      // wait a while until next request,
	      // to avoid flooding the console with output
	      Thread.sleep(2000)
	    }

    }
  }
}

//#frontend
class PiFrontend extends Actor {

  var backends = IndexedSeq.empty[(ActorRef, State)]
  var jobCounter = 0
  
  def receive = {
    case job: CalculatePi if backends.isEmpty ⇒
      sender ! CalculatePiFailed("Service unavailable, try again later", job)

    case job: CalculatePi ⇒
      jobCounter += 1
      val availBEs = backends.filter(_._2 == Idle()) // find `idle` backends
      val index = jobCounter % availBEs.size
      backends(index)._1 ! (job, sender) // send job to `Idle` backend
      val temp = backends(index)
      backends.updated(index, temp._1 → Busy()) // mark backend as `Busy`

    case (_: CalculatePi, s: State) ⇒ 
        val aBackend = backends.find(_._1 == sender).head // find the backend
        val index = backends.indexOf(aBackend)
        backends.updated(index, aBackend._1 → Idle()) // marked backend as `Idle`

    case BackendRegistration if !backends.contains(sender) ⇒
      context watch sender
      backends = backends :+ (sender, Idle())

    case Terminated(a) ⇒
      backends = backends.filterNot(_._1 == a)
  }
}
//#frontend

object PiBackend {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val config =
      (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}")
      else ConfigFactory.empty).withFallback(
        ConfigFactory.parseString("akka.cluster.roles = [backend]")).
        withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[PiBackend], name = "backend")
  }
}

//#backend
class Worker extends Actor {
     @tailrec
     private def calculatePiFor(start: Int, limit: Int, acc: Double) : Double =
        start match {
             case x if x == limit ⇒ acc
             case _               ⇒ calculatePiFor(start + 1, limit, acc + 4.0 * (1 - (start % 2) * 2) / (2 * start + 1))
         }

     def receive = {
         case Work(start, numberOfElements) ⇒ sender ! Result(calculatePiFor(start, start + numberOfElements - 1, 0.0))
     }

}

class PiBackend extends Actor {
  var originalSender : ActorRef = _
  var jobDispatcher  : ActorRef = _
  var currentJob : CalculatePi = _
  var pi: Double = _
  var numberOfResults : Int = _
  val start: Long = System.currentTimeMillis
  val numberOfWorkers = java.lang.Runtime.getRuntime.availableProcessors
  lazy val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(numberOfWorkers)), name = "routerForWorkers")

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  def receive = {
    case (job : CalculatePi, orgSender: ActorRef) ⇒ 
        originalSender = orgSender // capture the original sender i.e. the module PiFrontend
        jobDispatcher  = sender    // capture the dispatcher i.e. the actor PiFrontend
        currentJob     = job       // capture the job the dispatcher wants me to do
        for (i ← 0 until job.numberOfMessages) workerRouter ! Work(i * job.numberOfElements, job.numberOfElements)
    case Result(value) ⇒ pi += value
                         numberOfResults += 1
                         numberOfResults match {
                            case x if x == currentJob.numberOfMessages ⇒ originalSender ! ApproximatedPi(pi, duration = (System.currentTimeMillis - start).millis)
                                                                         jobDispatcher ! (currentJob, Idle())
                                                                         context.stop(self)
                            case _                ⇒
                         }
    case state: CurrentClusterState ⇒
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) ⇒ register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
        BackendRegistration
}
//#backend
