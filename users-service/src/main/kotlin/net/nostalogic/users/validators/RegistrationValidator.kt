package net.nostalogic.users.validators

import net.nostalogic.users.datamodel.RegistrationAvailability
import net.nostalogic.users.datamodel.UserRegistration
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object RegistrationValidator {

    fun validateRegistration(userRegistration: UserRegistration, registrationAvailability: RegistrationAvailability) {
        val report = InvalidFieldsReport()
        if (StringUtils.isBlank(userRegistration.username))
            report.addMissingField("username")
        if (StringUtils.isBlank(userRegistration.email))
            report.addMissingField("email")
        if (!registrationAvailability.usernameAvailable!!)
            report.addFieldAlreadyInUse("username")
        if (!registrationAvailability.emailAvailable!!)
            report.addFieldAlreadyInUse("email")
        report.validate(307001)
    }

}
