package com.materialcentral.container.oci.layout

import com.materialcentral.container.oci.configuration.ImageConfiguration
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestLayer
import java.io.File

class OCILayoutEntry(val manifest: Manifest, val manifestFile: File, val configuration: ImageConfiguration?, val configurationFile: File?, val layersFiles: List<Pair<ManifestLayer, File>>) {
}