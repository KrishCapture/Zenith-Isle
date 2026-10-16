cat > replace.txt << 'INNER'
        overlayView = ComposeView(this).apply {
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setViewTreeLifecycleOwner(this@DynamicIslandOverlayService)
INNER
sed -i 's/overlayView = ComposeView(this).apply {/overlayView = ComposeView(this).apply { \n            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)/g' app/src/main/java/com/example/service/DynamicIslandOverlayService.kt
