package tormonk

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class ShowRss(private val feedUri: URI = URI.create(SHOWRSS_URL)) {
    companion object {
        private const val SHOWRSS_URL =
            "https://showrss.info/user/72839.rss?magnets=true&namespaces=false&name=null&quality=null&re=null"
        private val CLIENT = HttpClient.newHttpClient()
    }

    fun getNewItems(lastUpdatedTime: Long): List<SyndEntry> {
        val request = HttpRequest.newBuilder(feedUri).GET().build()
        val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream())
        val feed = response.body().use { stream ->
            if (response.statusCode() !in 200..299) {
                throw IOException("showRSS request failed with HTTP ${response.statusCode()}.")
            }
            SyndFeedInput().build(XmlReader(stream))
        }

        return feed.entries
            .filter { entry ->
                entry.publishedDate.time > lastUpdatedTime
            }
            .sortedBy { entry -> entry.publishedDate.time }
    }
}
