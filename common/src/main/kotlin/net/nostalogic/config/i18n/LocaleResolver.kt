package net.nostalogic.config.i18n

import org.apache.commons.lang3.StringUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*
import javax.servlet.http.HttpServletRequest

@Configuration
open class LocaleResolver : AcceptHeaderLocaleResolver(), WebMvcConfigurer {

    private val locales: List<Locale> = listOf(
            Locale("en"),
            Locale("no")
    )

    override fun resolveLocale(request: HttpServletRequest): Locale {
        val headerLang = request.getHeader("Accept-Language")
        val parameterLang = request.getParameter("lang")
        val parseLocale: Locale? = when {
            StringUtils.isNotEmpty(parameterLang) -> Locale.lookup(Locale.LanguageRange.parse(parameterLang), locales)
            StringUtils.isNotEmpty(headerLang) -> Locale.lookup(Locale.LanguageRange.parse(headerLang), locales)
            else -> Locale.getDefault()
        }
        return parseLocale ?: Locale.UK
    }

    @Bean
    open fun messageSource(): ResourceBundleMessageSource {
        val rs = ResourceBundleMessageSource()
        rs.setBasename("i18n")
        rs.addBasenames("i18n_errors")
        rs.setDefaultEncoding("UTF-8")
        rs.setUseCodeAsDefaultMessage(true)
        return rs
    }
}
