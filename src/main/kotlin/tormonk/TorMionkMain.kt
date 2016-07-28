package com.sc.boonatsc.autosync

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
@PropertySource("classpath:/config.local.propertiess", ignoreResourceNotFound = true)
open class TorMonkApplication {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(TorMonkApplication::class.java, *args)
        }
    }

    @Bean open fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {
        val configurer = PropertySourcesPlaceholderConfigurer()
        configurer.setNullValue("@null")
        return configurer
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
        print(serverAddress)
    }
}