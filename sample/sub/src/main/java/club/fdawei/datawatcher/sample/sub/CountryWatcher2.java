package club.fdawei.datawatcher.sample.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.annotation.InheritWatch;
import club.fdawei.datawatcher.api.data.ChangeEvent;

/**
 * Created by david on 2019-04-23.
 */
@InheritWatch
public class CountryWatcher2 extends CountryWatcher1 {

    public CountryWatcher2(Context context) {
        super(context);
        this.watcherName = "CountryWatcher2";
    }

    @DataWatch(field = fields_Country.name, notifyWhenBind = false)
    public void onNameChange2(ChangeEvent<Country, String> event) {
        Toast.makeText(context, String.format("%s->%s name=%s", watcherName, "onNameChange2", event.getNewValue()), Toast.LENGTH_SHORT).show();
    }
}
