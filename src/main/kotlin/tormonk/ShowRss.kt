package tormonk

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.jdom2.input.SAXBuilder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.io.InputStream

@Component
class ShowRss {
    companion object {
        val SHOWRSS_URL = "http://showrss.info/user/72839.rss?magnets=true&namespaces=false&name=null&quality=null&re=null"
        val LOG = LoggerFactory.getLogger(ShowRss::class.java)
    }

    class RssDeserializer : ResponseDeserializable<RssChannel> {
        override fun deserialize(inputStream: InputStream): RssChannel {
            val document = SAXBuilder().build(inputStream)
            val channel = JDOM.load(document.rootElement, RssChannel::class.java)
            channel.initAfterXmlLoaded()

            return channel
        }
    }

    fun getNewItems(lastUpdatedTime: Long): RssChannel {
        val (req, res, result) = SHOWRSS_URL.httpGet().responseObject(RssDeserializer())
        if (result is Result.Failure) {
            throw RuntimeException("Failed to retrieve from showRss using ${SHOWRSS_URL}.", result.error.exception)
        }

        val rssChannel = result.get()
        rssChannel.items = rssChannel.items?.filter { item -> (item.pubDate?.isAfter(lastUpdatedTime) ?: false) }
        rssChannel.items = rssChannel.items ?: emptyList()

        return rssChannel
    }
}

class RssChannel {
    var title by JXML / "channel" / "title" / XText
    var items by JXML / "channel" / XElements("item") / XSub(RssItem::class.java)

    fun initAfterXmlLoaded() {
        items?.forEach { item -> item.initAfterXmlLoaded() }
    }
}

class RssItem {
    companion object {
        val PUB_DATE_FORMAT = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss Z")
    }

    var title by JXML / "title" / XText
    var link by JXML / "link" / XText
    private var pubDateStr by JXML / "pubDate" / XText

    var pubDate: DateTime? = null

    fun initAfterXmlLoaded() {
        if (!StringUtils.isEmpty(pubDateStr)) {
            pubDate = PUB_DATE_FORMAT.parseDateTime(pubDateStr)
        }
    }
}