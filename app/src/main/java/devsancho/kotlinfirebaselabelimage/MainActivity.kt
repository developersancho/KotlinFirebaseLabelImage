package devsancho.kotlinfirebaselabelimage

import android.app.AlertDialog
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions
import com.wonderkiln.camerakit.*
import devsancho.kotlinfirebaselabelimage.Helper.InternetCheck
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var waitingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setCancelable(false)
            .setMessage("Please Waiting ...")
            .build()

        btn_detect.setOnClickListener {
            camera_view.start()
            camera_view.captureImage()
        }

        camera_view.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {

            }

            override fun onEvent(p0: CameraKitEvent?) {

            }

            override fun onImage(p0: CameraKitImage?) {
                waitingDialog.show()
                var bitmap = p0!!.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, camera_view.width, camera_view.height, false)
                camera_view.stop()
                runDedector(bitmap)
            }

            override fun onError(p0: CameraKitError?) {

            }

        })
    }

    private fun runDedector(bitmap: Bitmap?) {
        var image = FirebaseVisionImage.fromBitmap(bitmap!!)
        InternetCheck(object : InternetCheck.Consumer {
            override fun accept(isConnected: Boolean?) {
                // internete bağlıysa cloud vision kullan değilse offline
                if (isConnected!!) {
                    val options = FirebaseVisionCloudDetectorOptions.Builder()
                        .setMaxResults(1)
                        .build()

                    val detector =  FirebaseVision.getInstance().getVisionCloudLabelDetector(options)
                    detector.detectInImage(image)
                        .addOnSuccessListener { result -> processResultFromCloud(result) }
                        .addOnFailureListener { e -> showError(e) }

                } else {
                    val options = FirebaseVisionLabelDetectorOptions.Builder()
                        .setConfidenceThreshold(0.8f)
                        .build()

                    val detector =  FirebaseVision.getInstance().getVisionLabelDetector(options)
                    detector.detectInImage(image)
                        .addOnSuccessListener { result -> processResultFromDevice(result) }
                        .addOnFailureListener { e -> showError(e) }
                }
            }
        })
    }

    private fun showError(e: Exception) {
        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
        Log.e(this.localClassName, e.message)
        waitingDialog.dismiss()
    }

    private fun processResultFromDevice(result: List<FirebaseVisionLabel>) {
        for(label in result)
            Toast.makeText(this, "Device result: " + label.label, Toast.LENGTH_LONG).show()
        waitingDialog.dismiss()
    }

    private fun processResultFromCloud(result: List<FirebaseVisionCloudLabel>) {
        for(label in result)
            Toast.makeText(this, "Cloud result: " + label.label, Toast.LENGTH_LONG).show()
        waitingDialog.dismiss()
    }


    override fun onResume() {
        super.onResume()
        camera_view.start()
    }

    override fun onPause() {
        super.onPause()
        camera_view.stop()
    }
}
