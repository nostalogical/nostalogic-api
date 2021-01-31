package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.validators.InvalidFieldsReport

object ArticleValidator {

    private const val CONTENTS_MAX_LENGTH = 500_000
    private const val TITLE_MAX_LENGTH = 100

    fun validateArticle(article: Article) {
        val report = InvalidFieldsReport()

        if (article.body.isNullOrBlank())
            report.addMissingField("body")
        else if (article.body!!.length > CONTENTS_MAX_LENGTH)
            report.addFieldTooLong("body", CONTENTS_MAX_LENGTH)
        if (article.title.isNullOrBlank())
            report.addMissingField("title")
        else if (article.title!!.length > TITLE_MAX_LENGTH)
            report.addFieldTooLong("title", TITLE_MAX_LENGTH)
        report.validate(507001)
    }

    fun validateRevision(article: Article) {
        val report = InvalidFieldsReport()

        if (article.body.isNullOrBlank() && article.title.isNullOrBlank()) {
            report.addMissingField("body")
            report.addMissingField("title")
        }
        if (article.body != null && article.body!!.length > CONTENTS_MAX_LENGTH)
            report.addFieldTooLong("body", CONTENTS_MAX_LENGTH)
        if (article.title != null && article.title!!.length > TITLE_MAX_LENGTH)
            report.addFieldTooLong("title", TITLE_MAX_LENGTH)

        report.validate(507002)
    }

}
