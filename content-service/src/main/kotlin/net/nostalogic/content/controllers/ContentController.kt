package net.nostalogic.content.controllers

import net.nostalogic.content.ContentApplication
import net.nostalogic.content.controllers.ContentController.Companion.CONTENT_ENDPOINT
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.services.ContentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@CrossOrigin
@RequestMapping(CONTENT_ENDPOINT, produces = ["application/json"])
class ContentController(@Autowired private val contentService: ContentService) {

    companion object {
        const val CONTENT_ENDPOINT = "/api/v${ContentApplication.MAJOR}/content"
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["**"])
    fun getContent(request: HttpServletRequest): Content<*> {
        return contentService.getNavContent(request.requestURI.substringAfter(CONTENT_ENDPOINT))
    }
}
