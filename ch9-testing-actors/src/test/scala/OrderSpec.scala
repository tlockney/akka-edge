import org.scalatest.FlatSpec

class OrderSpec extends FlatSpec {
    
    behavior of "An default Order i.e. no orders have been added" 

    private[this] val order = Order()

    it should "have zero orders" in {
        assert(order.numOfOrders === 0)
    }

    it should "return 0 when tally is invoked" in {
        assert(order.tally === 0) 
    }

    behavior of "An order when > 1 order(s) have been added"

    it should "have exactly 2 orders when 2 order are added" in {
        order.add(1)
        assert(order.top === 1)
        order.add(2)
        assert(order.numOfOrders === 2)
    }

    it should "return 13 when tally is invoked for orders of values {1,2,5,5}" in {
        order.add(5)
        assert(order.top === 5)
        order.add(5)
        assert(order.tally === 13)
    }

}

