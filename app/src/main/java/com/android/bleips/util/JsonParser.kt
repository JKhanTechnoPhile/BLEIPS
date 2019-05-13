package com.android.bleips.util

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

class JsonParser {
    companion object {

        @Suppress("UNCHECKED_CAST")
        fun getNodes(jsonString: StringBuilder, floor: Int, type: Int) : List<JsonObject> {
            val jsonArray = Parser.default().parse(jsonString) as JsonArray<JsonObject>

            return jsonArray.filter {
                it.int("floor") == floor && it.int("type") == type
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun getNodes(jsonString: StringBuilder, floor: Int) : List<JsonObject> {
            val jsonArray = Parser.default().parse(jsonString) as JsonArray<JsonObject>

            return jsonArray.filter {
                it.int("floor") == floor
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun getNode(jsonString: StringBuilder, roomCode: Int, floor: Int) : JsonObject {
            val jsonArray = Parser.default().parse((jsonString)) as JsonArray<JsonObject>

            val array = jsonArray.filter {
                it.int("room_code") == roomCode && it.int("floor") == floor
            }

            return array.first()
        }

        @Suppress("UNCHECKED_CAST")
        fun getNodeFromArray(jsonArray: List<JsonObject>, roomCode: Int) : JsonObject {
            val array = jsonArray.filter {
                it.int("room_code") == roomCode
            }

            return array.first()
        }

        @Suppress("UNCHECKED_CAST")
        fun getBeacon(jsonString: StringBuilder, major: Int, minor: Int) : JsonObject {
            val jsonArray = Parser.default().parse((jsonString)) as JsonArray<JsonObject>

            val array = jsonArray.filter {
                it.int("major") == major && it.int("minor") == minor
            }

            return array.first()
        }

        @Suppress("UNCHECKED_CAST")
        fun getNodeMappings(jsonString: StringBuilder, floor: Int) : List<JsonObject> {
            val jsonArray = Parser.default().parse(jsonString) as JsonArray<JsonObject>

            return jsonArray.filter {
                it.int("floor") == floor
            }
        }

    }
}