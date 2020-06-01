package net.nostalogic.datamodel

import com.fasterxml.jackson.annotation.JsonValue
import net.nostalogic.constants.ErrorStrings
import net.nostalogic.exceptions.NoValidationException
import java.lang.Exception
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

/**
 * Date class, wrapping around a <code>Instant</code> object.
 * Should allow easy construction of date object from a long (millis), Timestamp or String.
 * <P>
 */
class NoDate {

    companion object {
        fun plus(amount: Long, unit: ChronoUnit): NoDate {
            return NoDate(Instant.now().plus(amount, unit))
        }
    }

    private val instant: Instant
    @Transient
    private val strict: Boolean = false

    constructor() {
        this.instant = Instant.now()
    }

    constructor(timestamp: Timestamp) {
        this.instant = timestamp.toInstant()
    }

    constructor(instant: Instant) {
        this.instant = instant
    }

    constructor(time: Long) {
        this.instant = Instant.ofEpochMilli(time)
    }

    constructor(date: Date) {
        this.instant = date.toInstant()
    }

    constructor(dateString: String) {
        var formatTime: Instant? = null
        for (format in Format.values()) {
            try {
                formatTime = format.parse(dateString)
                break
            } catch (ignored: Exception) {}
        }
        if (formatTime != null) {
            this.instant = formatTime
        } else
            throw NoValidationException(106001, "date", ErrorStrings.dateFormat(dateString), null)
    }

    override fun equals(other: Any?): Boolean {
        if (other is NoDate) {
            val diff = abs(this.getTime() - other.getTime())
            if (this.strict && other.strict)
                return diff == 0L
            return diff < 1000L
        }
        return false
    }

    fun isAfter(other: NoDate): Boolean {
        return this.instant.isAfter(other.instant)
    }

    fun isBefore(other: NoDate): Boolean {
        return this.instant.isBefore(other.instant)
    }

    fun getTime(): Long {
        return instant.toEpochMilli()
    }

    fun getTimestamp(): Timestamp {
        return Timestamp.from(instant)
    }

    fun getDate(): Date {
        return Date.from(instant)
    }

    @JsonValue
    fun getIsoDate(): String {
        return instant.truncatedTo(ChronoUnit.MILLIS).toString()
    }

    fun getJavascript(): String {
        return Date(getTime()).toString()
    }

    /**
     * Returns a new NoDate object with original time plus the supplied change.
     */
    fun addTime(amount: Long, unit: ChronoUnit): NoDate {
        return NoDate(instant.plus(amount, unit))
    }

    enum class Format(val sdf: SimpleDateFormat) {
        FRIENDLY_MILLIS(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")),
        ISO(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")),
        ISO_MILLIS(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")),
        FRIENDLY(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        fun parse(rawDate: String): Instant {
            return sdf.parse(rawDate).toInstant()
        }
    }

}
