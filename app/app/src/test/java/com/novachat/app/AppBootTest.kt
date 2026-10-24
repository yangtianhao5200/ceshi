package com.novachat.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File
import java.io.FileOutputStream

/** Boots the real MainActivity (view model + encrypted prefs + Compose tree). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppBootTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun appBootsAndRendersWelcomeScreen() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
        ShadowLooper.idleMainLooper()
        composeRule.waitForIdle()

        val view: View = activity.window.decorView
        val width = if (view.width > 0) view.width else 1170
        val height = if (view.height > 0) view.height else 2532
        if (view.width == 0 || view.height == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, width, height)
        }
        assertTrue("decor view should have children", (view as? android.view.ViewGroup)?.childCount ?: 0 > 0)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val outDir = File("/workspace/dist/screens").apply { mkdirs() }
        FileOutputStream(File(outDir, "app_boot.png")).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        controller.pause().stop().destroy()
    }
}
