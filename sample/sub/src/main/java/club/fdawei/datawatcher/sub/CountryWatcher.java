package club.fdawei.datawatcher.sub;

import android.content.Context;
import android.widget.Toast;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.api.data.ChangeEvent;

public class CountryWatcher {

    private Context context;

    public CountryWatcher(Context context) {
        this.context = context;
    }

    @DataWatch(field = fields_Country.name, notifyWhenBind = false)
    public void onNameChange(ChangeEvent event) {
        Toast.makeText(context, "Country name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
    }

    public static class ProvinceWatcher {

        private Context context;

        public ProvinceWatcher(Context context) {
            this.context = context;
        }

        @DataWatch(field = fields_Country.Province.name, notifyWhenBind = false)
        public void onNameChange(ChangeEvent<Country.Province, String> event) {
            Toast.makeText(context, "Province name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
        }

        public static class CityWatcher {

            private Context context;

            public CityWatcher(Context context) {
                this.context = context;
            }

            @DataWatch(field = fields_Country.Province.City.name, notifyWhenBind = false)
            public void onNameChange(ChangeEvent<Country.Province.City, String> event) {
                Toast.makeText(context, "City name is " + event.getNewValue(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
