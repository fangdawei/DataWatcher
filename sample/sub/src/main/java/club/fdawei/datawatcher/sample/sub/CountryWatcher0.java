package club.fdawei.datawatcher.sample.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.api.data.ChangeEvent;

/**
 * Created by david on 2019-04-23.
 */
public class CountryWatcher0 {

    protected String watcherName;
    protected Context context;

    public CountryWatcher0(Context context) {
        this.context = context;
        this.watcherName = "CountryWatcher0";
    }

    @WatchData(data = Country.class, field = "name", notifyWhenBind = false)
    public void onNameChange0(ChangeEvent<Country, String> event) {
        Toast.makeText(context, String.format("%s->%s name=%s", watcherName, "onNameChange0", event.getNewValue()), Toast.LENGTH_SHORT).show();
    }
}
