package com.example.dajho.pos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.dajho.R
import java.util.concurrent.Executors

/**
 * Actividad de escáner de códigos QR y de barras con CameraX y Google ML Kit (Fase 12).
 * Soporta QR, EAN-13, EAN-8, UPC-A, UPC-E y Code 128 con desrebote (debounce).
 */
class QrScannerActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnFlashlight: ImageButton
    private var camera: Camera? = null
    private var isTorchOn = false

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isProcessingFrame = false
    private var lastScannedCode: String? = null
    private var lastScannedTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        val btnClose = findViewById<ImageButton>(R.id.btnCloseScanner)
        viewFinder = findViewById(R.id.viewFinder)
        btnFlashlight = findViewById<ImageButton>(R.id.btnFlashlight)

        btnClose.setOnClickListener { finish() }

        startCamera()

        btnFlashlight.setOnClickListener {
            toggleFlashlight()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_CODABAR
                    )
                    .build()

                val scanner = BarcodeScanning.getClient(options)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(scanner, imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (ex: Exception) {
                Toast.makeText(this, "Cámara no disponible: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(scanner: BarcodeScanner, imageProxy: ImageProxy) {
        if (isProcessingFrame) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            isProcessingFrame = true
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            val currentTime = System.currentTimeMillis()
                            // Control de desrebote para evitar lecturas repetidas consecutivas (1.5s)
                            if (rawValue != lastScannedCode || (currentTime - lastScannedTime) > 1500L) {
                                lastScannedCode = rawValue
                                lastScannedTime = currentTime

                                runOnUiThread {
                                    returnScannedResult(rawValue)
                                }
                                break
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    isProcessingFrame = false
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun toggleFlashlight() {
        val cam = camera
        if (cam != null && cam.cameraInfo.hasFlashUnit()) {
            isTorchOn = !isTorchOn
            cam.cameraControl.enableTorch(isTorchOn)
            btnFlashlight.alpha = if (isTorchOn) 1.0f else 0.7f
            Toast.makeText(this, if (isTorchOn) "Linterna encendida" else "Linterna apagada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "El dispositivo no cuenta con linterna", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnScannedResult(code: String) {
        val intent = Intent().apply {
            putExtra(EXTRA_SCAN_RESULT, code)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val EXTRA_SCAN_RESULT = "extra_scan_result"
    }
}
