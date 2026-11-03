package com.ucua.ucua

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class UcuaApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
    runApplication<UcuaApplication>(*args)
}
