package net.nostalogic.security.filters

import net.nostalogic.config.Config
import net.nostalogic.constants.NoStrings
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.GuestGrant
import net.nostalogic.security.grants.TestGrant
import net.nostalogic.security.utils.TokenDecoder
import org.apache.catalina.connector.RequestFacade
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
class AuthenticationFilter : Filter {
    override fun doFilter(
            request: ServletRequest?,
            response: ServletResponse?, chain: FilterChain?
    ) {
        var token: String? = null
        if (request is RequestFacade)
            token = request.getHeader(NoStrings.AUTH_HEADER)
        if (Config.isTest() && token == null)
            SessionContext.currentSession.set(SessionContext(grant = TestGrant()))
        else {
            if (token != null) {
                val grant = try {
                    TokenDecoder.decodeToken(token)
                } catch (e: Exception) {
                    if (Config.isTest()) TestGrant() else GuestGrant()
                }
                SessionContext.currentSession.set(SessionContext(token, grant))
            } else
                SessionContext.currentSession.set(SessionContext())
        }
        chain?.doFilter(request, response)
    }
}
