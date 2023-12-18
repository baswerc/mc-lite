package com.materialcentral.container.image.sbom

import com.materialcentral.container.image.ContainerImage
import com.materialcentral.container.image.ContainerImageCoordinates
import com.materialcentral.sbom.SpdxExporter


object ContainerImageSpdxExporter : SpdxExporter<ContainerImageCoordinates>() {

}