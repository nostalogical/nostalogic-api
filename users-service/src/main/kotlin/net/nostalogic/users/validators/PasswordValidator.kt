package net.nostalogic.users.validators

import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object PasswordValidator {

    private const val FIELD_NAME = "password"
    private const val MIN_LENGTH = 6

    fun validate(password: String?) {
        simpleValidate(password)
        // At least 1 number too?
//        report.validate(307003)
    }

    fun simpleValidate(password: String?) {
        val report = InvalidFieldsReport()
        if (StringUtils.isBlank(password))
            report.addMissingField(FIELD_NAME)
        else {
            if (password!!.length < MIN_LENGTH)
                report.addFieldTooShort(FIELD_NAME, MIN_LENGTH)
        }
        report.validate(307002)
    }

}
