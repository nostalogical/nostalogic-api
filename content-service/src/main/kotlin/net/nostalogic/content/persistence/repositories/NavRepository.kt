package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.NavEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NavRepository: JpaRepository<NavEntity, String> {

    fun findByFullUrnEquals(urn: String): NavEntity?

    @Query(value = "SELECT n.* FROM navigation n JOIN navigation_link nl ON n.id = nl.child_id LEFT JOIN navigation_link_mask nlm ON nlm.mask_id = nl.child_id WHERE (nl.parent_id = :navId OR nlm.nav_id = :navId) AND nl.status = 'ACTIVE' AND n.status = 'ACTIVE'", nativeQuery = true)
    fun findAllByNavLinks(navId: String): Collection<NavEntity>

}
