package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
//    val repos = service
//        .getOrgRepos(req.org)
//        .also { logRepos(req, it) }
//        .body() ?: listOf()
//
//    //这么做的话就不是并发的了
//    val allUsers = Collections.synchronizedList(mutableListOf<User>())
//    val requestCount = AtomicInteger(0)
//    for (repo in repos) {
//        val users =  service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
//        allUsers += users
//        //需要使用原子变量,而且allUsers也需要使用同步容器,才能保证没有结果会丢失
//        updateResults(allUsers.aggregate(),requestCount.incrementAndGet() == repos.size)
//    }


//    val repos = service
//        .getOrgRepos(req.org)
//        .also { logRepos(req, it) }
//        .body() ?: listOf()
//
//
//    val allUsers = Collections.synchronizedList(mutableListOf<User>())
//    val requestCount = AtomicInteger(0)
//    coroutineScope {
//        for (repo in repos) {
//            launch {
//                log("starting loading for ${repo.name}")
//                val users =  service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
//                allUsers += users
//                //需要使用原子变量,而且allUsers也需要使用同步容器,才能保证没有结果会丢失
//                updateResults(allUsers.aggregate(),requestCount.incrementAndGet() == repos.size)
//            }
//        }
//    }

//
//    updateResults(coroutineScope {
//        val repos = service
//            .getOrgRepos(req.org)
//            .also { logRepos(req, it) }
//            .body() ?: listOf()
//
//
//        return@coroutineScope repos.map { repo ->
//            async {
//                log("starting loading for ${repo.name}")
//                val list = service.getRepoContributors(req.org, repo.name)
//                    .also { logUsers(repo, it) }
//                    .bodyList()
//                list
//            }
//        }.awaitAll().flatten().aggregate()
//
//    },true)

    //官方实现
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

//    val allUser = mutableListOf<User>()
//    //因为不做并发所以不用管同步和加锁
//    repos.forEachIndexed { index, repo ->
//        allUser+=service.getRepoContributors(req.org, repo.name)
//            .also { logUsers(repo, it) }
//            .bodyList()
//        updateResults(allUser.aggregate(),index==repos.indices.last)
//    }

    var allUser = listOf<User>()
    //因为不做并发所以不用管同步和加锁
    repos.forEachIndexed { index, repo ->
        allUser = (allUser + service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()).aggregate()
        //这里比前一段的好在对结果aggregate后会存起来,第二次aggregate的时候,不用每次都遍历累加所有的结果,前面处理过的二次就不用再处理了
        updateResults(allUser, index == repos.lastIndex)
    }


}