package ObjBox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class WordsObjBox (
    @Id var id: Long = 0,
    var wordId: Int = 0,
    var word: String = "1",
    var pronounce: String = "1",
    var meaning: String = "",
    var sentenceJP: String = "",
    var sentenceCN: String = ""
)