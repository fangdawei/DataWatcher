package club.fdawei.datawatcher.sample.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.annotation.WatchInherit;
import club.fdawei.datawatcher.api.data.ChangeEvent;

/**
 * Created by david on 2019-04-23.
 */
@WatchInherit
public class CountryWatcher1 extends CountryWatcher0 {

    public CountryWatcher1(Context context) {
        super(context);
        this.watcherName = "CountryWatcher1";
    }

    @WatchData(data = Country.class, field = "name", notifyWhenBind = false)
    public void onNameChange1(ChangeEvent<Country, String> event) {
        Toast.makeText(context, String.format("%s->%s name=%s", watcherName, "onNameChange1", event.getNewValue()), Toast.LENGTH_SHORT).show();
    }
}
