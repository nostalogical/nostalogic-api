package net.nostalogic.security.filters

import net.nostalogic.config.Config
import net.nostalogic.constants.NoStrings
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.GuestGrant
import net.nostalogic.security.grants.TestGrant
import net.nostalogic.security.utils.TokenDecoder
import org.apache.catalina.connector.RequestFacade
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        handleRequestAuthorization(request, handler)
        return super.preHandle(request, response, handler)
    }

    /**
     * Extract an Authorization header token if it exists, convert it to a session grant, and store it in the session
     * context for easy access. If no token is supplied either a Test or Guest grant is generated instead, depending on
     * the current configuration.
     */
    private fun handleRequestAuthorization(request: HttpServletRequest, handler: Any) {
        if (doesMethodHaveAnnotation(handler, IgnoreTokens::class.java))
            return

        val token: String? = extractToken(request)
        token?.let {
            val grant = try {
                TokenDecoder.decodeToken(it)
            } catch (e: Exception) {
                if (Config.isTest()) TestGrant() else GuestGrant()
            }
            SessionContext.currentSession.set(SessionContext(token, grant))
        } ?: run {
            val context = if (Config.isTest()) SessionContext(grant = TestGrant()) else SessionContext()
            SessionContext.currentSession.set(context)
        }

        if (doesMethodHaveAnnotation(handler, RequireLogin::class.java))
            SessionContext.requireLogin()
    }

    private fun extractToken(request: HttpServletRequest): String? {
        return if (request is RequestFacade)
            request.getHeader(NoStrings.AUTH_HEADER)
        else null
    }

    private fun <T : Annotation> doesMethodHaveAnnotation(handler: Any, clazz: Class<T>): Boolean {
        val handlerMethod = try {
            handler as HandlerMethod
        } catch (e: ClassCastException) {
            null
        }
        return handlerMethod?.method?.getAnnotation(clazz) != null
    }
}
