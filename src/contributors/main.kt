package contributors

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    setDefaultFontSize(18f)
    ContributorsUI().apply {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }
}


/**
 * [main @coroutine#2 90 ] suspend A1
[main @coroutine#3 98 ] suspend B1
[main @coroutine#4 99 ] repeat 0
[main @coroutine#4 100 ] consume A1
[main @coroutine#2 106 ] resume A1  suspend A2
[main @coroutine#4 204 ] repeat 1
[main @coroutine#4 204 ] consume B1
[main @coroutine#3 204 ] resume B1
[main @coroutine#4 308 ] repeat 2
[main @coroutine#4 308 ] consume A2
[main @coroutine#2 309 ] resume A2
 */
//var startTime = System.currentTimeMillis()
//
//fun main() = runBlocking<Unit> {
//    val channel = Channel<String>()
//    launch {
//        log1("suspend A1")
//        channel.send("A1")//挂起
//        log1("resume A1  suspend A2")
//        channel.send("A2")//因为B1在这之前挂起,所以会先消费B1
//        log1("resume A2")
//    }
//    launch {
//        log1("suspend B1")
//        channel.send("B1")//因为是2个协程 所以和A1都会挂起
//        log1("resume B1")
//    }
//    launch {
//        repeat(3) {
//            log1("repeat $it")
//            val x = channel.receive()
////            delay(100)
//            log1("consume $x")
//            delay(100)
//        }
//    }
//}
//
//fun log1(message: Any?) {
//    println("[${Thread.currentThread().name} ${System.currentTimeMillis()-startTime} ] $message")
//}