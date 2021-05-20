package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.EntityUtils
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object ContainerValidator {

    private val CONTENT_TYPES = setOf(NoEntity.ARTICLE)

    fun validate(container: Container) {
        val report = InvalidFieldsReport()

        if (StringUtils.isBlank(container.navId))
            report.addMissingField("navigationId")
        if (StringUtils.isBlank(container.contentId))
            report.addMissingField("contentId")

        if (StringUtils.isBlank(container.type))
            report.addMissingField("type")
        else if (!EntityUtils.isEntity(container.type!!) || !CONTENT_TYPES.contains(EntityUtils.toEntity(container.type!!)))
            report.addInvalidFieldValue("type", container.type!!)

        report.validate(507004)
    }

}
