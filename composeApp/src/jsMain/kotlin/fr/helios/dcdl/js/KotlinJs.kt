package fr.helios.dcdl.js

import kotlinx.serialization.json.Json

object KotlinJs {
    inline fun <reified T> decodeJsObject(jsObject: dynamic) =
        Json.decodeFromString<T>(JSON.stringify(jsObject))
}
