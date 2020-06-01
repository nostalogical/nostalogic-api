package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.ServerSessionEventEntity
import org.springframework.data.repository.CrudRepository

interface ServerSessionEventRepository: CrudRepository<ServerSessionEventEntity, Long>
