package net.nostalogic.constants

enum class UsernameRule {

    USERNAME_ONLY, // Ignore tags, users will be informed if their username is taken
    AUTO_TAG, // Automatically append a random tag to a username if it's taken
    ALWAYS_TAG; // Always append a random tag to a username - bare usernames are disallowed

    companion object {

        fun fromName(name: String?): UsernameRule? {
            for (rule in UsernameRule.values()) {
                if (rule.name.equals(name, true))
                    return rule
            }
            return null
        }

    }

}
