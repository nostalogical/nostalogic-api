package net.nostalogic.config

class ApiVersion(val major: Int, val minor: Int, val patch: Int) {

    override fun toString(): String {
        return String.format("%02d.%02d.%03d", major, minor, patch)
    }
}
