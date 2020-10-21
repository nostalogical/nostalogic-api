package net.nostalogic.validation

import net.nostalogic.constants.ErrorStrings
import net.nostalogic.exceptions.NoValidationException

class InvalidFieldsReport {

    private val fieldNames = ArrayList<String>()
    private val invalidReasons = HashMap<String, String>()

    fun addMissingField(fieldName: String) {
        this.fieldNames.add(fieldName)
        this.invalidReasons[fieldName] = ErrorStrings.FIELD_MISSING
    }

    fun addFieldTooLong(fieldName: String, upperLimit: Int) {
        this.fieldNames.add(fieldName)
        this.invalidReasons[fieldName] = ErrorStrings.fieldTooLong(upperLimit)
    }

    fun addFieldTooShort(fieldName: String, lowerLimit: Int) {
        this.fieldNames.add(fieldName)
        this.invalidReasons[fieldName] = ErrorStrings.fieldTooShort(lowerLimit)
    }

    fun addInvalidEntity(fieldName: String, badEntity: String) {
        this.fieldNames.add(fieldName)
        this.invalidReasons[fieldName] = ErrorStrings.invalidEntity(badEntity)
    }

    /**
     * Throws a validation exception with the supplied error code if any validation has failed, otherwise does nothing.
     */
    fun validate(errorCode: Int) {
        if (fieldNames.isNotEmpty()) {
            val invalids = ArrayList<String>()
            for (field in fieldNames) {
                val reason = if (invalidReasons[field] == null) "" else " (${invalidReasons[field]})"
                invalids.add(field + reason)
            }

            throw NoValidationException(errorCode, invalids.joinToString(", "))
        }
    }

}
