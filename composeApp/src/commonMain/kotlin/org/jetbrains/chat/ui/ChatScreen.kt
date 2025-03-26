package org.jetbrains.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.chat.model.ChatState
import org.jetbrains.chat.model.Message
import org.jetbrains.chat.viewmodel.ChatViewModel

@Composable
fun MessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages) { message ->
            MessageBubble(message = message)
        }
    }
}

@Composable
fun ChatScreen() {
    val viewModel = remember { ChatViewModel() }
    val chatState by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MessageList(
                messages = chatState.messages,
                modifier = Modifier.weight(1f)
            )

            when (val state = chatState) {
                is ChatState.Loading -> LoadingIndicator()
                is ChatState.Error -> ErrorMessage(message = state.error, onDismiss = viewModel::clearError)
                else -> Unit
            }

            Divider()

            MessageInput(onTextSend = viewModel::sendMessage)
        }
    }
}
