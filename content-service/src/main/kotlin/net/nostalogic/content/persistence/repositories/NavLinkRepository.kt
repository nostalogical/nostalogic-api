package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.NavLinkEntity
import net.nostalogic.entities.EntityStatus
import org.springframework.data.jpa.repository.JpaRepository

interface NavLinkRepository: JpaRepository<NavLinkEntity, String> {

    fun findAllByParentIdEqualsAndStatusIn(parentId: String, status: Collection<EntityStatus>): Collection<NavLinkEntity>

}
