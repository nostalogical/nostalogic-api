package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import java.time.temporal.ChronoUnit

/**
 * Used to bypass the access service and mock rights for tests. Should never be generated outside a test profile.
 */
class TestGrant() : NoGrant(
        subject = TEST_SUBJECT,
        expiration = NoDate.plus(2, ChronoUnit.MINUTES),
        type = AuthenticationType.TEST,
        created = NoDate()
) {

    companion object {
        const val TEST_SUBJECT = "test_user_id"
    }
}
