package tormonk

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.springframework.util.StringUtils
import java.text.SimpleDateFormat

class Channel {
    var title by JXML / "channel" / "title" / XText
    var items by JXML / "channel" / XElements("item") / XSub(Item::class.java)

    fun initAfterXmlLoaded() {
        items?.forEach { item -> item.initAfterXmlLoaded() }
    }
}

class Item {
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