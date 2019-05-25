package club.fdawei.datawatcher.sample.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.annotation.WatchInherit;
import club.fdawei.datawatcher.api.data.ChangeEvent;

/**
 * Created by david on 2019-04-23.
 */
@WatchInherit(maxGenerations = 2)
public class CountryWatcherFinal extends CountryWatcher2 {

    public CountryWatcherFinal(Context context) {
        super(context);
        this.watcherName = "CountryWatcherFinal";
    }

    @WatchData(data = Country.class, field = "name", notifyWhenBind = false)
    public void onNameChangeFinal(ChangeEvent<Country, String> event) {
        Toast.makeText(context, String.format("%s->%s name=%s", watcherName, "onNameChangeFinal", event.getNewValue()), Toast.LENGTH_SHORT).show();
    }
}
