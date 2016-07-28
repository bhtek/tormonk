package com.sc.boonatsc.autosync

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@SpringBootApplication
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
    @PostConstruct fun init() {
        val url = "http://showrss.info/user/72839.rss?magnets=true&namespaces=true&name=null&quality=null&re=null"
        url.httpGet().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    print("Failed to fetch from remote.")
                }
                is Result.Success -> {
                    print("RSS Result: ${result}")
                }
            }
        }

        print(serverAddress)
    }
}