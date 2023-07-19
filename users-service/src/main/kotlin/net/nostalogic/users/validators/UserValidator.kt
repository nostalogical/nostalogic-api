package net.nostalogic.users.validators

import net.nostalogic.constants.NoLocale
import net.nostalogic.users.datamodel.users.SecureUserUpdate
import net.nostalogic.users.datamodel.users.User
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.utils.Serialiser
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object UserValidator {

    private const val DETAILS_MAX_LENGTH = 50_000
    private const val USERNAME_MAX_LENGTH = 20

    fun validateUpdate(user: User, userId: String, userByName: UserEntity?) {
        val report = InvalidFieldsReport()
        if (userByName != null && userByName.id != userId)
            report.addFieldAlreadyInUse("username")
        if (StringUtils.isNotBlank(user.username) && user.username!!.length > USERNAME_MAX_LENGTH)
            report.addFieldTooLong("username", USERNAME_MAX_LENGTH)
        if (StringUtils.isNotBlank(user.locale) && NoLocale.fromString(user.locale!!) == null)
            report.addInvalidFieldValue("locale", user.locale!!)
        if (user.details != null && !Serialiser.isValidJson(user.details!!))
            report.addInvalidFieldValue("details", user.details.toString())
        if (user.details != null && Serialiser.toJsonObject(user.details!!)!!.length() > DETAILS_MAX_LENGTH)
            report.addFieldTooLong("details", DETAILS_MAX_LENGTH)
        report.validate(307005)
    }

    fun validateSecureUpdate(userId: String, secureUserUpdate: SecureUserUpdate, userByEmail: UserEntity?) {
        val report = InvalidFieldsReport()
        if (userByEmail != null && userByEmail.id != userId)
            report.addFieldAlreadyInUse("email")
        if (StringUtils.isBlank(secureUserUpdate.currentPassword))
            report.addMissingField("password")
        report.validate(307007)
    }

}
