package net.nostalogic.constants

import net.nostalogic.config.i18n.Translator

object ErrorStrings {
    const val GENERIC_ERROR = "genericError"
    const val GENERIC_ACCESS = "permissionError"
    const val TOKEN_INVALID = "tokenInvalid"

    private const val FIELDS_INVALID = "fieldsInvalid"
    private const val DATE_FORMAT = "dateFormatInvalid"
    private const val IN_USE_VIOLATION = "duplicateKey"
    private const val RETRIEVE_ERROR = "retrieveError"
    private const val SAVE_ERROR = "saveError"
    private const val DELETE_ERROR = "deleteError"

    fun fieldsInvalid(fields: String): String {
        return Translator.translate(FIELDS_INVALID, fields)
    }

    fun dateFormat(date: String): String {
        return Translator.translate(DATE_FORMAT, date)
    }

    fun fieldAlreadyUsed(field: String): String {
        return Translator.translate(IN_USE_VIOLATION, field)
    }

    fun notFound(obj: String): String {
        return Translator.translate(RETRIEVE_ERROR, obj)
    }

    fun cannotSave(obj: String): String {
        return Translator.translate(SAVE_ERROR, obj)
    }

    fun cannotDelete(obj: String): String {
        return Translator.translate(DELETE_ERROR, obj)
    }
}
