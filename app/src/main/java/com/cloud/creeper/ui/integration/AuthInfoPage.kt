package com.cloud.creeper.ui.integration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloud.creeper.R
import com.cloud.creeper.repository.entity.ServiceAuth

/**
 *
 * Created by cloud on 2024/6/6.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthInfoPage(viewModel: AuthViewModel = hiltViewModel(), onUpClick: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val getAuthState = viewModel.getTokenState.collectAsStateWithLifecycle()
    val deleteAuthState = viewModel.deleteTokenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, onUpClick = onUpClick)
        }
    ) { contentPadding ->
        AuthInfoScreen(modifier = Modifier.padding(top = contentPadding.calculateTopPadding()), getAuthState.value.data, {
            viewModel.saveAuthInfo(it)
        })

    }

    when {
        deleteAuthState.value.isLoading -> {
            LoadingIndicator()
        }
        deleteAuthState.value.throwable != null -> {

        }
        deleteAuthState.value.vmError != null -> {

        }
        deleteAuthState.value.data != null -> {

        }
        else -> {

        }
    }
}

@Composable
fun AuthInfoScreen(modifier: Modifier = Modifier, auth: ServiceAuth?, saveAuth: (auth: ServiceAuth) -> Unit) {

    val isAuthenticated = auth != null && !auth.isExpired()
    Column {

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior,
                      modifier: Modifier = Modifier, onUpClick: () -> Unit) {
    androidx.compose.material3.TopAppBar(
        title = {
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
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}