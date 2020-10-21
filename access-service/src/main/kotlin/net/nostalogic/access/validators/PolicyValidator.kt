package net.nostalogic.access.validators

import net.nostalogic.access.services.AccessService
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.EntityUtils
import net.nostalogic.validation.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object PolicyValidator {

    private const val MAX_NAME_LENGTH = 50

    fun validate(policy: Policy, isNewEntity: Boolean = true) {
        val report = InvalidFieldsReport()
        if (isNewEntity && StringUtils.isBlank(policy.name))
            report.addMissingField("name")
        validateName(policy, report)
        if (isNewEntity && policy.priority == null)
            report.addMissingField("priority")

        validateResources(policy, report)
        validateSubjects(policy, report)

        report.validate(207002)
    }

    private fun validateName(policy: Policy, report: InvalidFieldsReport) {
        if (StringUtils.isNotBlank(policy.name) && policy.name!!.length > MAX_NAME_LENGTH)
            report.addFieldTooLong("name", MAX_NAME_LENGTH)
    }

    private fun validateSubjects(policy: Policy, report: InvalidFieldsReport) {
        policy.subjects?.let {
            for (id in policy.subjects!!) {
                val ref = EntityUtils.toEntityRef(id)
                if (!(ref.isSignature() && AccessService.ALLOWED_SUBJECTS.contains(ref.entity)) && NoEntity.ALL != ref.entity) {
                    report.addInvalidEntity("subjects", id)
                }
            }
        }
    }

    private fun validateResources(policy: Policy, report: InvalidFieldsReport) {
        policy.resources?.let {
            for (id in policy.resources!!) {
                if (!EntityUtils.isEntityReference(id)) {
                    report.addInvalidEntity("resources", id)
                }
            }
        }
    }

}
