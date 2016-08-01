package tormonk

import com.beust.klaxon.*
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.InputStream
import java.lang.Long.parseLong

@Component
class CheckvistTracker {
    companion object {
        val specialChecklistId: Long = 569126
        val checkvistService = CheckvistService(specialChecklistId)
        val getTasksUrl = "${CheckvistService.checklistBaseUrl}/${specialChecklistId}/tasks.json"
        val LOG = LoggerFactory.getLogger(CheckvistTracker::class.java.name)
    }

    class Deserializer : ResponseDeserializable<JsonArray<JsonObject>> {
        override fun deserialize(inputStream: InputStream) = Parser().parse(inputStream) as JsonArray<JsonObject>
    }

    fun getLastUpdateTime(): Long? {
        return checkvistService?.remote<Long?>(fun(token): Long? {
            println("Using token: ${token}")
            val (request, response, result) = getTasksUrl.httpGet(listOf("token" to token, "with_notes" to true)).responseObject(Deserializer())

            if (result is Result.Failure) {
                LOG.error("Remote service [$getTasksUrl] failed.", result.error.exception)
                return null
            }
            val jsonArr: JsonArray<JsonObject> = result.get()

            val lastUploadedJsonObject = (jsonArr as JsonArray<JsonObject>).filter {
                "Last Uploaded by tormonk".equals(it.string("content"))
            }

            if (lastUploadedJsonObject.size < 1) {
                LOG.error("Failed to load proper task object from JSON.")
                return null
            }
            val notesJsonArr = lastUploadedJsonObject[0].array<JsonObject>("notes")

            if (notesJsonArr == null) {
                LOG.error("Failed to find note object arrau from JSON.")
                return null
            }
            val noteObj = notesJsonArr[0]?.obj("note")

            if (noteObj == null) {
                LOG.error("Failed to identify expected note object from JSON.")
                return null
            }

            val commentString = noteObj.string("comment")

            try {
                return parseLong(commentString)
            } catch (e: Exception) {
                LOG.error("Failed to parse comment string [${commentString}].", e)
                return null
            }
        })
    }
}
