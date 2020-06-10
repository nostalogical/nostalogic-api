package net.nostalogic.config.i18n

import net.nostalogic.config.Config
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class Translator(messageSource: MessageSource) {

    companion object {
        private lateinit var messageSource: MessageSource

        fun translate(msgCode: String): String {
            if (Config.isUnitTest()) return msgCode
            val locale = LocaleContextHolder.getLocale()
            return messageSource.getMessage(msgCode, null, locale)
        }

        fun translate(msgCode: String, vararg args: String): String {
            if (Config.isUnitTest()) return msgCode
            val locale = LocaleContextHolder.getLocale()
            return messageSource.getMessage(msgCode, args, locale)
        }
    }

    init {
        Companion.messageSource = messageSource
    }

}
