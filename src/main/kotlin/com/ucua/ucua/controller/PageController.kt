package com.ucua.ucua.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Server-side rendered pages backed by the JTE template engine.
 *
 * Each controller method returns a logical view name (e.g. "dashboard") which the
 * auto-configured JTE view resolver maps to `src/main/jte/<viewName>.jte`.
 *
 * The pages themselves are thin shells: authentication is JWT based, so the browser
 * keeps the bearer token and the page scripts talk to the `/api` endpoints. The
 * `ctx` model attribute is the servlet context path ("/ucua") so every asset and API
 * URL rendered by the templates resolves correctly.
 */
@Controller
@RequestMapping("/pages")
class PageController {

    private fun Model.withContext(request: HttpServletRequest) {
        addAttribute("ctx", request.contextPath)
    }

    /** Landing route: send visitors straight to the dashboard (or login via the client guard). */
    @GetMapping("/home")
    fun home(): String = "redirect:/pages/dashboard"

    @GetMapping("/login")
    fun login(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "login"
    }

    @GetMapping("/register")
    fun register(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "register"
    }

    @GetMapping("/dashboard")
    fun dashboard(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "dashboard"
    }

    @GetMapping("/reports")
    fun reports(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "reports"
    }

    @GetMapping("/reports/new")
    fun newReport(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        model.addAttribute("mode", "new")
        model.addAttribute("reportId", "0")
        model.addAttribute("active", "new")
        return "form"
    }

    @GetMapping("/reports/{id}")
    fun reportDetail(@PathVariable id: Int, model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        model.addAttribute("reportId", id.toString())
        return "detail"
    }

    @GetMapping("/reports/{id}/edit")
    fun editReport(@PathVariable id: Int, model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        model.addAttribute("mode", "edit")
        model.addAttribute("reportId", id.toString())
        model.addAttribute("active", "reports")
        return "form"
    }

    @GetMapping("/approvals")
    fun approvals(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "approvals"
    }

    @GetMapping("/lookups")
    fun lookups(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "lookups"
    }

    @GetMapping("/ai")
    fun ai(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "ai"
    }

    @GetMapping("/health")
    fun health(model: Model, request: HttpServletRequest): String {
        model.withContext(request)
        return "health"
    }
}
