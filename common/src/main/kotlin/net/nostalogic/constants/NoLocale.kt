package net.nostalogic.constants

enum class NoLocale {
    en_GB,
    no_NO;

    companion object {
        fun fromString(string: String): NoLocale? {
            for (locale in NoLocale.values()) {
                if (locale.name.equals(string, true))
                    return locale
            }
            return null
        }
    }
}
