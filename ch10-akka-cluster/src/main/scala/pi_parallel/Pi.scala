package pi_parallel

import java.util.concurrent.{Callable, Executors}
import scala.concurrent._
import scala.annotation.tailrec

trait ThreadStrategy {
    def execute[A](f: () ⇒ A) : Function0[A]
}

object SameThreadStrategy extends ThreadStrategy {
    def execute[A](f: () ⇒ A) = f
}

object ThreadPoolStrategy extends ThreadStrategy {
    val tCount = java.lang.Runtime.getRuntime.availableProcessors match {
                    case 1 ⇒ 2 // provide a minimum size to prevent execution dead-lock
                    case x ⇒ x * 3 // triple the pool size as our computations are relatively short!
                 }
    val pool = Executors.newFixedThreadPool(tCount)

    def execute[A](f: () ⇒ A) = {
        val future = pool.submit(new Callable[A]{
            def call : A = { 
            //println(s"thread-ID: ${Thread.currentThread.getId}")
            f() }})
        () ⇒ future.get
    }
}

 
object Pi extends App {

   def calculatePi(start: Int, numberOfElements: Int)(implicit executeStrategy : ThreadStrategy = SameThreadStrategy) : Double = {
        val computations = 
        for (i ← start until (start + numberOfElements - 1))
            yield executeStrategy.execute( () ⇒ 4.0 * (1 - (i % 2) * 2) / (2 * i + 1) )

        computations.aggregate(0.0)( (acc,f) ⇒ f() + acc, _ + _ ) 
    }

    override def main(args: Array[String]) : Unit = {
        val start = System.currentTimeMillis
        val numberOfElements   = 10000
        val numberOfIterations = 10000
        var acc = 0.0
        for(i ← 0 until numberOfIterations) acc += calculatePi(i * numberOfElements, numberOfElements)(ThreadPoolStrategy)
        println(s"\n\tpi approximation: ${acc}, took: ${System.currentTimeMillis - start} millis")
    }
}


