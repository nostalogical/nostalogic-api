package net.nostalogic.content.persistence.repositories

import net.nostalogic.constants.NoLocale
import net.nostalogic.content.persistence.entities.ContainerEntity
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

interface ContainerRepository: JpaRepository<ContainerEntity, String> {

    fun findByNavigationIdAndLocale(navId: String, locale: NoLocale): ContainerEntity?
    @Transactional
    fun deleteAllByNavigationId(navId: String)
    fun findByNavigationId(navId: String): ContainerEntity?

}
