package com.example.dajho.pos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.dajho.R

/**
 * Actividad a pantalla completa para el escáner de códigos QR y de barras (Fase 3).
 * Interfaz profesional similar a aplicaciones bancarias y de comercio.
 */
class QrScannerActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnFlashlight: ImageButton
    private var camera: Camera? = null
    private var isTorchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        val btnClose = findViewById<ImageButton>(R.id.btnCloseScanner)
        viewFinder = findViewById(R.id.viewFinder)
        btnFlashlight = findViewById<ImageButton>(R.id.btnFlashlight)
        val btnGallery = findViewById<ImageButton>(R.id.btnGallery)

        btnClose.setOnClickListener { finish() }

        startCamera()

        // Tocar el visor de la cámara simula la lectura exitosa del código (ej. CAM-001)
        viewFinder.setOnClickListener {
            returnScannedResult("CAM-001")
        }

        btnFlashlight.setOnClickListener {
            toggleFlashlight()
        }

        btnGallery.setOnClickListener {
            Toast.makeText(this, "Seleccionar imagen de galería (Simulado)", Toast.LENGTH_SHORT).show()
            returnScannedResult("ZAP-042")
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
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (_: Exception) {
                // Fallback si la cámara no está disponible
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleFlashlight() {
        val cam = camera
        if (cam != null && cam.cameraInfo.hasFlashUnit()) {
            isTorchOn = !isTorchOn
            cam.cameraControl.enableTorch(isTorchOn)
            btnFlashlight.alpha = if (isTorchOn) 1.0f else 0.7f
            Toast.makeText(this, if (isTorchOn) "Linterna encendida" else "Linterna apagada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "El dispositivo no cuenta con flash", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnScannedResult(code: String) {
        val intent = Intent().apply {
            putExtra(EXTRA_SCAN_RESULT, code)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val EXTRA_SCAN_RESULT = "extra_scan_result"
    }
}
