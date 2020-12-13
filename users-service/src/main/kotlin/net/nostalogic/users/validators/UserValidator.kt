package net.nostalogic.users.validators

import net.nostalogic.users.datamodel.users.SecureUserUpdate
import net.nostalogic.users.datamodel.users.User
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.validators.InvalidFieldsReport

object UserValidator {

    fun validateUpdate(user: User, userId: String, userByName: UserEntity?) {
        val report = InvalidFieldsReport()
        if (userByName != null && userByName.id != userId)
            report.addFieldAlreadyInUse("username")
        report.validate(307005)
    }

    fun validateSecureUpdate(userId: String, secureUserUpdate: SecureUserUpdate, userByEmail: UserEntity?) {
        val report = InvalidFieldsReport()
        if (userByEmail != null && userByEmail.id != userId)
            report.addFieldAlreadyInUse("email")
        report.validate(307007)
    }

}
