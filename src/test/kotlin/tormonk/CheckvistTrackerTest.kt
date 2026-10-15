package tormonk

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CheckvistTrackerTest {
    @Test
    fun getLastUpdatedTime_simple() {
        val tracker = CheckvistTracker(CheckvistService())
        val allTasks = checkNotNull(javaClass.getResourceAsStream("/getLastUpdatedTime_simple.json")).use {
            CheckvistTracker.JsonArrayDeserializer().deserialize(it)
        }

        assertEquals(123L, tracker.getLastUpdateTime(allTasks))
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

        assertEquals(400L, nextTime)
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

        assertEquals(100L, nextTime)
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

        assertEquals(200L, nextTime)
    }

    @Test
    fun processTasks_deletesOnlySuccessfulAddsAndContinuesOnMalformedTasks() {
        val tracker = CheckvistTracker(CheckvistService())
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

        assertEquals(listOf("magnet:?ok", "magnet:?fail"), seenLinks)
        assertEquals(listOf(1), deletedTaskIds)
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
}
