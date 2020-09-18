package net.nostalogic.access.datamodel

import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity

class PolicyEntityComponents(val policy: PolicyEntity,
                             val actions: Collection<PolicyActionEntity>,
                             val subjects: Collection<PolicySubjectEntity>,
                             val resources: Collection<PolicyResourceEntity>)
