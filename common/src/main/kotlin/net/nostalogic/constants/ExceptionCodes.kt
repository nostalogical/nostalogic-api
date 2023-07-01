package net.nostalogic.constants

/**
 * Exception codes are constant integers used to give a quick and easy indication of where an error originated from in the
 * code base. These codes are stored as constants here for an easy overview of their number and usages.
 * These codes are composed of three parts:
 *
 * Service prefix: A two-digit number used to indicate the service, such as user service or access service, that threw
 * the error. Note that while this is intended to be two digits, when written as a primitive it can't include a leading
 * zero.
 *
 * Exception prefix: A two-digit number used to indicate the type of error being thrown, such as "not found" or
 * "access forbidden".
 *
 * Exception suffix: A 3-digit number used to indicate a single location in the code where an exception is thrown. This
 * starts at 001.
 */
object ExceptionCodes {

    /**
     * Access service
     */
    const val _0201001 = 201001
    const val _0201002 = 201002
    const val _0201003 = 201003
    const val _0201004 = 201004
    const val _0201005 = 201005
    const val _0201006 = 201006
    const val _0201007 = 201007
    const val _0201008 = 201008
    const val _0201009 = 201009
    const val _0201010 = 201010
    const val _0201011 = 201011
    const val _0201012 = 201012
    const val _0201013 = 201013

}
