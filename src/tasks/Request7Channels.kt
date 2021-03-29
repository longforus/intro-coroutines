package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import java.util.concurrent.atomic.AtomicInteger

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        var allUser = listOf<User>()
        val channel = Channel<List<User>>()
        val requestCount = AtomicInteger(0)
        repos.map { repo ->
            launch{
                log("starting loading for ${repo.name}")
              val users =   service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                allUser = (allUser+users).aggregate()
                requestCount.incrementAndGet()
                channel.send(allUser)
            }
        }

        repeat(repos.size){
            updateResults(channel.receive(),it==repos.size)
        }

    }
}
