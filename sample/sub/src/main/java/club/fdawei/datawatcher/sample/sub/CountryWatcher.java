package club.fdawei.datawatcher.sample.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.api.data.ChangeEvent;

public class CountryWatcher {

    private Context context;

    public CountryWatcher(Context context) {
        this.context = context;
    }

    @WatchData(data = Country.class, field = "name", notifyWhenBind = false)
    public void onNameChange(ChangeEvent<Country, String> event) {
        Toast.makeText(context, "Country name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
    }

    public static class ProvinceWatcher {

        protected String watcherName;
        private Context context;

        public ProvinceWatcher(Context context) {
            this.context = context;
            this.watcherName = "ProvinceWatcher";
        }

        @WatchData(data = Country.Province.class, field = "name", notifyWhenBind = false)
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

            @WatchData(data = Country.Province.City.class, field = "name", notifyWhenBind = false)
            public void onNameChange(ChangeEvent<Country.Province.City, String> event) {
                Toast.makeText(context, "City name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
