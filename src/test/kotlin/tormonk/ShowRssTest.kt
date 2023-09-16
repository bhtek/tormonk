package tormonk

import org.junit.jupiter.api.Test

class ShowRssTest {
    @Test
    fun `pub date parsing`() {
        RssItem.PUB_DATE_FORMAT.parseDateTime("Wed, 30 Aug 2023 08:20:41 +0000")
        RssItem.PUB_DATE_FORMAT.parseDateTime("Thu, 31 Aug 2023 08:20:41 +0000")
        RssItem.PUB_DATE_FORMAT.parseDateTime("Fri, 1 Sep 2023 08:20:41 +0000")
        RssItem.PUB_DATE_FORMAT.parseDateTime("Mon, 11 Sep 2023 10:15:34 +0000")
        RssItem.PUB_DATE_FORMAT.parseDateTime("Tue, 12 Sep 2023 10:15:34 +0000")
    }
}
