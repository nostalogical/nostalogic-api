package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.AccessExtensionEntity
import org.springframework.data.repository.CrudRepository

interface AccessExtensionRepository: CrudRepository<AccessExtensionEntity, String> {
    fun findAllByUserId(id: String): Set<AccessExtensionEntity>
    fun deleteAllByUserId(id: String)
}
