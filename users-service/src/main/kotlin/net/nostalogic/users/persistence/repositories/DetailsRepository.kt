package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.persistence.entities.DetailsEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DetailsRepository: JpaRepository<DetailsEntity, String> {
}
