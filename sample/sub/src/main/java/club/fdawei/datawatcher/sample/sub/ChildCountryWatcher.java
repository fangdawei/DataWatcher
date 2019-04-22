package club.fdawei.datawatcher.sample.sub;

import android.content.Context;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.annotation.DataWatcher;
import club.fdawei.datawatcher.api.data.ChangeEvent;

/**
 * Create by david on 2019/04/21
 */
@DataWatcher
public class ChildCountryWatcher extends CountryWatcher {

    public ChildCountryWatcher(Context context) {
        super(context);
        this.watcherName = "ChildCountryWatcher";
    }

    @Override
    @DataWatch(field = fields_Country.name)
    public void onNameChange(ChangeEvent<Country, String> event) {
        super.onNameChange(event);
    }
}
