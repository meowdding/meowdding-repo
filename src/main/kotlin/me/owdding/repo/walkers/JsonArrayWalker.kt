package me.owdding.repo.walkers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.repo.ConfigContext
import java.util.*

class JsonArrayWalker(
    private val origin: JsonArray,
    private val configContext: ConfigContext,
    val list: LinkedList<String> = LinkedList(),
) : DataWalker {
    override fun process(): JsonArray {
        val output = JsonArray()

        origin.forEach {
            when (it) {
                is JsonObject -> {
                    val processed = JsonObjectWalker(it, configContext, list).process()
                    if (configContext.isInvalid(processed)) return@forEach
                    output.add(processed)
                }

                is JsonArray -> {
                    val processed = JsonArrayWalker(it, configContext, list).process()
                    if (configContext.isInvalid(processed)) return@forEach
                    output.add(processed)
                }

                else -> output.add(it)
            }
        }

        return output
    }
}