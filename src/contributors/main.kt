package contributors

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

//fun main() {
//    setDefaultFontSize(18f)
//    ContributorsUI().apply {
//        pack()
//        setLocationRelativeTo(null)
//        isVisible = true
//    }
//}



//fun main() = runBlocking {
//    val job = launch {
//        try {
//            repeat(1000) { i ->
//                println("job: I'm sleeping $i ...")
//                delay(500L)
//            }
//        }catch (e:Exception){
//            println(e.message)
//        }
//        finally {
//            println("job: I'm running finally")
//        }
//    }
//    delay(1300L) // delay a bit
//    println("main: I'm tired of waiting!")
//    /**
//     * job: I'm sleeping 0 ...
//    job: I'm sleeping 1 ...
//    job: I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    main: Now I can quit.
//    StandaloneCoroutine was cancelled
//    job: I'm running finally
//
//     */
//   // job.cancel() //只调用cancel而不join等待的话,不能保证后续的代码运行在job这个协程块之后
//    /**
//     * job: I'm sleeping 0 ...
//    job: I'm sleeping 1 ...
//    job: I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    StandaloneCoroutine was cancelled
//    job: I'm running finally
//    main: Now I can quit.
//     */
////    job.cancelAndJoin() // 调用这个的话是在job协程块完全执行完之后才执行后续的代码
//    job.cancel() // cancels and join ==  job.cancelAndJoin()
//    job.join()
//    println("main: Now I can quit.")
//}


fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")




fun CoroutineScope.switchMapDeferreds(input: ReceiveChannel<Deferred<String>>) = produce<String> {
    var current = input.receive() // start with first received deferred value
    while (isActive) { // loop while not cancelled/closed
        val next = select<Deferred<String>?> { // return next deferred value from this select or null
            input.onReceiveOrNull { update ->
                update // replaces next value to wait
            }
            current.onAwait { value ->
                send(value) // send value that current deferred has produced
                input.receiveOrNull() // and use the next deferred from the input channel
            }
        }
        if (next == null) {
            println("Channel was closed")
            break // out of loop
        } else {
            current = next
        }
    }
}

fun CoroutineScope.asyncString(str: String, time: Long) = async {
    delay(time)
    str
}

fun main() = runBlocking<Unit> {
    val chan = Channel<Deferred<String>>() // the channel for test
    launch { // launch printing coroutine
        for (s in switchMapDeferreds(chan))
            println(s) // print each received string
    }
    chan.send(asyncString("BEGIN", 100))
    delay(200) // enough time for "BEGIN" to be produced
    chan.send(asyncString("Slow", 500))
    delay(100) // not enough time to produce slow
    chan.send(asyncString("Replace", 100))
    delay(500) // give it time before the last one
    chan.send(asyncString("END", 500))
    delay(1000) // give it time to process
    chan.close() // close the channel ...
    delay(500) // and wait some time to let it finish
}
//fun main() = runBlocking {
//    try {
//        supervisorScope {
//            val child = launch {
//                try {
//                    println("The child is sleeping")
//                    delay(Long.MAX_VALUE)
//                } finally {
//                    println("The child is cancelled")
//                }
//            }
//            // Give our child a chance to execute and print using yield
//            yield()
//            println("Throwing an exception from the scope")
//            throw AssertionError()
//        }
//    } catch(e: AssertionError) {
//        println("Caught an assertion error")
//    }
//}



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