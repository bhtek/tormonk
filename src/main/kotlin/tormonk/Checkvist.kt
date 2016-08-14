package tormonk

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class CheckvistService {
    companion object {
        val LOG = LoggerFactory.getLogger(CheckvistService::class.java)
        val baseUrl = "https://checkvist.com"
        val checklistBaseUrl = "${baseUrl}/checklists"
        // e.g. https://checkvist.com/auth/refresh_token.json?old_token=jX0MvhozWsWEsj&with_notes=true
        val refreshTokenBaseUrl = "${baseUrl}/auth/refresh_token.json"
        val tokenExpiry: Long = 1000 * 60 * 60 * 23
        val tokenFile = File("${System.getProperty("user.home")}/.tormonk.token")
    }

    private var lastActivity: Long = 0
        set(value) {
            if (value > field)
                field = value
        }

    private var token: String

    init {
        if (!tokenFile.exists()) {
            throw RuntimeException("Expected an existing token but found none.")
        }

        lastActivity = tokenFile.lastModified()
        token = tokenFile.readText().trim()
    }

    fun refreshToken(): String {
        val (request, response, result) = "${refreshTokenBaseUrl}".httpGet(Arrays.asList("old_token" to token)).responseString()
        if (result is Result.Failure) {
            throw RuntimeException("Refresh token failed.", result.error)
        }

        val token = result.get().replace(Regex("\""), "")
        LOG.info("Successfully retrieved refreshed token[${token}].")

        return token
    }

    fun <T> remote(body: (String) -> T): T {
        if (System.currentTimeMillis() - lastActivity > tokenExpiry) {
            token = refreshToken()
            tokenFile.writeText(token)
        }

        try {
            return body(token)
        } finally {
            lastActivity = System.currentTimeMillis()
        }
    }
}