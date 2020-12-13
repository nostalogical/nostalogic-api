package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.persistence.entities.AuthenticationEntity
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

interface AuthenticationRepository: JpaRepository<AuthenticationEntity, String> {

    fun findTopByUserIdEqualsOrderByCreatedDesc(userId: String): AuthenticationEntity?
    @Transactional
    fun deleteAllByUserId(userId: String)

}
