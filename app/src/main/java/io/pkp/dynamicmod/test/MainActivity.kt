package io.pkp.dynamicmod.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

const val DYNAMIC_MODULE = "dynamicfeature"
const val TAG = "Hello"

class MainActivity : AppCompatActivity() {
    lateinit var splitInstallManager: SplitInstallManager
    private var sessionId = 0
    lateinit var messageView: TextView

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    messageView.append("\nDynamic module downloading.. (state listener)")
                }
                SplitInstallSessionStatus.DOWNLOADED -> {
                    messageView.append("\nDynamic module download successful (state listener)")
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    messageView.append("\nDynamic module installed successfully (state listener)")
                    readDynamicModule()
                }
                SplitInstallSessionStatus.FAILED -> {
                    messageView.append("\n\nModule install failed with ${state.errorCode()} (state listener)")
                }
                else -> {
                    Log.d(TAG, "Status: ${state.status()}")
                    messageView.append("\nStatus: ${state.status()} (state listener)")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        splitInstallManager = SplitInstallManagerFactory.create(application)
        splitInstallManager.registerListener(listener)

        messageView = this.findViewById(R.id.message)

        messageView.append("\nChecking module..")

        if (isDynamicInstalled()) {
            readDynamicModule()
        } else {
            requestDynamicInstall()
        }
    }

    private fun readDynamicModule() {
        messageView.append("\nReading from module..")
        try {
            val lazyImpl = Class
                .forName("io.pkp.dynamicmod.dynamicfeature.LazyClass")
                .newInstance() as LazyInterface
            messageView.append("\n${lazyImpl.message()}")
        } catch (e: Exception) {
            messageView.append("\nError reading from module: $e")
        }
    }

    private fun isDynamicInstalled() = splitInstallManager.installedModules.contains(DYNAMIC_MODULE)

    private fun requestDynamicInstall() {
        messageView.append("\nInstalling module..")

        val request = SplitInstallRequest
                        .newBuilder()
                        .addModule(DYNAMIC_MODULE)
                        .build()

        splitInstallManager
                .startInstall(request)
                .addOnSuccessListener { id ->
                    sessionId = id
                    messageView.append("\nStart successful (start listener)")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error installing module: ", exception)
                    messageView.append("\nError starting install module: $exception (start listener)")
                }
    }
}