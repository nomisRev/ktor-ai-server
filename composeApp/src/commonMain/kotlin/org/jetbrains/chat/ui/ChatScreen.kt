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
fun ChatScreen(
    viewModel: ChatViewModel = remember { ChatViewModel() }
) {
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

            if (chatState.isLoading) {
                LoadingIndicator()
            }

            chatState.error?.let { error ->
                ErrorMessage(
                    message = error,
                    onDismiss = viewModel::clearError
                )
            }

            Divider()

            MessageInput(
                text = chatState.inputText,
                onTextChange = viewModel::updateInputText,
                onSendClick = viewModel::sendMessage
            )
        }
    }
}