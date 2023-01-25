package com.example.recordaudio

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.example.recordaudio.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fileName: String = ""

    private  var recorder: MediaRecorder? = null
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
            setAudioSource(MediaRecorder.AudioSource.MIC)//외부에서 들어오는 소리를 녹음
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)// 출력 파일 포맷을 설정
            setOutputFile(fileName) // 출력파일 이름 설정
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)// 오디오 인코더를 설정
            try {
                prepare() // 초기화
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed") // 실패시
            }
            Toast.makeText(this@MainActivity,"녹음 시작",Toast.LENGTH_LONG).show()
            start() // 녹음 시작
        }
    }

    private fun stopRecording() {
        isRecording = false
        job?.cancel()
        recorder?.apply {
            stop() //녹음 중지
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
            startRecording() // 녹음 시작
            getDb() //데시벨 측정

        }
        //end 버튼클릭시
        binding.end.setOnClickListener {
            stopRecording()// 녹음중지
        }



    }
    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
    //데시벨 측정 함수
    private fun getDb(){
        recorder?.let {
            isRecording = true
            //녹음이 중지 될 때 까지 작업 실행 텍스트에 데시벨 값 할당.
            job = CoroutineScope(Dispatchers.Main).launch {
                while (isRecording) {
                    delay(1000L) // 1초마다 데시벨 측정
                    val amplitude = it.maxAmplitude
                    binding.valueText.text = (20 * kotlin.math.log10(amplitude.toDouble())).toString()
                }
            }

        }


    }
}





