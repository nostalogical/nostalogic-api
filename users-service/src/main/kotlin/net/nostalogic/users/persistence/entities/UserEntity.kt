package net.nostalogic.users.persistence.entities

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import net.nostalogic.constants.NoLocale
import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

@Entity
@Table(name = "\"user\"")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
class UserEntity(
        id: String = EntityUtils.uuid(),
        creatorId: String = EntityUtils.SYSTEM_ID,
        var username: String,
        var email: String,
        var displayName: String?,
        @Type(type = "jsonb")  @Column(columnDefinition = "jsonb") var details: String? = "{}",
        @Enumerated(EnumType.STRING) var locale: NoLocale,
        @Enumerated(EnumType.STRING) var status: EntityStatus
): AbstractCoreEntity(id = id, creatorId = creatorId)
