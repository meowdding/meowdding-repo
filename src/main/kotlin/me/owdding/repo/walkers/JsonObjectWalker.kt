package me.owdding.repo.walkers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.repo.ConfigContext
import java.util.*

class JsonObjectWalker(
    private val origin: JsonObject,
    private val configContext: ConfigContext,
    val list: LinkedList<String> = LinkedList(),
) : DataWalker {
    override fun process(): JsonObject {
        val output = JsonObject()

        origin.entrySet().forEach { (key, value) ->
            fun path() = (list.joinToString(".", postfix = ".").takeUnless { it.length == 1 } ?: "") + key
            if (configContext.isIncluded(path())) {
                println(path() + " is included!")
                output.add(key, value)
                return@forEach
            }
            if (configContext.isExcluded(path())) {
                println(path() + " is excluded!")
                return@forEach
            }
            when (value) {
                is JsonObject -> {
                    list.addLast(key)
                    val process = JsonObjectWalker(value, configContext, list).process()
                    if (configContext.isInvalid(process)) {
                        output.add(key, process)
                    }
                    list.removeLast()
                    return@forEach
                }

                is JsonArray -> {
                    list.addLast(key)
                    val process = JsonArrayWalker(value, configContext, list).process()
                    if (configContext.isInvalid(process)) {
                        output.add(key, process)
                    }
                    list.removeLast()
                }

                else -> return@forEach
            }
        }

        return output
    }
}