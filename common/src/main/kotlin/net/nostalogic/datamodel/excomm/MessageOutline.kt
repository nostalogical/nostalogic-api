package net.nostalogic.datamodel.excomm

import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale
import java.util.*
import kotlin.collections.HashMap

class MessageOutline(
        val recipientId: String,
        val recipientEmailAddress: String,
        val type: MessageType,
        val locale: NoLocale,
        val parameters: HashMap<String, String> = HashMap()) {

    fun setParameter(key: String, value: String): MessageOutline {
        parameters[key.lowercase(Locale.getDefault())] = value
        return this
    }
}
