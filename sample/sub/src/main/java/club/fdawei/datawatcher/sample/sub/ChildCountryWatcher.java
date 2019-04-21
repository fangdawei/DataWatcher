package club.fdawei.datawatcher.sample.sub;

import android.content.Context;

/**
 * Create by david on 2019/04/21
 */
public class ChildCountryWatcher extends CountryWatcher {

    public ChildCountryWatcher(Context context) {
        super(context);
        this.watcherName = "ChildCountryWatcher";
    }
}
