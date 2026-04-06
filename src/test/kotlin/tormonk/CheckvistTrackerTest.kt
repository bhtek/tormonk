package tormonk

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.Date

class CheckvistTrackerTest {
    @Test
    @Disabled
    fun getLastUpdatedTime_simple() {
        FuelManager.instance.client = object : Client {
            override fun executeRequest(request: Request): Response {
                return Response(request.url, 200, body = DefaultBody({ javaClass.getResourceAsStream("/getLastUpdatedTime_simple.json") }))
            }
        }

        val tracker = CheckvistTracker()
        tracker.checkvistService = CheckvistService()
        tracker.checkvistService.afterPropertiesSet()
        val allTasks = tracker.getAllTasks()
        assertThat(allTasks, notNullValue())
        assertThat(tracker.getLastUpdateTime(allTasks!!), equalTo(123L))
    }

    @Test
    fun calculateLastUpdateTime_handlesUnorderedResults() {
        val nextTime = CheckvistTracker.calculateLastUpdateTime(
            100L,
            listOf(
                CheckvistTracker.Companion.EnqueueResult(300L, true),
                CheckvistTracker.Companion.EnqueueResult(200L, true),
                CheckvistTracker.Companion.EnqueueResult(400L, true)
            )
        )

        assertThat(nextTime, equalTo(400L))
    }

    @Test
    fun calculateLastUpdateTime_requiresWholeTimestampGroupToSucceed() {
        val nextTime = CheckvistTracker.calculateLastUpdateTime(
            100L,
            listOf(
                CheckvistTracker.Companion.EnqueueResult(200L, true),
                CheckvistTracker.Companion.EnqueueResult(200L, false),
                CheckvistTracker.Companion.EnqueueResult(300L, true)
            )
        )

        assertThat(nextTime, equalTo(100L))
    }

    @Test
    fun calculateLastUpdateTime_advancesToLastSuccessfulPrefix() {
        val nextTime = CheckvistTracker.calculateLastUpdateTime(
            100L,
            listOf(
                CheckvistTracker.Companion.EnqueueResult(200L, true),
                CheckvistTracker.Companion.EnqueueResult(200L, true),
                CheckvistTracker.Companion.EnqueueResult(300L, false),
                CheckvistTracker.Companion.EnqueueResult(300L, true),
                CheckvistTracker.Companion.EnqueueResult(400L, true)
            )
        )

        assertThat(nextTime, equalTo(200L))
    }

    @Test
    fun processTasks_deletesOnlySuccessfulAddsAndContinuesOnMalformedTasks() {
        val tracker = CheckvistTracker()
        val deletedTaskIds = mutableListOf<Int>()
        val seenLinks = mutableListOf<String>()

        tracker.processTasks(
            JsonArray(
                listOf(
                    task(1, "ok-1", "magnet:?ok", 1),
                    JsonObject(mapOf("id" to 2, "content" to "missing-notes", "status" to 1)),
                    task(3, "bad-exit", "magnet:?fail", 1),
                    task(4, "done", "magnet:?done", 0)
                )
            ),
            sendTorrent = { link ->
                seenLinks += link
                if (link.contains("fail")) 1 else 0
            },
            deleteTask = { taskId -> deletedTaskIds += taskId }
        )

        assertThat(seenLinks, equalTo(listOf("magnet:?ok", "magnet:?fail")))
        assertThat(deletedTaskIds, equalTo(listOf(1)))
    }

    private fun task(id: Int, title: String, link: String, status: Int): JsonObject {
        return JsonObject(
            mapOf(
                "id" to id,
                "content" to title,
                "status" to status,
                "notes" to JsonArray(listOf(JsonObject(mapOf("comment" to link))))
            )
        )
    }

    @Suppress("unused")
    private fun syndEntry(title: String, link: String, publishedAt: Long): SyndEntry {
        return SyndEntryImpl().apply {
            this.title = title
            this.link = link
            this.publishedDate = Date(publishedAt)
        }
    }
}
