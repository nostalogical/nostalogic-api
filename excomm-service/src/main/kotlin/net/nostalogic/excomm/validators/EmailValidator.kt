package net.nostalogic.excomm.validators

import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object EmailValidator {

    fun validate(messageOutline: MessageOutline) {
        val report = InvalidFieldsReport()
        if (StringUtils.isBlank(messageOutline.recipientEmailAddress))
            report.addMissingField("recipientEmailAddress")
        report.validate(407001)
    }
}
