package ObjBox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
data class HourlyObj (
    @Id var id: Long = 0,
    var pmData: Int? = null,
    var createAt: Date? = null,
    var wordsRemember: Int = 0
)