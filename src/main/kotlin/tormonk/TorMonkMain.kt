package tormonk

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.annotation.Resource

@SpringBootApplication
@ComponentScan("tormonk")
@EnableScheduling
@PropertySource("classpath:/config.local.properties", ignoreResourceNotFound = true)
open class TorMonkApplication {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(TorMonkApplication::class.java, *args)
        }
    }
}

@Component
class Scheduled {
    companion object {
        val LOG = LoggerFactory.getLogger(tormonk.Scheduled::class.java.name)
    }

    @Resource lateinit var checkvistTracker: CheckvistTracker
    @Resource lateinit var showRss: ShowRss

    @Scheduled(cron = "0 23 * * * *")
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