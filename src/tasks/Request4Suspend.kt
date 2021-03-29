package tasks

import contributors.*

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
//    return try {
//        service.getOrgRepos(req.org).bodyList().flatMap {
//            service.getRepoContributors(req.org,it.name).bodyList()
//        }.aggregate()
//    } catch (e: Exception) {
//        e.printStackTrace()
//        emptyList()
//    }


    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    return repos.flatMap { repo ->
        service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}