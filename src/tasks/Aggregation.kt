package tasks

import contributors.User

/*
TODO: Write aggregation code.

 In the initial list each user is present several times, once for each
 repository he or she contributed to.
 Merge duplications: each user should be present only once in the resulting list
 with the total value of contributions for all the repositories.
 Users should be sorted in a descending order by their contributions.

 The corresponding test can be found in test/tasks/AggregationKtTest.kt.
 You can use 'Navigate | Test' menu action (note the shortcut) to navigate to the test.
*/
fun List<User>.aggregate(): List<User> = groupBy { it.login }
    .map { (k, v) -> User(k, v.sumOf { it.contributions }) }
    .sortedByDescending { it.contributions }

fun List<User>.aggregateBySequence(): List<User> = this.asSequence().groupBy { it.login }
    .map { (k, v) -> User(k, v.sumOf { it.contributions }) }
    .sortedByDescending { it.contributions }


/**
 * 性能比aggregate好一些,千万次快500ms± 因为groupingBy返回的Grouping只是
 */
fun List<User>.aggregateFromGrouping(): List<User> = groupingBy { it.login }
    .aggregate<User, String, Int> { _, accumulator, element, _ ->
        element.contributions + (accumulator ?: 0)
    }
    .map { (k, v) -> User(k, v) }
    .sortedByDescending { it.contributions }
