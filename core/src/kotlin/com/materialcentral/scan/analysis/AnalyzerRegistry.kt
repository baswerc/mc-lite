package com.materialcentral.scan.analysis

import com.materialcentral.repository.container.image.scan.analysis.*
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTargetType
import kotlin.IllegalStateException

object AnalyzerRegistry {

    private val analyzersById = mutableMapOf<String, Analyzer>()

    private val analyzersByType = mutableMapOf<ScanTargetType, MutableMap<ScanMedium, MutableList<Analyzer>>>()

    init {
        add(TrivyContainerImageAnalyzer)
        add(GrypeContainerImageAnalyzer)
        add(SyftContainerImageAnalyzer)
        add(DependencyCheckContainerImageAnalyzer)
        add(OscapContainerImageAnalyzer)
        add(TruffleHogContainerImageAnalyzer)
    }

    operator fun get(id: String?): Analyzer? {
        return id?.let { analyzersById[it] }
    }

    operator fun get(targetType: ScanTargetType, medium: ScanMedium): List<Analyzer> {
        return analyzersByType[targetType]?.get(medium) ?: listOf()
    }

    @Throws(IllegalStateException::class)
    @Synchronized
    fun add(analyzer: Analyzer) {
        val existingAnalyzer = analyzersById[analyzer.id]
        if (existingAnalyzer != null) {
            if (existingAnalyzer == analyzer) {
                return
            } else {
                throw IllegalStateException("Analyzer ${analyzer.javaClass.kotlin.qualifiedName} has same id ${analyzer.id} as analyzer ${existingAnalyzer.javaClass.kotlin.qualifiedName}")
            }
        }

        analyzersById[analyzer.id] = analyzer
        val targetTypeAnalyzers = analyzersByType.getOrPut(analyzer.targetType) { mutableMapOf() }
        for (medium in analyzer.mediums) {
            targetTypeAnalyzers.getOrPut(medium) { mutableListOf() }.add(analyzer)
        }
    }
}