package pi_actors

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import scala.annotation.tailrec
//
// A strategy common in parallel computing is to partition the work/ workload
// across various computing resources. The application that is suitable for such
// strategies depends first and foremost whether the algorithm can be parallelized
// and those that do, exhibit either task-parallelism, data-parallelism or both.
// Examples of algorithms that exhibit both is the matrix-matrix multiplication problem
// 
object Pi extends App {
    
    calculate(numberOfWorkers = 4, numberOfElements = 10000, numberOfMessages = 10000) 

    sealed trait PiMessage
    case object Calculate extends PiMessage
    case class Work(start: Int, numberOfElements: Int) extends PiMessage
    case class Result(value: Double) extends PiMessage
    case class ApproximatedPi(pi: Double, duration: Duration) 

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

    class Master(numberOfWorkers: Int, numberOfMessages: Int, numberOfElements: Int, listener: ActorRef) extends Actor {
        var pi: Double = _
        var numberOfResults : Int = _
        val start: Long = System.currentTimeMillis
        val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(numberOfWorkers)), name = "routerForWorkers")
   
        def receive = {
            case Calculate     ⇒ for (i ← 0 until numberOfMessages) workerRouter ! Work(i * numberOfElements, numberOfElements)
            case Result(value) ⇒ pi += value 
                                 numberOfResults += 1
                                 numberOfResults match {
                                    case x if x == numberOfMessages ⇒ listener ! ApproximatedPi(pi, duration = (System.currentTimeMillis - start).millis)
                                                                      context.stop(self)
                                    case _                ⇒ 
                                 }
        }
    }

    class Listener extends Actor {
        def receive = {
            case ApproximatedPi(pi, duration) ⇒ println(s"\n\tPi approximation: $pi, took: $duration ")
                                                 context.system.shutdown()
        }
    }

    def calculate(numberOfWorkers: Int, numberOfElements: Int, numberOfMessages: Int) {
        val system = ActorSystem("PiActorSystem")

        val listener = system.actorOf(Props[Listener], name = "ResultsListener")
        val master   = system.actorOf(Props(new Master(numberOfWorkers, numberOfMessages, numberOfElements, listener)), name = "MasterNode")
        master ! Calculate
    }

}

