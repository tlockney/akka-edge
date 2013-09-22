import org.scalatest._
import org.scalatest.matchers.MustMatchers._

object SimpleModuleMultiJvmNode2 extends Configuration with FlatSpecLike with MustMatchers {

   def main(args: Array[String]) : Unit = {
        val p = getCustomMessageForNode
        val pn = getThreadPoolName
        pn must include ("ThreadPoolExecutor")
    }

}
