package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import org.springframework.data.repository.CrudRepository
import java.sql.Timestamp

interface ServerSessionRepository : CrudRepository<ServerSessionEntity, String> {
    fun findAllByUserIdAndEndDateTimeIsAfter(userId: String, now: Timestamp): Set<ServerSessionEntity>
}
