package com.ucua.ucua.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Server-side rendered pages backed by the JTE template engine.
 *
 * These controllers return a logical view name (e.g. "home") which the
 * auto-configured [gg.jte.springframework.boot.autoconfigure.JteViewResolver]
 * resolves to a template under `src/main/jte/<viewName>.jte`.
 */
@Controller
@RequestMapping("/pages")
class PageController {

    @GetMapping("/home")
    fun home(model: Model): String {
        model.addAttribute("appName", "UCUA")
        model.addAttribute("version", "1.0.0")
        model.addAttribute(
            "generatedAt",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())
        )
        model.addAttribute("showEndpoints", true)
        model.addAttribute(
            "endpoints",
            listOf(
                "/api/health",
                "/api/auth/login",
                "/api/dashboard",
                "/api/reports",
                "/api/workflows",
                "/api/lookups"
            )
        )
        return "home"
    }
}
