package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.navigations.NavLink
import net.nostalogic.content.utils.PathUtils
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object NavValidator {

    private const val TEXT_MAX_LENGTH = 20
    private const val PATH_MAX_LENGTH = 50
    private const val ICON_MAX_LENGTH = 20

    fun validateNavigation(nav: NavLink, create: Boolean = false) {
        val report = InvalidFieldsReport()

        if (nav.text.isNullOrBlank() && create)
            report.addMissingField("text")
        else if (nav.text != null && nav.text!!.length > TEXT_MAX_LENGTH)
            report.addFieldTooLong("text", TEXT_MAX_LENGTH)

        if (nav.icon.isNullOrBlank() && create)
            report.addMissingField("icon")
        else if (nav.icon != null && nav.icon!!.length > ICON_MAX_LENGTH)
            report.addFieldTooLong("icon", ICON_MAX_LENGTH)

        val sanitisedPath = PathUtils.sanitisePath(nav.path?: "")

        if ((nav.path.isNullOrBlank() || sanitisedPath.split("/").last().isBlank()) && create) // Not expecting the base nav to ever be created
            report.addMissingField("path")
        else if (nav.path != null && nav.path!!.length > PATH_MAX_LENGTH)
            report.addFieldTooLong("path", PATH_MAX_LENGTH)
        else if (!nav.path.isNullOrBlank() && !PathUtils.isPathValid(nav.path!!))
            report.addInvalidFieldValue("path", nav.path!!)

        if (nav.path != null && sanitisedPath.split("/").size > 1 && StringUtils.isBlank(nav.parentId))
            report.addMissingField("parentId")
//        if (StringUtils.isNotBlank(nav.parentId) && (nav.path == null || sanitisedPath.split("/").size <= 1))
//            report.addInvalidFieldValue("path", nav.path!!)

        if (!create && nav.text == null && nav.icon == null && nav.path == null) {
            report.addMissingField("text")
            report.addMissingField("icon")
            report.addMissingField("path")
        }

        report.validate(507003)
    }

}
