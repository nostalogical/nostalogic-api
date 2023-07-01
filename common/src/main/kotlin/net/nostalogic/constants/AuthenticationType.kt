package net.nostalogic.constants

enum class AuthenticationType {
    TEST, // Access for unit tests
    GUEST, // Access for a not-logged-in user

    REFRESH, // Long term access to generate login tokens for a user
    LOGIN, // Short term access to a user's account via login
    IMPERSONATION, // Short term access to a user's account through impersonation

    CONFIRMATION, // Sent to an email to confirm the recipient has access to it and is not a bot
    PASSWORD_RESET // Sent to an email to allow password reset
}
