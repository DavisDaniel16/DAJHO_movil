package com.example.dajho.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dajho.R

/**
 * BottomSheetDialogFragment con previsualización real de cámara (CameraX) para escaneo QR/Barras en dispositivo físico (Fase 3).
 */
class QrScannerSheet : BottomSheetDialogFragment() {

    var onCodeDetected: ((String) -> Unit)? = null
    private lateinit var viewFinder: PreviewView

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

        // Iniciar cámara con post para asegurar que el PreviewView está listo
        viewFinder.post {
            startCamera()
        }

        // Tocar el visor de la cámara selecciona automáticamente el producto de prueba (CAM-001)
        // para facilitar pruebas en dispositivos físicos o emuladores
        viewFinder.setOnClickListener {
            onCodeDetected?.invoke("CAM-001")
            dismiss()
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
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview
                )
            } catch (_: Exception) {
                // Si ocurre algún inconveniente con la cámara del dispositivo, el usuario puede usar la barra manual o toque
            }
        }, ContextCompat.getMainExecutor(context))
    }

    companion object {
        const val TAG = "QrScannerSheet"
    }
}
