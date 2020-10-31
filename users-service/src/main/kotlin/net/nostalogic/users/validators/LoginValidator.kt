package net.nostalogic.users.validators

import net.nostalogic.users.datamodel.authentication.LoginRequest
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object LoginValidator {

    fun validate(loginRequest: LoginRequest, requirePassword: Boolean = true) {
        val report = InvalidFieldsReport()
        if (StringUtils.isBlank(loginRequest.username))
            report.addMissingField("username")
        if (requirePassword && StringUtils.isBlank(loginRequest.password))
            report.addMissingField("password")
        report.validate(307004)
    }

}
