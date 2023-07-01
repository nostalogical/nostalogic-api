package net.nostalogic.content.config

import org.springframework.context.annotation.DependsOn
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.io.IOException
import java.sql.Connection

@Component(value = "ContentLoader")
@DependsOn(value = ["DatabaseLoader"])
class ContentLoader(private val resourceLoader: ResourceLoader,
                    jdbcTemplate: JdbcTemplate
) {

    private val connection: Connection? = if (jdbcTemplate.dataSource != null && jdbcTemplate.dataSource?.connection != null)
        jdbcTemplate.dataSource?.connection else null

    companion object {
        private const val PAGES_PATTERN = "classpath*:preset_pages/**/*.md"
    }

    init {
        runResourceScripts()
    }

    fun runResourceScripts() {
        if (connection == null)
            return

        val pageFiles = try {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PAGES_PATTERN)
        } catch (e: IOException) {
            throw RuntimeException("Unable to load page files from resources")
        }

        for (file in pageFiles) {
            val urn = file.filename!!.replace(".md", "")
            val contents = file.file.inputStream().readBytes().toString(Charsets.UTF_8)
            val ps = connection.prepareStatement("UPDATE article SET contents = ?, last_updated = now() WHERE name ilike ?")
            ps.setString(1, contents)
            ps.setString(2, urn)
            ps.execute()
        }
    }

}
