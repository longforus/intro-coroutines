package tasks

import contributors.User
import org.junit.Assert
import org.junit.Test

class AggregationKtTest {


    val expected = listOf(
        User("Bob", 10),
        User("Alice", 8),
        User("Charlie", 3)
    )

    val list = listOf(
        User("Alice", 1), User("Bob", 3),
        User("Alice", 2), User("Bob", 7),
        User("Charlie", 3), User("Alice", 5)
    )


    @Test
    fun testAggregation() {
        var actual:List<User>? = null

        println("start groupingBy")
        val s2 = System.currentTimeMillis()
        repeat(10000000){
            actual = list.aggregateFromGrouping()
        }
        println("end groupingBy ${System.currentTimeMillis()-s2}ms")
        Assert.assertEquals("Wrong result for 'aggregateFromGrouping'", expected, actual)


        println("start groupBy")
        val s1 = System.currentTimeMillis()
        repeat(10000000){
            actual = list.aggregate()
        }
        println("end groupBy ${System.currentTimeMillis()-s1}ms")
        Assert.assertEquals("Wrong result for 'aggregation'", expected, actual)
    }
}