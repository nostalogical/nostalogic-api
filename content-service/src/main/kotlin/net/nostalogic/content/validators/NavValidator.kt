package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.navigations.Nav
import net.nostalogic.validators.InvalidFieldsReport

object NavValidator {

    private const val TEXT_MAX_LENGTH = 20
    private const val PATH_MAX_LENGTH = 20
    private const val ICON_MAX_LENGTH = 20

    fun validateNavigation(nav: Nav, create: Boolean = false) {
        val report = InvalidFieldsReport()

        if (nav.text.isNullOrBlank() && create)
            report.addMissingField("text")
        else if (nav.text != null && nav.text!!.length > TEXT_MAX_LENGTH)
            report.addFieldTooLong("text", TEXT_MAX_LENGTH)

        if (nav.icon.isNullOrBlank() && create)
            report.addMissingField("icon")
        else if (nav.icon != null && nav.icon!!.length > ICON_MAX_LENGTH)
            report.addFieldTooLong("icon", ICON_MAX_LENGTH)

        if (nav.path.isNullOrBlank() && create)
            report.addMissingField("path")
        else if (nav.path != null && nav.path!!.length > PATH_MAX_LENGTH)
            report.addFieldTooLong("path", PATH_MAX_LENGTH)

        if (!create && nav.text == null && nav.icon == null && nav.path == null) {
            report.addMissingField("text")
            report.addMissingField("icon")
            report.addMissingField("path")
        }

        report.validate(507003)
    }

}
