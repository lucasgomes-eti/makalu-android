package eti.lucasgomes.makalu.components.imagePreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import eti.lucasgomes.makalu.components.R
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
internal fun ImagePreviewScreen() {
    ConfigureTopBar(title = "Image preview")
    Image(
        painter = painterResource(R.drawable.profile_pic),
        contentDescription = "Profile picture",
        modifier = Modifier
            .fillMaxSize()
            .zoomable(rememberZoomableState())
    )
}