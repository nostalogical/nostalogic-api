package net.nostalogic.constants

enum class Tenant {
    NOSTALOGIC;

    companion object {

        fun fromName(name: String?): Tenant? {
            for (tenant in Tenant.values()) {
                if (tenant.name.equals(name, true))
                    return tenant
            }
            return null
        }

    }
}
