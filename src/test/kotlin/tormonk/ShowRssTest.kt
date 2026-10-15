package tormonk

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URI
import java.time.Instant

class ShowRssTest {
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    private val showRss = ShowRss(URI.create("http://127.0.0.1:${server.address.port}/feed"))

    @AfterEach
    fun stopServer() {
        server.stop(0)
    }

    @Test
    fun filtersStrictlyAfterCursorAndSortsOldestFirst() {
        respond(200, checkNotNull(javaClass.getResourceAsStream("/showrss_test.rss")).use { it.readBytes() })

        val items = showRss.getNewItems(Instant.parse("2016-07-30T06:20:04Z").toEpochMilli())

        assertEquals(
            listOf(
                "Real Time With Bill Maher 2016 07 21 HDTV x264 BATV",
                "Real Time With Bill Maher 2016 07 15 HDTV x264 BATV",
                "Last Week Tonight With John Oliver S03E19 HDTV x264 BATV"
            ),
            items.map { it.title }
        )
        assertEquals(
            "magnet:?xt=urn:btih:48BE5DF9798E2BE865639353D3BEFAAC4ECA2923&dn=Real+Time+With+Bill+Maher+2016+07+21+HDTV+x264+BATV&tr=udp://tracker.coppersurfer.tk:6969/announce&tr=udp://tracker.leechers-paradise.org:6969&tr=udp://open.demonii.com:1337",
            items.first().link
        )
    }

    @Test
    fun returnsNoItemsWhenCursorIsAtNewestEntry() {
        respond(200, checkNotNull(javaClass.getResourceAsStream("/showrss_test.rss")).use { it.readBytes() })

        assertEquals(emptyList<Any>(), showRss.getNewItems(Instant.parse("2016-08-01T04:20:04Z").toEpochMilli()))
    }

    @Test
    fun rejectsHttpErrorsEvenWhenBodyContainsAValidFeed() {
        respond(503, checkNotNull(javaClass.getResourceAsStream("/showrss_test.rss")).use { it.readBytes() })

        assertThrows(IOException::class.java) { showRss.getNewItems(0) }
    }

    private fun respond(status: Int, body: ByteArray) {
        server.createContext("/feed") { exchange ->
            exchange.responseHeaders.add("Content-Type", "application/rss+xml; charset=UTF-8")
            exchange.sendResponseHeaders(status, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
    }
}
