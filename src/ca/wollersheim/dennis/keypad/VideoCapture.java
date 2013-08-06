package ca.wollersheim.dennis.keypad;


import java.io.File;
import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.widget.VideoView;
/**
 * Android video recorder with "no" preview (the preview is a 1x1 pixel which
 * simulates an unobtrusive recording led). Based on Pro Android 2 2010 (Hashimi
 * et al) source code in Listing 9-6. 
 * 
 * Also demonstrates how to use the front-facing and back-facing cameras. 
 * A calling Intent can pass an Extra to use the front facing camera if available.
 * 
 * Suitable use cases: 
 * A: eye gaze tracking library to let users use eyes as a mouse to navigate a web page 
 * B: use tablet camera(s) to replace video camera in lab experiments
 * (psycholingusitics or other experiments)
 * 
 * Video is recording is controlled in two ways: 
 * 1. Video starts and stops with the activity 
 * 2. Video starts and stops on any touch
 * 
 * To control recording in other ways see the try blocks of the onTouchEvent
 * 
 * To incorporate into project add these features and permissions to
 * manifest.xml:
 * 
 * <uses-feature android:name="android.hardware.camera"/> 
 * <uses-feature android:name="android.hardware.camera.autofocus"/>
 * 
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.CAMERA" /> 
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 * 
 * Tested Date: October 2 2011 with manifest.xml 
 * <uses-sdk android:minSdkVersion="8" android:targetSdkVersion="11"/>
 */
@TargetApi(9)
public class VideoCapture  {
    //public static final String EXTRA_USE_FRONT_FACING_CAMERA ="frontcamera";
    private static final String OUTPUT_FILE = "/sdcard/videooutput";
    private static final String TAG = "RecordVideo";
    private Boolean mRecording = false;
    private Boolean mUseFrontFacingCamera = false;
    private VideoView mVideoView = null;
    private MediaRecorder mVideoRecorder = null;
    private Camera mCamera;
    private KeypadActivity context;
    private long videoStopTime = 0;
    public VideoCapture( KeypadActivity context, VideoView videoView) {
    	this.context=context;
        mVideoView = videoView;
 }

    public void oneRecord(int seconds ) {
        // can use the xy of the touch to start and stop recording

            if (!mRecording) {
                // To begin recording attach this try block to another event listener,
                // button etc
                try {
                    beginRecording(mVideoView.getHolder(), seconds);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }


    private void stopRecording() throws Exception {
        mRecording = false;
        if (mVideoRecorder != null) {
            mVideoRecorder.stop();
            mVideoRecorder.release();
            mVideoRecorder = null;
        }
        if (mCamera != null) {
            mCamera.reconnect();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Uses the surface defined in video_recorder.xml 
     * Tested using 
     * 2.2 (HTC Desire/Hero phone) -> Use all defaults works, records back facing camera with AMR_NB audio
     * 3.0 (Motorola Xoom tablet) -> Use all defaults doesn't work, works with these specs, might work with others
     * 
     * @param holder The surfaceholder from the videoview of the layout
     * @throws Exception
     */
    private void beginRecording(SurfaceHolder holder, int seconds) throws Exception {
        if (mVideoRecorder != null) {
            mVideoRecorder.stop();
            mVideoRecorder.release();
            mVideoRecorder = null;
        }
        if (mCamera != null) {
            mCamera.reconnect();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        String uniqueOutFile = OUTPUT_FILE + System.currentTimeMillis() + ".3gp";
        File outFile = new File(uniqueOutFile);
        if (outFile.exists()) {
            outFile.delete();
        }

        try {
                mCamera = Camera.open();

            // Camera setup is based on the API Camera Preview demo
            mCamera.setPreviewDisplay(holder);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(640, 480);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mCamera.unlock();

            mVideoRecorder = new MediaRecorder();
            mVideoRecorder.setCamera(mCamera);

            // Media recorder setup is based on Listing 9-6, Hashimi et all 2010
            // values based on best practices and good quality, 
            // tested via upload to YouTube and played in QuickTime on Mac Snow Leopard
            mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mVideoRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);// THREE_GPP
            mVideoRecorder.setVideoSize(640, 480);// YouTube recommended size: 320x240,
                                                                            // OpenGazer eye tracker: 640x480
                                                                            // YouTube HD: 1280x720
//            mVideoRecorder.setVideoFrameRate(20); //might be auto-determined due to lighting
            //mVideoRecorder.setVideoEncodingBitRate(3000000);// 3 megapixel, or the max of
                                                                                                // the camera
            mVideoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// MPEG_4_SP
            int sdk = android.os.Build.VERSION.SDK_INT;
            // Gingerbread and up can have wide band ie 16,000 hz recordings 
            // (Okay quality for human voice)
            if (sdk >= 10) {
                mVideoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                mVideoRecorder.setAudioSamplingRate(16000);
            } else {
                // Other devices only have narrow band, ie 8,000 hz 
                // (Same quality as a phone call, not really good quality for any purpose. 
                // For human voice 8,000 hz means /f/ and /th/ are indistinguishable)
                mVideoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
            mVideoRecorder.setMaxDuration(seconds * 1000); // limit to 30 seconds
            mVideoRecorder.setPreviewDisplay(holder.getSurface());
            mVideoRecorder.setOutputFile(uniqueOutFile);
            mVideoRecorder.prepare();
            mVideoRecorder.start();
            mRecording = true;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }
}
