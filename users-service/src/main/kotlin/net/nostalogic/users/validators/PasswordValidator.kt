package net.nostalogic.users.validators

import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object PasswordValidator {

    private const val FIELD_NAME = "password"
    private const val MIN_LENGTH = 6
    private const val MAX_LENGTH = 500

    fun validate(password: String?) {
        simpleValidate(password)
    }

    fun simpleValidate(password: String?) {
        val report = InvalidFieldsReport()
        if (StringUtils.isBlank(password))
            report.addMissingField(FIELD_NAME)
        else {
            if (password!!.length < MIN_LENGTH)
                report.addFieldTooShort(FIELD_NAME, MIN_LENGTH)
            if (password.length > MAX_LENGTH)
                report.addFieldTooLong(FIELD_NAME, MAX_LENGTH)
        }
        report.validate(307002)
    }

}
