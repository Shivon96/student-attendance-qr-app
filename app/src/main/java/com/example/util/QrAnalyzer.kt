package com.example.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()
    private var lastScannedTimestamp = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        // Throttle scans by 1.8 seconds between triggers
        if (currentTimestamp - lastScannedTimestamp < 1800) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image
        if (image != null) {
            try {
                val planes = image.planes
                val yBuffer: ByteBuffer = planes[0].buffer
                val ySize = yBuffer.remaining()
                val yData = ByteArray(ySize)
                yBuffer.get(yData)

                val width = imageProxy.width
                val height = imageProxy.height

                val source = PlanarYUVLuminanceSource(
                    yData,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                val result = reader.decodeWithState(binaryBitmap)
                if (result != null && result.text.isNotBlank()) {
                    lastScannedTimestamp = currentTimestamp
                    onQrCodeScanned(result.text)
                }
            } catch (_: Exception) {
                // Frame didn't contain readable QR, continue
            } finally {
                reader.reset()
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}
