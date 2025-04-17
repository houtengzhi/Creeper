package com.cloud.creeper.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.cloud.creeper.R
import com.cloud.creeper.base.VMError

@Composable
fun ErrorDialog(message: String, onDismissRequest: () -> Unit = { }) {
    AlertDialog(onDismissRequest = onDismissRequest, modifier = Modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.Dismiss))
            }
        },
        dismissButton = {

        },
        title = {
            Text(text = stringResource(id = R.string.Error))
        },
        text = {
            Text(text = message)
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = true)
    )
}