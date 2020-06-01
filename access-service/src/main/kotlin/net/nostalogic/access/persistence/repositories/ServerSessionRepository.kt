package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import org.springframework.data.repository.CrudRepository

interface ServerSessionRepository : CrudRepository<ServerSessionEntity, String> {
    fun findAllByUserId(userId: String): Set<ServerSessionEntity>
}
