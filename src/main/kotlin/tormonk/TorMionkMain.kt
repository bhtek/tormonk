package tormonk

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.Resource

@SpringBootApplication
@ComponentScan("tormonk")
@PropertySource("classpath:/config.local.properties", ignoreResourceNotFound = true)
open class TorMonkApplication {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(TorMonkApplication::class.java, *args)
        }
    }
}

@Component
class DoOnStartup {
    @Resource lateinit var checkvistTracker: CheckvistTracker
    @Resource lateinit var showRss: ShowRss

    @PostConstruct fun init() {
        val lastUpdateTime = checkvistTracker.getLastUpdateTime() ?: return
        val channel = showRss.getNewItems(lastUpdateTime)
        checkvistTracker.addTorrentTasks(channel.items ?: emptyList())
    }
}