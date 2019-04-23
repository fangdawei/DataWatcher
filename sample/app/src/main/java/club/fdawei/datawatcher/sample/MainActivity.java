package club.fdawei.datawatcher.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.api.DataWatcher;
import club.fdawei.datawatcher.sample.data.UserInfo;
import club.fdawei.datawatcher.sample.data.fields_UserInfo;
import club.fdawei.datawatcher.sample.sub.Country;
import club.fdawei.datawatcher.api.data.ChangeEvent;
import club.fdawei.datawatcher.sample.sub.CountryWatcher;
import club.fdawei.datawatcher.sample.sub.CountryWatcherFinal;
import club.fdawei.datawatcher.sample.test.Tester;

public class MainActivity extends AppCompatActivity {

    private TextView tvName, tvAge, tvLocation, tvTesterFunBtn;
    private EditText etName, etAge, etLocation, etCountryName, etProvinceName, etCityName;
    private UserInfo userInfo = new UserInfo();
    private Country country = new Country();
    private Country.Province province = new Country.Province();
    private Country.Province.City city = new Country.Province.City();
    private CountryWatcher countryWatcher;
    private CountryWatcher.ProvinceWatcher provinceWatcher;
    private CountryWatcher.ProvinceWatcher.CityWatcher cityWatcher;
    private CountryWatcherFinal countryWatcherFinal;
    private Tester tester = new Tester();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvName = findViewById(R.id.tv_name);
        tvAge = findViewById(R.id.tv_age);
        tvLocation = findViewById(R.id.tv_location);

        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etLocation = findViewById(R.id.et_location);
        etCountryName = findViewById(R.id.et_country_name);
        etProvinceName = findViewById(R.id.et_province_name);
        etCityName = findViewById(R.id.et_city_name);
        tvTesterFunBtn = findViewById(R.id.tv_tester_fun);

        initListeners();

        countryWatcher = new CountryWatcher(getBaseContext());
        provinceWatcher = new CountryWatcher.ProvinceWatcher(getBaseContext());
        cityWatcher = new CountryWatcher.ProvinceWatcher.CityWatcher(getBaseContext());
        countryWatcherFinal = new CountryWatcherFinal(getBaseContext());

        DataWatcher.bind(this, userInfo);
        DataWatcher.bind(countryWatcher, country);
        DataWatcher.bind(provinceWatcher, province);
        DataWatcher.bind(cityWatcher, city);
        DataWatcher.bind(countryWatcherFinal, country);
    }

    @Override
    protected void onDestroy() {
        DataWatcher.unbindAll(this);
        DataWatcher.unbindAll(countryWatcher);
        DataWatcher.unbindAll(provinceWatcher);
        DataWatcher.unbindAll(cityWatcher);
        DataWatcher.unbindAll(countryWatcherFinal);
        super.onDestroy();
    }

    private void initListeners() {
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                userInfo.setName(editable.toString());
            }
        });

        etAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String ageString = editable.toString();
                if (TextUtils.isEmpty(ageString)) {
                    userInfo.setAge(0);
                } else {
                    int age = 0;
                    try {
                        age = Integer.valueOf(editable.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    userInfo.setAge(age);
                }
            }
        });

        etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                userInfo.setLocation(editable.toString());
            }
        });

        etCountryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                country.setName(s.toString());
            }
        });

        etProvinceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                province.setName(s.toString());
            }
        });

        etCityName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                city.setName(s.toString());
            }
        });

        tvTesterFunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tester.running) {
                    tester.stop();
                    tvTesterFunBtn.setText("Start");
                } else {
                    tester.start();
                    tvTesterFunBtn.setText("Stop");
                }
            }
        });
    }

    @DataWatch(field = fields_UserInfo.name, thread = DataWatch.Thread.MAIN)
    public void onNameChanged(ChangeEvent<UserInfo, String> event) {
        tvName.setText(event.getNewValue());
    }

    @DataWatch(field = fields_UserInfo.age, thread = DataWatch.Thread.MAIN, notifyWhenBind = false)
    public void onAgeChanged(ChangeEvent<UserInfo, Integer> event) {
        tvAge.setText("" + event.getNewValue());
    }

    @DataWatch(field = fields_UserInfo.location, thread = DataWatch.Thread.MAIN)
    public void onLocationChanged(ChangeEvent<UserInfo, String> event) {
        tvLocation.setText(event.getNewValue());
    }
}
