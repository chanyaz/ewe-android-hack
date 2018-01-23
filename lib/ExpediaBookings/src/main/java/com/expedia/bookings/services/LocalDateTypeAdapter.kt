package com.expedia.bookings.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Type

class LocalDateTypeAdapter(val pattern: String) : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    val fmt = DateTimeFormat.forPattern(pattern)

    override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(fmt.print(src))
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate? {
        // Do not try to deserialize null or empty values
        if (json.asString == null || json.asString.isEmpty()) {
            return null
        }

        return fmt.parseLocalDate(json.asString)
    }
}
