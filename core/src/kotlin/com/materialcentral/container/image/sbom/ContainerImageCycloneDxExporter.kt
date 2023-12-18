package com.materialcentral.container.image.sbom

import com.materialcentral.container.image.ContainerImage
import com.materialcentral.container.image.ContainerImageCoordinates
import com.materialcentral.sbom.CycloneDxExporter
import org.cyclonedx.model.Bom
import java.io.FileOutputStream

object ContainerImageCycloneDxExporter : CycloneDxExporter<ContainerImageCoordinates>() {
    override fun toBom(value: ContainerImageCoordinates): Bom {
        TODO("Not yet implemented")
    }
}