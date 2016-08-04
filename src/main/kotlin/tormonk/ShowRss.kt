package tormonk

import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

class Channel {
    var title by JXML / "channel" / "title" / XText
    var items by JXML / "channel" / XElements("item") / XSub(Item::class.java)
}

class Item {
    var title by JXML / "title" / XText
}