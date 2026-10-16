package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.model.IslandExpansionMode
import com.example.model.LiveActivityState
import com.example.ui.components.DynamicIslandPill
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.GlobalIslandState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DynamicIslandAccessibilityService : AccessibilityService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var overlayLayoutParams: WindowManager.LayoutParams? = null

    private var currentForegroundPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        GlobalIslandState.initPersistence(applicationContext)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Configure AccessibilityServiceInfo explicitly for TYPE_WINDOW_STATE_CHANGED
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 100
        this.serviceInfo = info

        GlobalIslandState.setAccessibilityServiceConnected(true)
        showAccessibilityOverlayWindow()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // Ignore system UI overlays, volume sliders, input methods, and self
        if (pkg == "com.android.systemui" || pkg.contains("inputmethod") || pkg.contains("volume") || pkg == packageName) return

        val wasInSpotify = currentForegroundPackage == "com.spotify.music"
        val isNowInSpotify = pkg == "com.spotify.music"
        currentForegroundPackage = pkg

        if (!wasInSpotify && isNowInSpotify) {
            // User opened Spotify -> Smoothly retract/hide the media pill
            GlobalIslandState.onSpotifyEnteredForeground()
        } else if (wasInSpotify && !isNowInSpotify) {
            // User swiped home or switched away from Spotify -> Launch Center-to-Notch morph!
            val isPlaying = GlobalIslandState.mediaActivityState.value?.isPlaying == true
            if (isPlaying) {
                GlobalIslandState.startCenterToTopFlight()
            } else {
                GlobalIslandState.onSpotifyExitedToBackground()
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupt callback
    }

    private fun showAccessibilityOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val density = resources.displayMetrics.density
        val viewModel = DynamicIslandViewModel()
        val initialOffsetYDp = viewModel.pillOffsetYDp.value

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (initialOffsetYDp * density).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        overlayLayoutParams = params

        val composeView = ComposeView(this).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setViewTreeLifecycleOwner(this@DynamicIslandAccessibilityService)
            setViewTreeViewModelStoreOwner(this@DynamicIslandAccessibilityService)
            setViewTreeSavedStateRegistryOwner(this@DynamicIslandAccessibilityService)
            setContent {
                MyApplicationTheme {
                    val activeActivity = viewModel.activeActivity.collectAsState().value
                    val secondaryActivity = viewModel.secondaryActivity.collectAsState().value
                    val expansionMode = viewModel.expansionMode.collectAsState().value
                    val settings = viewModel.settings.collectAsState().value
                    val activeNotification = viewModel.activeNotification.collectAsState().value

                    LaunchedEffect(activeActivity, activeNotification) {
                        if ((activeActivity !is LiveActivityState.Idle || activeNotification != null) && expansionMode == IslandExpansionMode.HIDDEN) {
                            val isSnoozedVideo = (activeActivity as? LiveActivityState.Music)?.let {
                                GlobalIslandState.dismissedVideoToken != null &&
                                        (it.song.title == GlobalIslandState.dismissedVideoToken || GlobalIslandState.dismissedVideoToken == "current_video")
                            } ?: false

                            if (!isSnoozedVideo && !GlobalIslandState.isSpotifyInForeground.value) {
                                viewModel.setExpansionMode(IslandExpansionMode.COMPACT)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopCenter),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DynamicIslandPill(
                            activeActivity = activeActivity,
                            secondaryActivity = secondaryActivity,
                            expansionMode = expansionMode,
                            settings = settings,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        overlayComposeView = composeView

        composeView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                detectHardwareCutout(v)
            }
            override fun onViewDetachedFromWindow(v: View) {}
        })

        composeView.setOnApplyWindowInsetsListener { v, insets ->
            detectHardwareCutout(v)
            insets
        }

        try {
            windowManager?.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Y-Offset Synchronization
        lifecycleScope.launch {
            viewModel.pillOffsetYDp.collectLatest { offsetDp ->
                val displayDensity = resources.displayMetrics.density
                params.y = (offsetDp * displayDensity).toInt()
                if (composeView.isAttachedToWindow) {
                    try {
                        windowManager?.updateViewLayout(composeView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Center-to-Notch Flight: Dynamic Flight Canvas
        lifecycleScope.launch {
            GlobalIslandState.isCenterToNotchFlightActive.collectLatest { isFlightActive ->
                if (isFlightActive) {
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.MATCH_PARENT
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                } else {
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                }
                if (composeView.isAttachedToWindow) {
                    try {
                        windowManager?.updateViewLayout(composeView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun detectHardwareCutout(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cutout = view.rootWindowInsets?.displayCutout
            val rect = cutout?.boundingRects?.firstOrNull()
            if (rect != null) {
                val density = resources.displayMetrics.density
                val cutoutTopDp = rect.top / density
                val cutoutCenterYDp = rect.centerY() / density
                GlobalIslandState.updateHardwareCutout(cutoutTopDp, cutoutCenterYDp)
            }
        }
    }

    override fun onDestroy() {
        GlobalIslandState.setAccessibilityServiceConnected(false)
        if (overlayComposeView != null) {
            try {
                overlayComposeView?.disposeComposition()
                windowManager?.removeView(overlayComposeView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayComposeView = null
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}
