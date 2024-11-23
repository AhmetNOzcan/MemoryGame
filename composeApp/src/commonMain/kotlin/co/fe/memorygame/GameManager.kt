package co.fe.memorygame

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val IMAGES = arrayOf(
    "birds_icon",
    "bulls_icon",
    "camels_icon",
    "cats_icon",
    "cows_icon",
    "dogs_icon",
    "ducks_icon",
    "elephants_icon",
    "fishes_icon",
    "giraffes_icon",
    "gooses_icon",
    "horses_icon",
    "lions_icon",
    "rabbits_icon",
    "rat_icon",
    "tigers_icon",
    "turtles_icon",
    "wolfs_icon",
    "monkey_icon",
    "husky_icon",
    "pig_icon",
    "koala_icon",
    "bear_icon",
    "goat_icon",
)

sealed interface GameAction {
    data object GameWon: GameAction
    data object GameLost: GameAction
}

class GameManager {

    val state = MutableStateFlow(GameState(rowCount = 0, columnCount = 0, itemsCount = 0, items = emptyArray()))

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _eventChannel = Channel<GameAction>()
    val eventChannel = _eventChannel.receiveAsFlow()

    fun initGame() {
        val itemsCount = state.value.itemsCount + 4
        val pair = closestFactors(itemsCount)
        val uniqueItemCount = itemsCount / 2
        val gameItems = mutableListOf<Int>()

        scope.launch(Dispatchers.Default) {

            for (i in 0..< uniqueItemCount) {
                gameItems.add(i)
                gameItems.add(i)
            }

            gameItems.shuffle()

            state.update {
                it.copy(
                    itemsCount = itemsCount,
                    columnCount = pair.first,
                    rowCount = pair.second,
                    items = createGameBoard(pair.second, pair.first, CellStatus.Opened, gameItems),
                    foundCount = 0,
                    firstGuess = null,
                    secondGuess = null,
                    level = it.level + 1,
                    imagesList = IMAGES.toList().shuffled().subList(0, uniqueItemCount),
                    previewMode = true
                )
            }

            delay(2500)

            state.update {
                it.copy(
                    items = createGameBoard(pair.second, pair.first, CellStatus.Closed, gameItems),
                    previewMode = false
                )
            }
        }
    }

    fun guess(column: Int, row: Int) {
        if (state.value.previewMode) return

        val guess = state.value.items[row][column]

        if(guess.status == CellStatus.Match) return

        if (state.value.firstGuess == null) {

            val items: Array<Array<Cell>> = state.value.items.copyOf()
            items[row][column] = guess.copy(status = CellStatus.Opened)

            state.update {
                it.copy(
                    items = items,
                    firstGuess = items[row][column],
                    secondGuess = null
                )
            }
        } else if (state.value.secondGuess == null) {

            if (state.value.firstGuess == guess) return

            var status: CellStatus = CellStatus.Opened
            var foundCount = state.value.foundCount
            var addToScore = 0

            if (state.value.firstGuess?.cellValue == guess.cellValue) {
                status = CellStatus.Match
                addToScore = 1
                foundCount++
            }
            else {
                addToScore = -1
            }

            state.value.firstGuess?.let { f ->

                val items = state.value.items.copyOf()
                items[f.row][f.column] = items[f.row][f.column].copy(status = status)
                items[row][column] = guess.copy(status = status)

                if (foundCount == (state.value.rowCount * state.value.columnCount) / 2) {
                    state.update {
                        it.copy(
                            previewMode = true
                        )
                    }
                    scope.launch {
                        _eventChannel.send(GameAction.GameWon)
                    }
                }

                state.update { s ->
                    s.copy(
                        items = items,
                        firstGuess = items[f.row][f.column],
                        secondGuess = items[row][column],
                        foundCount = foundCount,
                        score = s.score + addToScore,
                    )
                }
            }
        }
        else {
            val items: Array<Array<Cell>> = state.value.items.copyOf()

            state.value.firstGuess?.let {
                if (it.status != CellStatus.Match)
                    items[it.row][it.column] = it.copy(status = CellStatus.Closed)
            }

            state.value.secondGuess?.let {
                if (it.status != CellStatus.Match)
                    items[it.row][it.column] = it.copy(status = CellStatus.Closed)
            }

            items[row][column] = guess.copy(status = CellStatus.Opened)


            state.update {
                it.copy(
                    items = items,
                    firstGuess = items[row][column],
                    secondGuess = null
                )
            }
        }
        println(state.value)
    }

    private fun closestFactors(number: Int): Pair<Int, Int> {
        val sqrt = kotlin.math.sqrt(number.toDouble()).toInt()
        for (x in sqrt downTo 1) {
            if (number % x == 0) {
                val y = number / x
                return Pair(x, y)
            }
        }
        return Pair(0, 0)
    }

    private fun createGameBoard(rowCount: Int, columnCount: Int, status: CellStatus, gameItems: List<Int>): Array<Array<Cell>> {
        var index = 0
        return Array(rowCount) { r ->
            val item = Array(columnCount) { c ->
                val item = Cell(cellValue = gameItems[index], column = c, row = r, status = status)
                index++
                item
            }
            item
        }
    }

}

sealed interface CellStatus {
    data object Closed: CellStatus
    data object Opened: CellStatus
    data object Match: CellStatus
}

data class Cell(
    val status: CellStatus = CellStatus.Closed,
    val cellValue: Int,
    val column: Int,
    val row: Int
)