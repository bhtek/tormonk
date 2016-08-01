package tormonk

import java.util.*

class CheckvistService(checklistId: Long) {
    companion object {
        val baseUrl = "https://checkvist.com"
        val checklistBaseUrl = "${CheckvistService.baseUrl}/checklists"
        val tokenExpiry: Long = 1000 * 60 * 60 * 23
    }

    private var lastActivity: Long = 0
        set(value) {
            if (value > field)
                field = value
        }

    private var token: String = login()

    fun login(): String {
        return "7P35qpoIURdwak6eT0oGHKZk9G9Pay"
    }

    fun <T> remote(body: (String) -> T): T {
        if (System.currentTimeMillis() - lastActivity > tokenExpiry) {
            token = login()
        }

        return body(token)
    }

    fun getTasks(): List<CheckvistTask> {
        return ArrayList()
    }
}

data class CheckvistTask(val id: String, val content: String, val status: Short, val notes: List<CheckvistNote>)

data class CheckvistNote(val id: String, val comment: String)