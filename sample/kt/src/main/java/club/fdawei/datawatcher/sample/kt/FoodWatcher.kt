package club.fdawei.datawatcher.sample.kt

import android.content.Context
import android.widget.Toast
import club.fdawei.datawatcher.annotation.WatchData
import club.fdawei.datawatcher.api.data.ChangeEvent

/**
 * Created by david on 2019/04/25.
 */
class FoodWatcher constructor(private val context: Context) {

    @WatchData(data = Food::class, field = "name", thread = WatchData.Thread.MAIN)
    fun onNameUpdate(event: ChangeEvent<Food, String>) {
        Toast.makeText(context, "FoodWatcher onNameUpdate ${event.newValue}",
            Toast.LENGTH_SHORT).show()
    }
}