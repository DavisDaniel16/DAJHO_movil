package com.example.dajho.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.dajho.R
import java.util.concurrent.Executors

/**
 * BottomSheetDialogFragment con escáner real de cámara (CameraX + ML Kit) para escaneo QR/Barras (Fase 12).
 */
class QrScannerSheet : BottomSheetDialogFragment() {

    var onCodeDetected: ((String) -> Unit)? = null
    private lateinit var viewFinder: PreviewView

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isProcessingFrame = false
    private var lastScannedCode: String? = null
    private var lastScannedTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_qr_scanner, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        val btnClose: ImageButton = v.findViewById(R.id.btnCloseScanner)
        viewFinder = v.findViewById(R.id.viewFinder)

        btnClose.setOnClickListener { dismiss() }

        viewFinder.post {
            startCamera()
        }
    }

    private fun startCamera() {
        val context = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
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
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Cámara no disponible: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))
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
                            if (rawValue != lastScannedCode || (currentTime - lastScannedTime) > 1500L) {
                                lastScannedCode = rawValue
                                lastScannedTime = currentTime

                                activity?.runOnUiThread {
                                    onCodeDetected?.invoke(rawValue)
                                    dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    companion object {
        const val TAG = "QrScannerSheet"
    }
}
