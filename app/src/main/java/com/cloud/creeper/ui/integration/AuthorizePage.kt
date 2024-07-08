package com.cloud.creeper.ui.integration

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.util.SERVICE_GITHUB
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider

/**
 *
 * Created by cloud on 2024/5/31.
 */

private const val TAG = "AuthorizePage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorizePage(viewModel: AuthViewModel = hiltViewModel(), onUpClick: () -> Unit, onAuthInfoClick: (serviceAuth: ServiceAuth) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val getAuthState = viewModel.getTokenState.collectAsStateWithLifecycle()
    val saveAuthState = viewModel.saveTokenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick)
        }
    ) { contentPadding ->
        AuthorizeScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()), getAuthState.value.data, {
            viewModel.saveAuthInfo(it)
        })

    }

    when {
        saveAuthState.value.isLoading -> {
            LoadingIndicator()
        }
        saveAuthState.value.throwable != null -> {

        }
        saveAuthState.value.error != null -> {

        }
        saveAuthState.value.data != null -> {
            viewModel.getAuthInfo(saveAuthState.value.data!!.serviceName)
        }
        else -> {

        }
    }
}

@Composable
fun AuthorizeScreen(modifier: Modifier = Modifier, auth: ServiceAuth?, saveAuth: (auth: ServiceAuth) -> Unit) {

    val isAuthenticated = auth != null && !auth.isExpired()
    Column {

        val context = LocalContext.current

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 24.dp, top = 12.dp, end = 24.dp)
                .clickable(onClick = {
                    if (isAuthenticated) {

                    } else {
                        authorize(context as Activity, {
                            saveAuth(it)
                        }, {
                           Log.e(TAG, "authorize failed for ${it}")
                        }, {
                            Log.i(TAG, "authorize canceled")
                        })
                    }
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
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp)
                    .weight(1f),
                textAlign = TextAlign.Center)

            if (isAuthenticated) {
                Text(text = stringResource(id = R.string.Connected),
                    modifier = Modifier.padding(end = 24.dp))
            }

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

fun authorize(activity: Activity, onAuthorizeSuccess: (auth: ServiceAuth) -> Unit,
              onAuthorizeFailure: (e: Exception) -> Unit,
              onAuthorizeCanceled: () -> Unit) {
    Log.d(TAG, "authorize()")
    val provider = OAuthProvider.newBuilder("github.com")
    provider.scopes = arrayListOf("read:user", "gist")

    val onSuccess : (authResult: AuthResult) -> Unit = {
        val credential = it.credential
        var accessToken: String? = null
        var idToken: String? = null
        if (credential is OAuthCredential) {
            accessToken = credential.accessToken
            idToken = credential.idToken

        }
        val profile = it.additionalUserInfo?.profile
        Log.d(TAG, "accessToken=${accessToken}, idToken=${idToken}")
        profile?.keys?.forEach { key ->
            Log.d(TAG, "profile ${key}=${profile[key]}")
        }

        accessToken?.let {
            val auth = ServiceAuth(serviceName = SERVICE_GITHUB, accessToken = accessToken)
            auth.userName = profile?.get("login").toString()
            auth.email = profile?.get("email").toString()

            onAuthorizeSuccess(auth)
        }
    }

    val firebaseAuth = FirebaseAuth.getInstance()

    val authResult = firebaseAuth.pendingAuthResult
    if (authResult != null) {
        authResult.addOnSuccessListener {
            onSuccess(it)
        }
            .addOnFailureListener {
                onAuthorizeFailure(it)
            }
            .addOnCanceledListener {
                onAuthorizeCanceled()
            }
    } else {
        firebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener {
                onSuccess(it)
            }
            .addOnFailureListener {
                onAuthorizeFailure(it)
            }
            .addOnCanceledListener {
                onAuthorizeCanceled()
            }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}