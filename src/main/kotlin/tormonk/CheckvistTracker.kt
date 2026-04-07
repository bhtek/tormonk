package tormonk

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result
import com.rometools.rome.feed.synd.SyndEntry
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class CheckvistTracker(
    private val checkvistService: CheckvistService,
) {
    companion object {
        data class EnqueueResult(val timestamp: Long, val success: Boolean)

        private const val specialChecklistId: Long = 569126
        private var specialTaskId: Long? = null
        private var specialNoteId: Long? = null

        private val getTasksUrl = "${CheckvistService.checklistBaseUrl}/${specialChecklistId}/tasks.json"
        private val postTaskUrl = getTasksUrl
        private val postNoteBaseUrl = "${CheckvistService.checklistBaseUrl}/${specialChecklistId}/tasks"
        private val LOG by logger()

        fun calculateLastUpdateTime(previousLastUpdateTime: Long, enqueueResults: List<EnqueueResult>): Long {
            var nextLastUpdateTime = previousLastUpdateTime

            for ((timestamp, group) in enqueueResults.groupBy { it.timestamp }.toSortedMap()) {
                if (timestamp <= previousLastUpdateTime) {
                    continue
                }

                if (group.all { it.success }) {
                    nextLastUpdateTime = timestamp
                    continue
                }

                break
            }

            return nextLastUpdateTime
        }
    }

    class JsonArrayDeserializer : ResponseDeserializable<JsonArray<JsonObject>> {
        @Suppress("UNCHECKED_CAST")
        override fun deserialize(inputStream: InputStream) =
            Parser.default().parse(inputStream) as JsonArray<JsonObject>
    }

    class JsonObjectDeserializer : ResponseDeserializable<JsonObject> {
        override fun deserialize(inputStream: InputStream) = Parser.default().parse(inputStream) as JsonObject
    }

    fun getAllTasks(): JsonArray<JsonObject>? {
        return checkvistService.remote(fun(token): JsonArray<JsonObject>? {
            val (_, _, result) = getTasksUrl.httpGet(listOf("token" to token, "with_notes" to true))
                .responseObject(JsonArrayDeserializer())

            if (result is Result.Failure) {
                LOG.error("Remote service GET [$getTasksUrl] failed.", result.error.exception)
                return null
            }
            return result.get()
        })
    }

    fun getLastUpdateTime(jsonArr: JsonArray<JsonObject>): Long? {
        val lastUploadedJsonObject = jsonArr.filter {
            "Last Uploaded by tormonk" == it.string("content")
        }

        if (lastUploadedJsonObject.isEmpty()) {
            LOG.error("Failed to load proper task object from JSON.")
            return null
        }

        specialTaskId = lastUploadedJsonObject[0].long("id")
        val notesJsonArr = lastUploadedJsonObject[0].array<JsonObject>("notes")

        if (notesJsonArr == null) {
            LOG.error("Failed to find note object array from JSON.")
            return null
        }
        val noteObj = notesJsonArr.getOrNull(0)

        if (noteObj == null) {
            LOG.error("Failed to identify expected note object from JSON.")
            return null
        }

        specialNoteId = noteObj.long("id")
        val commentString = noteObj.string("comment")

        return try {
            commentString?.toLong()
        } catch (e: Exception) {
            LOG.error("Failed to parse comment string [${commentString}].", e)
            null
        }
    }

    fun addTorrentTasks(items: List<SyndEntry>): List<EnqueueResult> {
        return checkvistService.remote { token ->
            items.map { item ->
                val timestamp = item.publishedDate.time
                val (_, _, result) = postTaskUrl.httpPost(listOf("token" to token, "task[content]" to item.title))
                    .responseObject(JsonObjectDeserializer())
                if (result is Result.Failure) {
                    LOG.error("Remote service POST [$getTasksUrl] failed.", result.error.exception)
                    return@map EnqueueResult(timestamp, false)
                }

                val taskJson: JsonObject = result.get()
                val taskId = taskJson.int("id")
                LOG.info("Successfully posted task[${taskId}] for title[${item.title}] at pub date [$timestamp].")

                val (_, _, nResult) = "${postNoteBaseUrl}/${taskId}/comments.json".httpPost(
                    listOf(
                        "token" to token,
                        "comment[comment]" to item.link
                    )
                )
                    .responseString()
                if (nResult is Result.Failure) {
                    LOG.error("Remote service POST for note of taskId[${taskId}] failed.", nResult.error.exception)
                    return@map EnqueueResult(timestamp, false)
                }
                LOG.info("Successfully posted note for task[${taskId}].")
                EnqueueResult(timestamp, true)
            }
        }
    }

    fun setLastUpdateTime(lastUpdateTime: Long) {
        if (specialTaskId == null || specialNoteId == null) {
            LOG.error("specialNoteId is null, need to call getLastUpdateTime() at least once before this method.")
            return
        }

        checkvistService.remote { token ->
            val updateNoteUrl =
                "${CheckvistService.checklistBaseUrl}/${specialChecklistId}/tasks/${specialTaskId}/comments/${specialNoteId}.json"
            val (_, _, result) = updateNoteUrl.httpPut(listOf("token" to token, "comment[comment]" to lastUpdateTime))
                .response()
            if (result is Result.Failure) {
                LOG.error("Remote service PUT failed.", result.error.exception)
            } else {
                LOG.info("Last update time updated to [$lastUpdateTime].")
            }
        }
    }

    fun processTasks(
        allTasks: JsonArray<JsonObject>,
        sendTorrent: (String) -> Int = { link ->
            val process =
                Runtime.getRuntime().exec(arrayOf("transmission-remote", "-n", "transmission:transmission", "-a", link))
            process.waitFor()
        },
        deleteTask: ((Int) -> Unit)? = null
    ) {
        val toTorrentTasks = allTasks
            .filter { task ->
                task.string("content") != "Last Uploaded by tormonk"
                        && task.int("status") == 1
            }

        val successfulTaskIds = mutableListOf<Int>()
        for (toTorrentTask in toTorrentTasks) {
            val notesJsonArr = toTorrentTask.array<JsonObject>("notes")

            if (notesJsonArr == null) {
                LOG.error("Failed to find note object array from JSON.")
                continue
            }

            val noteObj = notesJsonArr.getOrNull(0)

            if (noteObj == null) {
                LOG.error("Failed to identify expected note object from JSON.")
                continue
            }

            val link = noteObj.string("comment")
            if (link == null) {
                LOG.error("Failed to identify expected magnet link from JSON.")
                continue
            }

            val exitCode = sendTorrent(link)

            LOG.info("Sent torrent[${toTorrentTask.string("content")}] w/ magnet[$link] to home w/ exit code of $exitCode.")
            if (exitCode == 0) {
                val taskId = toTorrentTask.int("id")
                if (taskId != null) {
                    successfulTaskIds += taskId
                }
            } else {
                LOG.error("Transmission rejected torrent[${toTorrentTask.string("content")}], keeping task for retry.")
            }
        }

        if (deleteTask != null) {
            successfulTaskIds.forEach(deleteTask)
            return
        }

        checkvistService.remote { token ->
            for (taskId in successfulTaskIds) {
                val (_, _, result) = "${CheckvistService.checklistBaseUrl}/${specialChecklistId}/tasks/${taskId}.json".httpDelete(
                    listOf("token" to token)
                )
                    .response()
                if (result is Result.Failure) {
                    LOG.error("Failed to delete task for taskId[$taskId].", result.error.exception)
                }
            }
        }
    }
}
