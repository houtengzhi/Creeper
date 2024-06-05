package com.cloud.creeper.ui.integration

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cloud.creeper.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider

/**
 *
 * Created by cloud on 2024/5/31.
 */

private const val TAG = "AuthorizationPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorizationPage(onUpClick: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick)
        }
    ) { contentPadding ->
        AuthorizationScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()))

    }
}

@Composable
fun AuthorizationScreen(modifier: Modifier = Modifier) {

    Column {

        val context = LocalContext.current

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 24.dp, top = 12.dp, end = 24.dp)
                .clickable(onClick = {
                    authorize(context as Activity)
                })) {
            Icon(painter = painterResource(R.drawable.github_mark),
                contentDescription = "stopped icon",
                modifier = Modifier
                    .padding(start = 24.dp, top = 12.dp, bottom = 12.dp)
                    .size(24.dp, 24.dp),
                tint = LocalContentColor.current
            )

            Text(text = "Github",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
                textAlign = TextAlign.Center)
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                                     modifier: Modifier = Modifier, onUpClick: () -> Unit) {
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.Authorize))
    },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onUpClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
        },
        scrollBehavior = scrollBehavior)
}

fun authorize(activity: Activity) {
    Log.d(TAG, "authorize()")
    val provider = OAuthProvider.newBuilder("github.com")
    provider.scopes = arrayListOf("read:user", "gist")
    val authResult = FirebaseAuth.getInstance().pendingAuthResult
    if (authResult != null) {
        authResult.addOnSuccessListener {
            val credential = it.credential
            if (credential is OAuthCredential) {
                val accessToken = credential.accessToken
            }
        }
            .addOnFailureListener {

            }
            .addOnCanceledListener {

            }
    } else {
        FirebaseAuth.getInstance().startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
            .addOnCanceledListener {

            }
    }
}