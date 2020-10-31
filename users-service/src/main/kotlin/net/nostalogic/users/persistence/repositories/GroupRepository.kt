package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.persistence.entities.GroupEntity
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository: JpaRepository<GroupEntity, String> {
}
