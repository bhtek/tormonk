package com.sc.boonatsc.autosync

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
open class Application {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
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