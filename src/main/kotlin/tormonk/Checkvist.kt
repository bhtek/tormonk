package tormonk

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.io.File

@Component
class CheckvistService : InitializingBean {
    private val LOG = KotlinLogging.logger {}

    companion object {
        private const val baseUrl = "https://checkvist.com"
        val checklistBaseUrl = "$baseUrl/checklists"
        // e.g. https://checkvist.com/auth/refresh_token.json?old_token=jX0MvhozWsWEsj&with_notes=true
        private val refreshTokenBaseUrl = "$baseUrl/auth/refresh_token.json"
        private const val tokenExpiryMillis: Long = 1000 * 60 * 60 * 23
        private val tokenFile = File("${System.getProperty("user.home")}/.tormonk.token")
    }

    override fun afterPropertiesSet() {
        if (!tokenFile.exists()) {
            throw RuntimeException("Expected an existing token but found none.")
        }
        token = tokenFile.readText().trim()
    }

    private lateinit var token: String


    fun refreshToken(): String {
        val (_, _, result) = refreshTokenBaseUrl.httpGet(listOf("old_token" to token)).responseString()
        if (result is Result.Failure) {
            throw RuntimeException("Refresh token failed.", result.error)
        }

        val token = result.get().replace(Regex("\""), "")
        LOG.info { "Successfully retrieved refreshed token[$token]." }

        return token
    }

    fun <T> remote(body: (String) -> T): T {
        if (System.currentTimeMillis() - tokenFile.lastModified() > tokenExpiryMillis) {
            token = refreshToken()
            tokenFile.writeText(token)
        }

        return body(token)
    }
}
