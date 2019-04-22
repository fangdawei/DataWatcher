package club.fdawei.datawatcher.sample.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.api.data.ChangeEvent;

public class CountryWatcher {

    protected String watcherName;
    private Context context;

    public CountryWatcher(Context context) {
        this.context = context;
        this.watcherName = "CountryWatcher";
    }

    @DataWatch(field = fields_Country.name, notifyWhenBind = false)
    public void onNameChange(ChangeEvent<Country, String> event) {
        Toast.makeText(context, watcherName + " name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
    }

    public static class ProvinceWatcher {

        protected String watcherName;
        private Context context;

        public ProvinceWatcher(Context context) {
            this.context = context;
            this.watcherName = "ProvinceWatcher";
        }

        @DataWatch(field = fields_Country.Province.name, notifyWhenBind = false)
        public void onNameChange(ChangeEvent<Country.Province, String> event) {
            Toast.makeText(context, "Province name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
        }

        public static class CityWatcher {

            protected String watcherName;
            private Context context;

            public CityWatcher(Context context) {
                this.context = context;
                this.watcherName = "CityWatcher";
            }

            @DataWatch(field = fields_Country.Province.City.name, notifyWhenBind = false)
            public void onNameChange(ChangeEvent<Country.Province.City, String> event) {
                Toast.makeText(context, "City name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
