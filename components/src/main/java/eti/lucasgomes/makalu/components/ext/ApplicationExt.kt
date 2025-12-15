package eti.lucasgomes.makalu.components.ext

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS

private const val APP_SETTINGS_URI_SCHEME = "package"

fun Application.openApplicationSettings() {
    startActivity(
        Intent(
            ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts(APP_SETTINGS_URI_SCHEME, packageName, null)
        ).addFlags(FLAG_ACTIVITY_NEW_TASK)
    )
}

