package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.NavEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NavRepository: JpaRepository<NavEntity, String> {

    fun findByFullUrnEquals(urn: String): NavEntity?
    fun findAllByParentIdEquals(parentId: String): Collection<NavEntity>

}
