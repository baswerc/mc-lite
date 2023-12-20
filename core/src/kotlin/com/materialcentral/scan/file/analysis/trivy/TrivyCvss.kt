package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.geezer.flexDouble

class TrivyCvss(val index: Int, val source: String, val v2Vector: String?, val v3Vector: String?, val v2Score: Double?, val v3Score: Double?) : Comparable<TrivyCvss> {
    val availableAttributesCore: Int
        get() {
            var score = 0
            if (!v2Vector.isNullOrBlank()) {
                ++score
            }
            if (!v3Vector.isNullOrBlank()) {
                ++score
            }

            if (v2Score != null) {
                ++score
            }

            if (v3Score != null) {
                ++score
            }

            return score
        }

    override fun compareTo(other: TrivyCvss): Int {
        val score = availableAttributesCore
        val otherScore = other.availableAttributesCore
        return if (score == otherScore) {
            index.compareTo(other.index)
        } else {
            otherScore.compareTo(score)
        }
    }

    companion object {
        fun parse(json: JsonObject?): List<TrivyCvss> {
            val cvss = mutableListOf<TrivyCvss>()
            var index = 0
            json?.let { json ->
                val sources = json.keys
                for (source in sources) {
                    json.obj(source)?.let { sourceJson ->
                        cvss.add(TrivyCvss(index++, source, sourceJson.string("V2Vector"), sourceJson.string("V3Vector"), sourceJson.flexDouble("V2Score"), sourceJson.flexDouble("V3Score")))
                    }
                }
            }

            return cvss.sorted()
        }
    }
}
