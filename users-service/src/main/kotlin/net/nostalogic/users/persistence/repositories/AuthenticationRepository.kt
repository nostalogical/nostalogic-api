package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.persistence.entities.AuthenticationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AuthenticationRepository: JpaRepository<AuthenticationEntity, String> {

    fun findTopByUserIdEqualsOrderByCreatedDesc(userId: String): AuthenticationEntity?

}
