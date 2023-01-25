package com.example.recordaudio

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.example.recordaudio.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fileName: String = ""

    private  var recorder: MediaRecorder? = null
    private var db: Double = 0.0
    private var isRecording = false
    private var job: Job? = null


    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
            Toast.makeText(this@MainActivity,"녹음 시작",Toast.LENGTH_LONG).show()
            start()
        }
    }

    private fun stopRecording() {
        isRecording = false
        job?.cancel()
        recorder?.apply {
            stop()
            release()
        }
        Toast.makeText(this@MainActivity,"녹음 중지",Toast.LENGTH_LONG).show()
        recorder = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        setContentView(binding.root)


        //start버튼 클릭
        binding.start.setOnClickListener {
            startRecording()
            getDb()
            binding.valueText.text = db.toString()

        }
        //end 버튼클릭시
        binding.end.setOnClickListener {
            stopRecording()
        }



    }
    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
    private fun getDb(){
        recorder?.let {
            isRecording = true
            job = CoroutineScope(Dispatchers.Default).launch {
                while (isRecording) {
                    delay(1000L)
                    val amplitude = it.maxAmplitude
                    db = 20 * kotlin.math.log10(amplitude.toDouble())
                }
            }

        }


    }
}





