package com.example.scannerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scannerapp.databinding.ActivityMainBinding
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scanner: GmsBarcodeScanner
    private var isScannerInstalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        installScanner()
        val option =initializeScanner()
        scanner= GmsBarcodeScanning.getClient(this , option)
        registerListeners()
    }

    private fun registerListeners() {
        binding.btnScan.setOnClickListener {
            if (isScannerInstalled){
                startScanning()
            }else{
                Toast.makeText(this,"Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun installScanner() {
        val moduleInstall= ModuleInstall.getClient(this)
        val moduleInstallRequeste= ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(this))
            .build()

        moduleInstall.installModules(moduleInstallRequeste).addOnSuccessListener {
            isScannerInstalled= true
        }.addOnFailureListener {
            isScannerInstalled= false
            Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeScanner(): GmsBarcodeScannerOptions{
        return GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .enableAutoZoom().build()
    }

    private fun startScanning(){
        scanner.startScan().addOnSuccessListener {
            val result= it.rawValue
            result.let {
                binding.resultValueTxt.text= it
                if (result != null) {
                    if (result.contains("https://")||result.contains("http://")){
                        val intent= Intent(this, WebViewActivity::class.java)
                        intent.putExtra("url",result)
                        startActivity(intent)
                    }
                }

            }
        }.addOnCanceledListener {
            Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }
}