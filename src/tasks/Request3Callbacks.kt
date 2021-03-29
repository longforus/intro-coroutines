package tasks

import contributors.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun loadContributorsCallbacks(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    service.getOrgReposCall(req.org).onResponse { responseRepos ->
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList().toMutableList()
//        val allUsers = mutableListOf<User>()
//        while (repos.isNotEmpty()) {
//           val repo = repos.removeFirst()
//            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
//                logUsers(repo, responseUsers)
//                val users = responseUsers.bodyList()
//                allUsers += users
//                if (repos.isEmpty()) {//这么做的话会导致多个请求返回时,repos已经空了,导致updateResults多次调用
//                    updateResults(allUsers.aggregate())
//                }
//            }
//        }

//        val allUsers = mutableListOf<User>()
//        for (repo in repos.withIndex()) {
//            service.getRepoContributorsCall(req.org, repo.value.name).onResponse { responseUsers ->
//                logUsers(repo.value, responseUsers)
//                val users = responseUsers.bodyList()
//                allUsers += users
//                if (repo.index == repos.lastIndex) {
//                    //這样就不会造成多次调用了,因为repo是final的,一个repo对应一个onResponse回调,这里只会执行一次
//                    // 仍然存在的问题是,不能保证最后一个回调的时候,前面所有的请求都返回了,這样判断的话,可能会丢掉前面的一些请求的结果
//                    updateResults(allUsers.aggregate())
//                }
//            }
//        }

//        val allUsers = mutableListOf<User>()
//        var requestCount = 0
//        println("repos size = ${repos.size}")
//        for (repo in repos) {
//            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
//                logUsers(repo, responseUsers)
//                val users = responseUsers.bodyList()
//                allUsers += users
//                requestCount++
//                println("requestCount = $requestCount")
//                if (requestCount == repos.size) {
//                    //這样的话requestCount和allUsers都不是线程安全的,如果当前回调不全是从同一个线程回调的话,
//                        // 就会导致并发读写从而可能导致数据丢失,造成上面的if不成立,updateResults永远不会被调用,或者是allUsers的数据不完整
//                    updateResults(allUsers.aggregate())
//                }
//            }
//        }

        val allUsers = Collections.synchronizedList(mutableListOf<User>())
        val requestCount = AtomicInteger(0)
        for (repo in repos) {
            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
                logUsers(repo, responseUsers)
                val users = responseUsers.bodyList()
                allUsers += users
                if (requestCount.incrementAndGet() == repos.size) {
                    //需要使用原子变量,而且allUsers也需要使用同步容器,才能保证没有结果会丢失
                    updateResults(allUsers.aggregate())
                }
            }
        }

//        // TODO: Why this code doesn't work? How to fix that?
//        updateResults(allUsers.aggregate())
    }
}

inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
