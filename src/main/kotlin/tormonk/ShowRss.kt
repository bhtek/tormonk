package tormonk

import org.jdom2.JDOMFactory
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaderSAX2Factory
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

class Channel {
    var title by JXML / "channel" / "title" / XText
}