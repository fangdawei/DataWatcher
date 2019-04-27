package club.fdawei.datawatcher.sample.kt

import club.fdawei.datawatcher.annotation.DataSource
import club.fdawei.datawatcher.annotation.FieldIgnore

/**
 * Created by david on 2019/04/25.
 */
@DataSource
class Food {
    @FieldIgnore
    val id: Long = System.currentTimeMillis()
    var name: String = ""
}