package tormonk

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    @Bean open fun propertySourcesPlaceholderConfigurator(): PropertySourcesPlaceholderConfigurer {
        val configurator = PropertySourcesPlaceholderConfigurer()
        configurator.setNullValue("@null")
        return configurator
    }
}

@RestController
@RequestMapping("/auto-sync")
class AutoSyncController {
    @RequestMapping("/register")
    fun register(): String {
        return "hello world"
    }
}

@Component
class DoOnStartup {
    @Value("\${auto-sync.server:@null}") var serverAddress: String? = null
    @Resource lateinit var checkvistTracker : CheckvistTracker

    @PostConstruct fun init() {
        println("Last Update Time: ${checkvistTracker.getLastUpdateTime()}")
    }
}