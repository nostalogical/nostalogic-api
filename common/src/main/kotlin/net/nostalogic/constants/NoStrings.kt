package net.nostalogic.constants

import net.nostalogic.config.i18n.Translator

object NoStrings {

    fun authGranted(): String {return Translator.translate("authSuccess")}
    fun authValid(): String {return Translator.translate("authValid")}
    fun authInvalidated(): String {return Translator.translate("logoutSuccess")}
    fun authAlreadyInvalid(): String {return Translator.translate("alreadyLoggedOut")}
    fun guest(): String {return Translator.translate("guest")}

    fun sessionCreateFail(): String {return Translator.translate("sessionCreateFail")}
    fun sessionVerifyFail(): String {return Translator.translate("sessionVerifyFail")}
    fun sessionRefreshFail(): String {return Translator.translate("sessionRefreshFail")}
    fun sessionEndFail(): String {return Translator.translate("sessionEndFail")}

    fun authMissing(): String {return Translator.translate("authMissing")}
    fun authInvalid(): String {return Translator.translate("authInvalid")}
    fun authNotSupported(): String {return Translator.translate("authNotSupported")}
    fun authMethodMismatch(): String {return Translator.translate("authMethodMismatch")}
    fun authInvalidRequest(): String {return Translator.translate("invalidAuthRequest")}
    fun authRefreshDenied(): String {return Translator.translate("refreshDenied")}
    fun passwordNotVerified(): String {return Translator.translate("passwordNotVerified")}
    fun impersonationDenied(): String {return Translator.translate("impersonationDenied")}
    fun notLoggedIn(): String {return Translator.translate("notLoggedIn")}
    fun passwordChanged(): String {return Translator.translate("passwordWasChanged")}
    fun passwordEmailSent(): String {return Translator.translate("passwordEmailSent")}

    fun membershipPermissions(): String {return Translator.translate("membershipPermissions")}
    fun membershipUnchanged(): String {return Translator.translate("membershipUnchanged")}
    fun alreadyMember(): String {return Translator.translate("alreadyMember")}
    fun alreadySuspended(): String {return Translator.translate("alreadySuspended")}
    fun notMember(): String {return Translator.translate("notMember")}
    fun ownerCannotLeave(): String {return Translator.translate("ownerCannotLeave")}
    fun cannotRemoveFromGroup(): String {return Translator.translate("cannotRemoveFromGroup")}
    fun cannotRemoveFromRightsGroup(): String {return Translator.translate("cannotRemoveFromRightsGroup")}


    const val EMAIL_REGEX = "\\w+@\\w+\\.\\w+"
    const val DETAIL_USERNAME = "username_login"
    const val DETAIL_EMAIL = "email_login"
    const val DETAIL_IMPERSONATE = "admin_override"
    const val AUTH_HEADER = "Authorization"
    const val TENANT_HEADER = "Tenant"
    const val AUTH_COOKIE = "NostaAuth"
    const val AUTH_GUEST = "GUEST"
    const val TEST_TOKEN = "test_token"
}
