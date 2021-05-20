package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.NavEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NavRepository: JpaRepository<NavEntity, String> {

    fun findByFullUrnEquals(urn: String): NavEntity?

    @Query(value = "SELECT n.* FROM navigation n JOIN navigation_link nl ON n.id = nl.child_id LEFT JOIN navigation_link_mask nlm ON nlm.mask_id = nl.child_id WHERE (nl.parent_id = :navId OR nlm.nav_id = :navId) AND nl.status = 'ACTIVE' AND n.status = 'ACTIVE'", nativeQuery = true)
    fun findAllByNavLinks(navId: String): Collection<NavEntity>

    fun findAllByParentIdIs(parentId: String): Collection<NavEntity>

    @Query(value = "SELECT n.* FROM navigation n WHERE n.status IN (:status)", nativeQuery = true)
    fun searchNavs(status: Collection<String>, page: Pageable): Page<NavEntity>

    @Query(value = "SELECT n.* FROM navigation n WHERE n.urn ILIKE ANY (string_to_array(:urns, ','))", nativeQuery = true)
    fun searchNavsByUrn(urns: String, page: Pageable): Page<NavEntity>

    @Query(value = "SELECT n.* FROM navigation n WHERE (n.id IN (:navIds) OR n.urn ILIKE ANY (string_to_array(:urns, ',')) OR n.full_urn ILIKE ANY (string_to_array(:urns, ',')) OR n.text ILIKE ANY (string_to_array(:texts, ','))) AND n.status IN (:status)", nativeQuery = true)
    fun searchNavsByFields(navIds: Collection<String>, urns: String, texts: String, status: Collection<String>, page: Pageable): Page<NavEntity>

}
