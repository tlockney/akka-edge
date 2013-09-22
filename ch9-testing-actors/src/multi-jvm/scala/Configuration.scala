
trait Configuration {
    val default = "Nothing"
    // System.getProperty is weird...but that's in the Java world...
    def getPropertyOrElse(prop: String, default: ⇒ String = default) = System.getProperty(prop) match { case null ⇒ default; case x ⇒ x }
    def getCustomMessageForNode = getPropertyOrElse("custom_message")
    def getThreadPoolName       = getPropertyOrElse("custom_pool_name")
    def getThreadPoolType       = Class.forName(getThreadPoolName)
}

object Configuration {
    def apply = new Configuration {}
}

