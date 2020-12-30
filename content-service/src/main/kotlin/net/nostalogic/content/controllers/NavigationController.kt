package net.nostalogic.content.controllers

import net.nostalogic.content.ContentApplication
import net.nostalogic.content.controllers.NavigationController.Companion.NAV_ENDPOINT
import net.nostalogic.content.datamodel.NavDetails
import net.nostalogic.content.services.NavService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@CrossOrigin
@RequestMapping(NAV_ENDPOINT, produces = ["application/json"])
class NavigationController(@Autowired private val navService: NavService) {

    companion object {
        const val NAV_ENDPOINT = "/api/v${ContentApplication.MAJOR}/navigations"
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["path/**"])
    fun getContent(request: HttpServletRequest): NavDetails {
        return navService.getNavDetails(request.requestURI.substringAfter("${NAV_ENDPOINT}/path"))
    }

    // Create nav
    // Edit nav - include setting content on a nav
    // Delete nav
    // Get nav
    // Get all navs

}
