sed -i '/overlayView = ComposeView(this).apply {/a\
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
' app/src/main/java/com/example/service/DynamicIslandOverlayService.kt
