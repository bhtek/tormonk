package tormonk

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus

@SpringBootApplication
@EnableScheduling
@PropertySource("classpath:/config.local.properties", ignoreResourceNotFound = true)
open class TorMonkApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication.run(TorMonkApplication::class.java, *args)
            val tjc: TorrentJobContainer = app.getBean(TorrentJobContainer::class.java)
            tjc.checkForTorrents()
        }
    }
}

@Component
class TorrentJobContainer(
    private val checkvistTracker: CheckvistTracker,
    private val showRss: ShowRss,
) {
    companion object {
        val LOG = LoggerFactory.getLogger(TorrentJobContainer::class.java.name)!!
    }

    @Scheduled(cron = "0 2,11,23,33,42,52 * * * *")
    fun checkForTorrents() {
        LOG.info("Check start.")

        val allTasks = checkvistTracker.getAllTasks() ?: return
        val lastUpdateTime = checkvistTracker.getLastUpdateTime(allTasks) ?: return
        checkvistTracker.processTasks(allTasks)

        val newEntries = showRss.getNewItems(lastUpdateTime)
        LOG.info("Check done, found [${newEntries.size}] item(s).")

        if (newEntries.isNotEmpty()) {
            val enqueueResults = checkvistTracker.addTorrentTasks(newEntries)
            val nextLastUpdateTime = CheckvistTracker.calculateLastUpdateTime(lastUpdateTime, enqueueResults)
            if (nextLastUpdateTime > lastUpdateTime) {
                checkvistTracker.setLastUpdateTime(nextLastUpdateTime)
            }
        }
    }
}

@Controller
open class TorController {
    @Autowired
    lateinit var tjc: TorrentJobContainer

    @RequestMapping("/track", method = arrayOf(RequestMethod.POST))
    @ResponseStatus(value = HttpStatus.OK)
    open fun trackNow() {
        tjc.checkForTorrents()
    }
}
