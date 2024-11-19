@file:OptIn(ExperimentalResourceApi::class)

package co.fe.memorygame

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import memorygame.composeapp.generated.resources.Res
import memorygame.composeapp.generated.resources.allDrawableResources
import memorygame.composeapp.generated.resources.compose_multiplatform
import memorygame.composeapp.generated.resources.home
import memorygame.composeapp.generated.resources.rat_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val ROTATION_INITIAL = 0f
private const val ROTATION_OPEN = 360f
private const val ANIMATION_DURATION_MS = 500

private val COLOR_INITIAL = Color(0xFFFFAB40)
private val COLOR_OPEN = Color.White
private val COLOR_MATCH = Color(0xFFFF3D00)
private val COLOR_BACKGROUND = Color(0xFFE65100)

@Composable
fun App() {
    val gameManager = remember { GameManager() }
    val state = gameManager.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        gameManager.initGame()
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier.background(COLOR_BACKGROUND)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Level:",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        "${state.value.level}",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Score:",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        "${state.value.score}",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Tile Count:",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        "${state.value.rowCount * state.value.columnCount}",
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                }
                Column(
                    Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GameBoard(state.value) { cell ->
                        gameManager.guess(cell.column, cell.row)
                    }
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
            for (r in 0..<state.rowCount) {
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
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = if (state.columnCount >= 3) Alignment.Center else if (c % 2 == 0) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .graphicsLayer {
                                        rotationY = currentRotation
                                    }
                                    .background(
                                        color.value, RoundedCornerShape(8.dp)
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
                                    .aspectRatio(1f)
                            ) {
                                when (state.items[r][c].status) {
                                    CellStatus.Closed -> {
                                        Image(painterResource(Res.drawable.home), null)
                                    }

                                    CellStatus.Opened -> {
                                        Image(
                                            painterResource(Res.allDrawableResources[state.imagesList[state.items[r][c].cellValue]]!!),
                                            null
                                        )
                                    }

                                    CellStatus.Match -> {
                                        Image(
                                            painterResource(Res.allDrawableResources[state.imagesList[state.items[r][c].cellValue]]!!),
                                            null
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
}