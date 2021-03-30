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
        repos.map { repo ->
            launch{
                log("starting loading for ${repo.name}")
                channel.send(service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList())
            }
        }

        repeat(repos.size){
            allUser = (allUser+channel.receive()).aggregate()
            updateResults(allUser,it==repos.size)
        }

    }
}
