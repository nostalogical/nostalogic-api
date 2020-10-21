package net.nostalogic.excomm.persistence.repositories

import net.nostalogic.excomm.persistence.entities.EmailEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository: JpaRepository<EmailEntity, String> {
}
