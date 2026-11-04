package com.ucua.ucua.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * sqlite-jdbc 3.47 (org.sqlite.jdbc3.JDBC3ResultSet#getTimestamp / #getDate) can only parse
 * "yyyy-MM-dd HH:mm:ss[.SSS]". Any ISO-8601 text using a 'T' separator - e.g. the value
 * "2026-11-04T09:00:00" that demo/imported data is typically written with - fails with
 * "java.sql.SQLException: Error parsing time stamp", which surfaces as
 * "Could not extract column [n] from JDBC ResultSet" and breaks every query on that table.
 *
 * These auto-applied converters take temporal values off the driver's hands completely:
 * values are written as canonical text and parsed here, accepting every representation that
 * exists in the database (space or 'T' separator, with or without fractional seconds, or
 * epoch millis written by older Hibernate versions).
 */
private val CANONICAL_DATE_TIME: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

private val LENIENT_DATE_TIME: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm")
    .optionalStart()
    .appendPattern(":ss")
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
    .optionalEnd()
    .optionalEnd()
    .toFormatter()

@Converter(autoApply = true)
class SqliteLocalDateTimeConverter : AttributeConverter<LocalDateTime, String> {

    override fun convertToDatabaseColumn(attribute: LocalDateTime?): String? =
        attribute?.format(CANONICAL_DATE_TIME)

    override fun convertToEntityAttribute(dbData: String?): LocalDateTime? {
        val raw = dbData?.trim()
        if (raw.isNullOrEmpty()) return null

        epochToDateTime(raw)?.let { return it }

        runCatching { LocalDateTime.parse(raw.replace('T', ' '), LENIENT_DATE_TIME) }
            .getOrNull()
            ?.let { return it }

        // Date-only value stored in a timestamp column.
        return runCatching { LocalDate.parse(raw.take(10)).atStartOfDay() }.getOrNull()
    }

    private fun epochToDateTime(raw: String): LocalDateTime? {
        if (raw.length < 10 || !raw.all(Char::isDigit)) return null
        val value = raw.toLongOrNull() ?: return null
        return if (raw.length >= 12) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
        } else {
            LocalDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneId.systemDefault())
        }
    }
}

@Converter(autoApply = true)
class SqliteLocalDateConverter : AttributeConverter<LocalDate, String> {

    override fun convertToDatabaseColumn(attribute: LocalDate?): String? = attribute?.toString()

    override fun convertToEntityAttribute(dbData: String?): LocalDate? {
        val raw = dbData?.trim()
        if (raw.isNullOrEmpty()) return null

        if (raw.length >= 10 && raw.all(Char::isDigit)) {
            val value = raw.toLongOrNull()
            if (value != null) {
                return if (raw.length >= 12) {
                    Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
                } else {
                    Instant.ofEpochSecond(value).atZone(ZoneId.systemDefault()).toLocalDate()
                }
            }
        }

        return runCatching { LocalDate.parse(raw.replace('T', ' ').take(10)) }.getOrNull()
    }
}
