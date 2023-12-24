package com.materialcentral.container.client

/**
 * A listener that receives download events from RegistryClient as a layer is being downloaded. These callbacks are inline with the download thread
 */
interface LayerDownloadListener {

    /**
     * This function will be periodically called during the download process to determine if the download should be cancelled.
     */
    fun cancelUpload(): Boolean = false

    /**
     * The download of the layer has started.
     *
     * @param digest The digest of the layer being downloaded.
     */
    fun onLayerDownloadStart(digest: String)

    /**
     * Progress update on the number of bytes downloaded so far.
     *
     * @param digest The digest of the layer being downloaded.
     * @param bytesDownloaded The bytes downloaded so far.
     * @param totalBytes The total bytes to be downloaded.
     */
    fun onLayerDownloadProgress(digest: String, bytesDownloaded: Long, totalBytes: Long)

    /**
     * The layer download could not complete due to some error.
     *
     * @param digest The digest of the layer being downloaded.
     * @param bytesDownloaded The total bytes downloaded before the error occurred.
     * @param totalBytes The total bytes to be downloaded if known at the time of the error.
     */
    fun onLayerDownloadError(digest: String, bytesDownloaded: Long, totalBytes: Long?)

    /**
     * The layer download could not complete due to some error.
     *
     * @param digest The digest of the layer being downloaded.
     * @param bytesDownloaded The bytes downloaded before it was cancelled.
     * @param totalBytes The total bytes to be downloaded.
     */
    fun onLayerDownloadCancelled(digest: String, bytesDownloaded: Long, totalBytes: Long)

    /**
     * The layer was successfully downloaded.
     *
     * @param digest The digest of the layer being downloaded.
     * @param bytesDownloaded The total bytes of the layer that was downloaded.
     */
    fun onLayerDownloadComplete(digest: String, bytesDownloaded: Long)
}