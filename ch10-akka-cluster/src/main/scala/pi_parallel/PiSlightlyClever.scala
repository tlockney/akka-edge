package pi_parallel

import java.util.concurrent.{Callable, Executors}
import scala.concurrent._
import scala.annotation.tailrec

object PiSlightlyClever extends App {

    def calculatePiSlightlyClever(start: Int, end : Int, step : Double)(implicit executeStrategy : ThreadStrategy = SameThreadStrategy) : Double = {
        val computations = 
        for( i ← start until end )
            yield executeStrategy.execute{ () ⇒ val x = (i + 0.5) * step; 4.0 / (1.0 + (x * x)) }
        computations.aggregate(0.0)( (acc, f) ⇒ f() + acc, _ + _ )
    }

    override def main(args: Array[String]) : Unit = {
        val start = System.currentTimeMillis
        val numberOfBins = 1048576 // slightly over 1 million bins
        val step = 1.0/numberOfBins
        var acc = 0.0
        val numberOfTasks = 2 // task:cpu-core ratio = 1:1
        val dataPartition = numberOfBins / numberOfTasks
        for( i ← 0 until numberOfTasks) acc += calculatePiSlightlyClever(i * dataPartition, (i + 1) * dataPartition, step)(ThreadPoolStrategy)
        println(s"\n\tpi approximation: ${acc * step}, took: ${System.currentTimeMillis - start} millis")
    }
}


