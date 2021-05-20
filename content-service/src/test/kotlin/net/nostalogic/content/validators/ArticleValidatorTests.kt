package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.exceptions.NoValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
class ArticleValidatorTests {

    private val longTitle = "A title over the 100 character limit for the length of the title which is restricted to 100 characters"

    private fun article(): Article {
        return Article(title = "An article title", body = "An article body")
    }

    @Test
    fun `Confirm a valid article`() {
        ArticleValidator.validateArticle(article())
    }

    @Test
    fun `Confirm an article with a null or empty title is invalid`() {
        val article = article()
        article.title = null
        assertThrows<NoValidationException> { ArticleValidator.validateArticle(article) }
        article.title = " "
        assertThrows<NoValidationException> { ArticleValidator.validateArticle(article) }
    }

    @Test
    fun `Confirm an article with a title over the size limit is invalid`() {
        val article = article()
        article.title = longTitle
        assertThrows<NoValidationException> { ArticleValidator.validateArticle(article) }
    }

    @Test
    fun `Confirm an article with a null or empty body is invalid`() {
        val article = article()
        article.body = null
        assertThrows<NoValidationException> { ArticleValidator.validateArticle(article) }
        article.body = " "
        assertThrows<NoValidationException> { ArticleValidator.validateArticle(article) }
    }

    @Test
    fun `Confirm a valid revision`() {
        ArticleValidator.validateRevision(article())
    }

    @Test
    fun `Confirm a revision with either a null or empty title OR body is valid`() {
        var article = article()
        article.title = null
        ArticleValidator.validateRevision(article)
        article.title = " "
        article = article()
        ArticleValidator.validateRevision(article)
        article.body = null
        ArticleValidator.validateRevision(article)
        article.body = " "
        ArticleValidator.validateRevision(article)
    }

    @Test
    fun `Confirm a revision with both a null or empty title AND body is invalid`() {
        val article = article()
        article.title = null
        article.body = null
        assertThrows<NoValidationException> { ArticleValidator.validateRevision(article) }
        article.title = " "
        article.body = " "
        assertThrows<NoValidationException> { ArticleValidator.validateRevision(article) }
    }

    @Test
    fun `Confirm a revision with a title over the size limit is invalid`() {
        val article = article()
        article.title = longTitle
        assertThrows<NoValidationException> { ArticleValidator.validateRevision(article) }
    }

}
