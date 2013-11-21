package pi_sequential
import scala.annotation.tailrec

object Pi extends App {

    def calculatePi(start: Int, numberOfElements: Int) : Double = {
        @tailrec
        def calculatePiFor(start: Int, limit: Int, acc: Double) : Double =
           start match {
                case x if x == limit ⇒ acc
                case _               ⇒ calculatePiFor(start + 1, limit, acc + 4.0 * (1 - (start % 2) * 2) / (2 * start + 1))
            }   
        
        calculatePiFor(start, start + numberOfElements - 1, 0.0)
    }

    override def main(args: Array[String]) : Unit = {
        val start = System.currentTimeMillis
        val numberOfIterations = 10000
        val numberOfElements   = 10000
        var acc = 0.0
        for(i ← 0 until numberOfIterations) 
            acc += calculatePi(i * numberOfElements, numberOfElements)

        println(s"\n\tpi approximation: ${acc}, took: ${System.currentTimeMillis - start} millis")
    }
}


