package tormonk

import com.beust.klaxon.*
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class CheckvistTracker {
    companion object {
        val specialChecklistId: Long = 569126
        val checkvistService = CheckvistService(specialChecklistId)
        val getTasksUrl = "${CheckvistService.checklistBaseUrl}/${specialChecklistId}/tasks.json"
    }

    class Deserializer : ResponseDeserializable<JsonArray<JsonObject>> {
        override fun deserialize(inputStream: InputStream) = Parser().parse(inputStream) as JsonArray<JsonObject>
    }

    fun getLastUpdateTime(): Long? {
        val lastUpdateTime = checkvistService?.remote<Long?>(fun(token): Long? {
            println("Using token: ${token}")
            val (request, response, result) = getTasksUrl.httpGet(listOf("token" to token, "with_notes" to true)).responseObject(Deserializer())

            //do something with response
            var jsonArr: JsonArray<JsonObject>? = null
            when (result) {
                is Result.Failure -> {
                    println("Got error: " + result.error)
                }
                is Result.Success -> {
                    jsonArr = result.value
                }
            }

            if (jsonArr != null) {
                val lastUploadedJsonObject = (jsonArr as JsonArray<JsonObject>).filter {
                    "Last Uploaded by tormonk".equals(it.string("content"))
                }

                val notesJsonArr = lastUploadedJsonObject[0].array<JsonObject>("notes")
                if (notesJsonArr != null) {
                    val noteObj = notesJsonArr[0]?.obj("note")
                    print("WHAT WE WANT[" + noteObj?.string("comment") + "]")

                    // TODO Next, to change to reading the notes...
                    lastUploadedJsonObject[0].long("checklist_id") ?: -1
                } else {
                    return null
                }
            } else {
                return null
            }

            return null
        })

        return lastUpdateTime
    }
}
