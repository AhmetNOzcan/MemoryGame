package co.fe.memorygame


data class GameState(
    val columnCount: Int = 0,
    val rowCount: Int = 0,
    val itemsCount: Int = 0,
    val firstGuess: Cell? = null,
    val secondGuess: Cell? = null,
    val foundCount: Int = 0,
    val items: Array<Array<Cell>>,
    val score: Int = 0,
    val level: Int = 0,
    val imagesList: List<String> = emptyList(),
    val previewMode: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameState

        if (columnCount != other.columnCount) return false
        if (rowCount != other.rowCount) return false
        if (itemsCount != other.itemsCount) return false
        if (firstGuess != other.firstGuess) return false
        if (secondGuess != other.secondGuess) return false
        if (foundCount != other.foundCount) return false
        if (!items.contentDeepEquals(other.items)) return false
        if (score != other.score) return false
        if (level != other.level) return false
        if (imagesList != other.imagesList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = columnCount
        result = 31 * result + rowCount
        result = 31 * result + itemsCount
        result = 31 * result + (firstGuess?.hashCode() ?: 0)
        result = 31 * result + (secondGuess?.hashCode() ?: 0)
        result = 31 * result + foundCount
        result = 31 * result + items.contentDeepHashCode()
        result = 31 * result + score
        result = 31 * result + level
        result = 31 * result + imagesList.hashCode()
        return result
    }
}
