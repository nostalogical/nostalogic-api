package net.nostalogic.content.controllers

import net.nostalogic.content.ContentApplication
import net.nostalogic.content.controllers.NavigationController.Companion.NAV_ENDPOINT
import net.nostalogic.content.datamodel.navigations.Nav
import net.nostalogic.content.datamodel.navigations.NavDetails
import net.nostalogic.content.datamodel.navigations.NavigationSearchCriteria
import net.nostalogic.content.services.NavService
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.entities.EntityStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@CrossOrigin
@RequestMapping(NAV_ENDPOINT, produces = ["application/json"])
class NavigationController(@Autowired private val navService: NavService) {

    companion object {
        const val NAV_ENDPOINT = "/api/v${ContentApplication.MAJOR}/cms"
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/path/**"])
    fun getNavsAtPath(request: HttpServletRequest): NavDetails {
        return navService.getNavDetails(request.requestURI.substringAfter("${NAV_ENDPOINT}/path"))
    }

    @RequestMapping(method = [RequestMethod.POST], path = ["/navigations"])
    fun createNav(@RequestBody nav: Nav): Nav {
        return navService.createNav(nav)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/navigations/{navigationId}"])
    fun editNav(@PathVariable navigationId: String, @RequestBody nav: Nav): Nav {
        nav.id = navigationId
        return navService.editNav(nav)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/navigations/{navigationId}"])
    fun getNav(@PathVariable navigationId: String): Nav {
        return navService.getNav(navigationId)
    }

    /**
     * Navs use hard deletions due to each nav 'reserving' it's associated path.
     * Deleting a nav also deletes any and all of its child navs, regardless of the user's permissions for them.
     */
    @RequestMapping(method = [RequestMethod.DELETE], path = ["/navigations/{navigationId}"])
    fun deleteNav(@PathVariable navigationId: String) {
        navService.deleteNav(navigationId)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/navigations"])
    fun searchNavs(@RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "20") size: Int,
                   @RequestParam id: Set<String>?,
                   @RequestParam urn: Set<String>?,
                   @RequestParam text: Set<String>?,
                   @RequestParam status: Set<EntityStatus>?): NoPageResponse<Nav> {
        val pageable = NoPageable<Nav>(page, size, *NavigationSearchCriteria.DEFAULT_SORT_FIELDS)
        val result = navService.searchNavs(NavigationSearchCriteria(navIds = id, urns = urn, texts = text, status = status, page = pageable))
        return pageable.toResponse(result)
    }

}
