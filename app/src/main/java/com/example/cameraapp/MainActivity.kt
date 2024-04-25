package com.example.cameraapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.cameraapp.databinding.ActivityMainBinding
import com.example.cameraapp.ui.theme.CameraAppTheme
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import android.widget.TextView



class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var barcodeScanner: BarcodeScanner

    private lateinit var cameraController:LifecycleCameraController
    //private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!hasPermissions(baseContext)){
            activityResultlauncher.launch(REQUIRED_PERMISSIONS.toTypedArray())
        }
        else{
            startCamera()
        }
    }

    private fun startCamera() {
        val previewView: PreviewView = viewBinding.viewFinder
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
        var qrContent : String =""
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_128)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if ((barcodeResults == null) ||
                    (barcodeResults.size == 0) ||
                    (barcodeResults.first() == null)
                ) {
                    previewView.overlay.clear()
                    previewView.setOnTouchListener { _, _ -> false } //no-op
                    return@MlKitAnalyzer
                }

                if (barcodeResults[0]!=null){
                    qrContent= barcodeResults[0].displayValue.toString()
                    val textValue = findViewById<TextView>(R.id.myTextView)
                    textValue.text=qrContent
                }


            }
        )

    }




    private val activityResultlauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {permissions ->
            var permissionGranted = true
            permissions.entries.forEach{
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted=false
            }
            if (permissionGranted == false){
                Toast.makeText(this,"Permission access denied",Toast.LENGTH_LONG).show()
            }
            else{
                startCamera()
            }

        }

    companion object {
        private const val TAG = "Camera App"
        private const val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS=
            listOf(
                android.Manifest.permission.CAMERA
            )
        fun hasPermissions(context: Context) = Companion.REQUIRED_PERMISSIONS.all{
            ContextCompat.checkSelfPermission(context,it) == PackageManager.PERMISSION_GRANTED
        }
    }

}