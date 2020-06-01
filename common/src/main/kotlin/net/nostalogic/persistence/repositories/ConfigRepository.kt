package net.nostalogic.persistence.repositories

import net.nostalogic.persistence.entities.ConfigEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfigRepository : CrudRepository<ConfigEntity, Long>
