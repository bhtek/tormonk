package tormonk

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import javax.annotation.Resource

@SpringBootApplication
@ComponentScan("tormonk")
@EnableScheduling
@PropertySource("classpath:/config.local.properties", ignoreResourceNotFound = true)
open class TorMonkApplication {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val app = SpringApplication.run(TorMonkApplication::class.java, *args)
            val tjc: TorrentJobContainer = app.getBean(TorrentJobContainer::class.java)
            tjc.checkForTorrents()
        }
    }
}

@Component
class TorrentJobContainer {
    companion object {
        val LOG = LoggerFactory.getLogger(tormonk.TorrentJobContainer::class.java.name)
    }

    @Resource lateinit var checkvistTracker: CheckvistTracker
    @Resource lateinit var showRss: ShowRss

    @Scheduled(cron = "0 2,11,23,33,42,52 * * * *")
    fun checkForTorrents() {
        LOG.info("Begin check.")

        val allTasks = checkvistTracker.getAllTasks() ?: return
        val lastUpdateTime = checkvistTracker.getLastUpdateTime(allTasks) ?: return
        checkvistTracker.processTasks(allTasks)

        val channel = showRss.getNewItems(lastUpdateTime)
        checkvistTracker.addTorrentTasks(channel.items!!)

        if (channel.items!!.size > 0) {
            checkvistTracker.setLastUpdateTime(channel.items!![0].pubDate!!.millis)
        }
    }
}

@Controller
open class TorController {
    @Autowired lateinit var tjc: tormonk.TorrentJobContainer

    @RequestMapping("/track", method = arrayOf(RequestMethod.POST))
    @ResponseStatus(value = HttpStatus.OK)
    open fun trackNow() {
        tjc.checkForTorrents()
    }
}
