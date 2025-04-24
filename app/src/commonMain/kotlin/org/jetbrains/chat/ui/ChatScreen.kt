package org.jetbrains.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.launch
import org.jetbrains.chat.repository.WebSocketChatRepository
import org.jetbrains.chat.viewmodel.ChatViewModel
import org.jetbrains.chat.viewmodel.Message
import org.jetbrains.chat.viewmodel.MessageType

/** Main chat screen composable. */
@Composable
fun ChatScreen() {
    val scope = rememberCoroutineScope()
    val httpClient = remember { HttpClient { install(WebSockets) } }
    val repository = remember { WebSocketChatRepository(httpClient, "localhost:8000", scope) }
    val viewModel = remember { ChatViewModel(repository, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.connect() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ChatMessages(messages = state.messages, modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(8.dp))

        MessageInput(onSendMessage = { viewModel.sendMessage(it) }, isLoading = state.isLoading)

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Displays the list of chat messages. */
@Composable
private fun ChatMessages(messages: List<Message>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(messages) { message -> MessageItem(message = message) }
    }
}

/** Displays a single chat message. */
@Composable
private fun MessageItem(message: Message, modifier: Modifier = Modifier) {
    val isUserMessage = message.type == MessageType.USER

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
    ) {
        if (!isUserMessage) {
            // AI avatar
            Box(
                modifier =
                    Modifier.size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary)
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "AI",
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message bubble
        Column(
            modifier =
                Modifier.weight(0.8f)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isUserMessage) 12.dp else 0.dp,
                            topEnd = if (isUserMessage) 0.dp else 12.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp,
                        )
                    )
                    .background(
                        if (isUserMessage) MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        else MaterialTheme.colors.surface
                    )
                    .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color =
                    if (isUserMessage) MaterialTheme.colors.onPrimary
                    else MaterialTheme.colors.onSurface,
            )

            if (!message.isComplete) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(1.dp)
                )
            }
        }

        if (isUserMessage) {
            Spacer(modifier = Modifier.width(8.dp))

            // User avatar
            Box(
                modifier =
                    Modifier.size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.secondary)
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "U",
                    color = MaterialTheme.colors.onSecondary,
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Input field for sending messages. */
@Composable
private fun MessageInput(
    onSendMessage: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Type a message...") },
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            singleLine = true,
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    scope.launch {
                        onSendMessage(text)
                        text = ""
                    }
                }
            },
            enabled = text.isNotBlank() && !isLoading,
            modifier =
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isLoading) MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint =
                        if (text.isNotBlank()) MaterialTheme.colors.onPrimary
                        else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}
