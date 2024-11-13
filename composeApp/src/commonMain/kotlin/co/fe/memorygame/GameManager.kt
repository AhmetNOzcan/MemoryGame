package co.fe.memorygame

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class GameState(
    val columnCount: Int = 0,
    val rowCount: Int = 0,
    val firstGuess: Cell? = null,
    val secondGuess: Cell? = null,
    val foundCount: Int = 0,
    val items: Array<Array<Cell>>,
    val gameProgress: GameProgress
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameState

        if (columnCount != other.columnCount) return false
        if (rowCount != other.rowCount) return false
        if (firstGuess != other.firstGuess) return false
        if (secondGuess != other.secondGuess) return false
        if (foundCount != other.foundCount) return false
        if (!items.contentDeepEquals(other.items)) return false
        if (gameProgress != other.gameProgress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = columnCount
        result = 31 * result + rowCount
        result = 31 * result + (firstGuess?.hashCode() ?: 0)
        result = 31 * result + (secondGuess?.hashCode() ?: 0)
        result = 31 * result + foundCount
        result = 31 * result + items.contentDeepHashCode()
        result = 31 * result + gameProgress.hashCode()
        return result
    }
}

class GameManager {

    val state = MutableStateFlow(GameState(columnCount = 0, rowCount = 0, items = emptyArray(), gameProgress = GameProgress.Idle))

    fun initGame(columnCount: Int) {
        val uniqueItemCount = (columnCount * (state.value.rowCount + 1)) / 2

        val gameItems = mutableListOf<Int>()

        for (i in 0..<uniqueItemCount) {
            gameItems.add(i)
            gameItems.add(i)
        }

        gameItems.shuffle()

        var index = 0

        val gameBoardMatrix = Array(state.value.rowCount + 1) { r ->
            val item = Array(columnCount) { c ->
                val item = Cell(cellValue = gameItems[index], column = c, row = r)
                index++
                item
            }
            item
        }

        state.update {
            state.value.copy(
                columnCount = columnCount,
                rowCount = state.value.rowCount + 1,
                items = gameBoardMatrix,
                gameProgress = GameProgress.InProgress,
                foundCount = 0,
                firstGuess = null,
                secondGuess = null
            )
        }
    }

    fun guess(column: Int, row: Int) {
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
            var gameProgress = state.value.gameProgress

            if (state.value.firstGuess?.cellValue == guess.cellValue) {
                status = CellStatus.Match
                foundCount++
            }

            state.value.firstGuess?.let { f ->

                val items = state.value.items.copyOf()
                items[f.row][f.column] = items[f.row][f.column].copy(status = status)
                items[row][column] = guess.copy(status = status)

                if (foundCount == (state.value.rowCount * state.value.columnCount) / 2) {
                    gameProgress = GameProgress.Win
                }

                state.update { s ->
                    s.copy(
                        items = items,
                        firstGuess = items[f.row][f.column],
                        secondGuess = items[row][column],
                        foundCount = foundCount,
                        gameProgress = gameProgress
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
}

enum class GameProgress {
    Idle,
    InProgress,
    Win,
    Lose
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