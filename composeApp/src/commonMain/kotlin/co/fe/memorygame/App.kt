package co.fe.memorygame

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

private const val ROTATION_INITIAL = -180f
private const val ROTATION_OPEN = 0f
private const val ANIMATION_DURATION_MS = 500

private val COLOR_INITIAL = Color(0xFFF095DC)
private val COLOR_OPEN = Color.White
private val COLOR_MATCH = Color(0xFFf5b9a9)

@Composable
fun App() {
    val gameManager = remember { GameManager() }
    val state = gameManager.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.value.gameProgress) {
        if (state.value.gameProgress == GameProgress.Idle || state.value.gameProgress == GameProgress.Win) {
            gameManager.initGame(4)
        }
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            Column(
                Modifier.background(Color(0xffd16c69)).fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameBoard(state.value) { cell ->
                    gameManager.guess(cell.column, cell.row)
                }
            }
        }
    }
}

@Composable
fun GameBoard(
    state: GameState,
    onClick: (Cell) -> Unit,
) {
    if (state.columnCount > 0) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(state.rowCount / 10f),
        ) {
            for (r in 0..< state.rowCount) {
                Row(
                    modifier = Modifier.padding(4.dp).fillMaxWidth().weight(1f),
                ) {
                    for (c in 0..<state.columnCount) {

                        var currentRotation by remember { mutableStateOf(ROTATION_INITIAL) }

                        val color = remember { Animatable(COLOR_INITIAL) }
                        val rotation = remember { Animatable(currentRotation) }

                        LaunchedEffect(state.items[r][c].status) {
                            when (state.items[r][c].status) {
                                CellStatus.Closed -> {
                                    launch {
                                        rotation.animateTo(
                                            targetValue = ROTATION_INITIAL,
                                            animationSpec = tween(durationMillis = ANIMATION_DURATION_MS)
                                        ) {
                                            currentRotation = value
                                        }
                                    }

                                    launch {
                                        color.animateTo(
                                            COLOR_INITIAL,
                                            animationSpec = tween(ANIMATION_DURATION_MS)
                                        )
                                    }
                                }

                                CellStatus.Opened -> {
                                    launch {
                                        rotation.animateTo(
                                            targetValue = ROTATION_OPEN,
                                            animationSpec = tween(durationMillis = ANIMATION_DURATION_MS)
                                        ) {
                                            currentRotation = value
                                        }
                                    }

                                    launch {
                                        color.animateTo(
                                            COLOR_OPEN,
                                            animationSpec = tween(ANIMATION_DURATION_MS)
                                        )
                                    }
                                }

                                CellStatus.Match -> {
                                    launch {
                                        rotation.animateTo(
                                            targetValue = ROTATION_OPEN,
                                            animationSpec = tween(durationMillis = ANIMATION_DURATION_MS)
                                        ) {
                                            currentRotation = value
                                        }
                                    }

                                    launch {
                                        color.animateTo(
                                            COLOR_MATCH,
                                            animationSpec = tween(ANIMATION_DURATION_MS)
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .graphicsLayer {
                                    rotationY = currentRotation
                                }
                                .background(color.value, RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onClick(state.items[r][c])
                                }
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            when (state.items[r][c].status) {
                                CellStatus.Closed -> {
//                                    Text(
//                                        text = "${state.items[r][c].cellValue}",
//                                        textAlign = TextAlign.Center,
//                                        fontSize = 24.sp
//                                    )
                                }

                                CellStatus.Opened -> {
                                    Text(
                                        text = if (currentRotation == 0f) "${state.items[r][c].cellValue}" else "",
                                        textAlign = TextAlign.Center,
                                        fontSize = 24.sp
                                    )
                                }

                                CellStatus.Match -> {
                                    Text(
                                        text = if (currentRotation == 0f) "${state.items[r][c].cellValue}" else "",
                                        textAlign = TextAlign.Center,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}