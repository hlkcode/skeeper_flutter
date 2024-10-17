package com.hippo.skeeper_flutter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import android.widget.Toast
import com.hippo.skeeper_flutter.Message.Companion.ACTION_CLOSE
import com.hippo.skeeper_flutter.Message.Companion.ID_AUDIO
import com.hippo.skeeper_flutter.Message.Companion.NOITF_ID_AUDIO
import com.hippo.skeeper_flutter.Message.Companion.STATUS_AUDIO_STOPPED
import com.punchthrough.blestarterappandroid.ble.ConnectionManager
import com.smartsound.skeeper.*
import com.smartsound.skeeper.AudioProcessing.BUF_TYPE_SPEAKER
import kotlinx.coroutines.*


@Suppress("DEPRECATION")
class ClientPlayer(private val context: Context) {

    private val bufferSize by lazy { 8000 * 60 + (8000 * 10) }
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val buffer by lazy { ShortArray(bufferSize) }
    private var playBuffer = ShortArray(bufferSize * 2)

    public var bufferPcm: ShortArray = ShortArray(0)
    public var bufferHeadset: ShortArray = ShortArray(0)
    public var bufferSpaker: ShortArray = ShortArray(0)

    private var bufferIndex: Int = 0
    private var playIndex: Int = 0
    private var sumWrite: Int = 0

    private val audioTrack by lazy {
        val sr = 8000
        AudioTrack(
            AudioManager.STREAM_MUSIC,
            sr,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            playSize,
            AudioTrack.MODE_STREAM
        )
    }
    private val playSize: Int = 256
//    private val playSize by lazy {
//        val sr = 8000
//        AudioTrack.getMinBufferSize(
//            sr, AudioFormat.CHANNEL_OUT_MONO,
//            AudioFormat.ENCODING_PCM_16BIT
//        )
//    }

    private lateinit var device: BluetoothDevice
    private var path: Int = PATH_SPEAKER
    private var isRunning = true

    @ExperimentalUnsignedTypes
    fun create(device: BluetoothDevice, mode: Int, path: Int) {
        this.device = device
        this.path = path

        bufferPcm = ShortArray(0)
        bufferHeadset = ShortArray(0)
        bufferSpaker = ShortArray(0)

        bufferIndex = 0
        playIndex = 0

        this.device = device
        this.path = path
        sumWrite = 0

        AudioProcessing.create(mode)
        Response.registerEventListener(eventListener)

        audioTrack.play()

        bufferIndex = 0
        playIndex = 0

        isRunning = true

        Toast.makeText(context.applicationContext, "Path: $path, Mode: $mode", Toast.LENGTH_SHORT)
            .show()

        coroutineScope.launch {
            while (isRunning) {
                runBlocking {
                    var remains = bufferIndex - playIndex
                    if (remains < 0) remains += buffer.size

                    if (remains > playSize) {

                        //Timber.i("bufferIndex $bufferIndex, playIndex $playIndex")
                        logInfo("ClientPlayer => bufferIndex $bufferIndex, playIndex $playIndex")

                        audioTrack.write(buffer, playIndex, playSize)

                        /** Update play index */
                        playIndex += playSize
                        if (playIndex >= buffer.size) {
                            playIndex = 0
                        }
                    }
                }
            }
        }
//        coroutineScope.launch {
//            // LLog.debug("audio coroutine")
//
//            var prevTs = System.currentTimeMillis()
//            var filled = 0
//            var playShortSize = playSize / 2  //short로
//
//
//            while (isRunning) {
//
//                if (audioTrack.playState == AudioTrack.PLAYSTATE_PAUSED) {
//                    delay(1)
//                    continue
//                }
//
//                while (filled < playShortSize && isRunning) {
//
//
//
//                    var remains = bufferIndex - playIndex
//
//                    if (remains < 0) {
//                        remains += buffer.size
//                    }
//
//                    if (remains < (playShortSize - filled)) {
//                        continue
//                    }
//                    synchronized(buffer) {
//                        var writeShortSize = playShortSize - filled
//                        buffer.copyInto(playBuffer, filled, playIndex, playIndex + writeShortSize)
//
//                        playIndex += writeShortSize
//                        filled += writeShortSize
//
//                        if (playIndex > buffer.size - 1) {
//                            playIndex = 0
//                        }
//                    }
//
//                    //LLog.debug("remains:$remains")
//                }
//
//                if (isRunning == false) {
//                    break
//                }
//
//                val ret = audioTrack.write(playBuffer, 0, filled)
//                sumWrite += ret
//                listener?.onCompleteEnqueue(ret, sumWrite)
//                filled -= ret
//
//                //LLog.debug("remains:$remains")
//                delay(1)
//            }
////            LLog.debug("audio coroutine end")
//
//        }
    }

    @ExperimentalUnsignedTypes
    fun destroy() {

        isRunning = false
        Response.unregisterEventListener(eventListener)
        coroutineScope.coroutineContext.cancelChildren()
        AudioProcessing.destroy()

        val audio = AudioProcessing.getBuffer(BUF_TYPE_SPEAKER)
        //activity.log("Get buffer size: ${audio?.size}")
        logInfo("ClientPlayer => Get buffer size: ${audio?.size}")

        // https://stackoverflow.com/questions/23324943/unable-to-retrieve-audiotrack-pointer-for-write
        if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED) {
            if (audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                try {
                    audioTrack.stop()

                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                audioTrack.release()
            }
        }
    }

    fun clear() {

        pausePlay()

        bufferIndex = 0
        playIndex = 0

        isRunning = true

        bufferPcm = ShortArray(0)
        bufferHeadset = ShortArray(0)
        bufferSpaker = ShortArray(0)


    }

    fun pausePlay() {

        if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED) {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                try {
                    audioTrack.pause()
                } catch (e: IllegalStateException) {

                }

            }
        }
    }

    fun continuePlay() {

        if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED) {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PAUSED) {
                try {
                    audioTrack.play()
                } catch (e: IllegalStateException) {

                }

            }
        }
    }

    @ExperimentalUnsignedTypes
    private val eventListener by lazy {
        EventListener().apply {
            onAudioEvent2 =
                {speaker: ShortArray, earjack: ShortArray, pcm: ShortArray, filtered: FloatArray ->

                    // LLog.debug("On Audio Event2")
                    //버퍼에 넣기 전에 항상 체크하도록 수정함.
//                    if (App.INSTANCE.heasetPluggedIn == true)
//                        path = PATH_EARJACK
//                    else
                    path = PATH_SPEAKER

                    while (bufferIndex - playIndex > playSize * 2 && isRunning) {
                        Thread.sleep(1)
                    }

                    if (isRunning) {
                        path = com.smartsound.skeeper.PATH_SPEAKER

                        synchronized(buffer) {
                            bufferIndex += if (path == com.smartsound.skeeper.PATH_SPEAKER) {
                                speaker.copyInto(buffer, bufferIndex, 0, speaker.size)
                                speaker.size
                            } else {
                                earjack.copyInto(buffer, bufferIndex, 0, earjack.size)
                                earjack.size
                            }

                            if (bufferIndex > bufferSize - 1) {
                                bufferIndex = 0
                            }
                        }
                        listener?.onAudio(filtered)
                    }
                }

            onVitalEvent = { bpm: Int, dB: Int, regularity: Int, beatCount: Int, err: Int ->
                //Timber.i("bpm $bpm, dB $dB, regularity $regularity, err $err")
                //LLog.debug("bpm $bpm, dB $dB, regularity $regularity, beatCount:$beatCount")
                //  LLog.debug("On Vital Event")
                logInfo("ClientPlayer => bpm $bpm, dB $dB, regularity $regularity, err $err")
                listener?.onVitalEvent(bpm, dB, regularity, beatCount, err)


            }
            onNotificationEvent = { id: Int, status: Int ->
                if (id == NOITF_ID_AUDIO && status == STATUS_AUDIO_STOPPED) {
                    Log.i("test", "onNotificationEvent")
//                    LLog.debug("onNotificationEvent")
                    val payload =
                        Request.getPayload(ID_AUDIO, ACTION_CLOSE)
                    ConnectionManager.skeeperWrite?.let { characteristic ->
                        ConnectionManager.writeCharacteristic(device!!, characteristic, payload)
                    }
                    destroy()
                }
            }
            onError = {
                logError("ClientPlayer => err: id ${it.id}, action ${it.action}, err ${it.error}")
//                LLog.debug("err: id ${it.id}, action ${it.action}, err ${it.error}")
            }
        }
    }

    interface ClientPlayerListener {
        fun onVitalEvent(bpm: Int, dB: Int, regularity: Int, beatCount: Int, err: Int)
        fun onAudio(audio: FloatArray)
        fun onCompleteEnqueue(enqueueSize: Int, sumWrite: Int)
    }

    private var listener: ClientPlayerListener? = null

    fun setListener(listener: ClientPlayerListener?) {
        this.listener = listener
    }

}