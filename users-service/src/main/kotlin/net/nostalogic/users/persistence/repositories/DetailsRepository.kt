package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.persistence.entities.DetailsEntity
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

interface DetailsRepository: JpaRepository<DetailsEntity, String> {

    @Transactional
    fun deleteAllById(id: String)

}
