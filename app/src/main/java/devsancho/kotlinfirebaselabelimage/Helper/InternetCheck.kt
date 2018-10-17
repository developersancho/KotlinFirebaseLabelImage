package devsancho.kotlinfirebaselabelimage.Helper

import android.os.AsyncTask
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

class InternetCheck(private val consumer: Consumer) : AsyncTask<Void, Void, Boolean>() {

    init {
        execute()
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        try {
            val sock = Socket()
            sock.connect(InetSocketAddress("google.com", 80), 1500)
            sock.close()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        consumer.accept(result)
    }

    interface Consumer {
        fun accept(isConnected: Boolean?)
    }
}