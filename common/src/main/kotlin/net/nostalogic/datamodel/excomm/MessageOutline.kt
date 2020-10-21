package net.nostalogic.datamodel.excomm

import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale

class MessageOutline(
        val recipientId: String,
        val recipientEmailAddress: String,
        val type: MessageType,
        val locale: NoLocale,
        val parameters: HashMap<String, String> = HashMap()) {

    fun setParameter(key: String, value: String): MessageOutline {
        parameters[key.toLowerCase()] = value
        return this
    }
}
