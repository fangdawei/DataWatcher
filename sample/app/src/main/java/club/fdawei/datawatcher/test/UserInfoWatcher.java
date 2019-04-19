package club.fdawei.datawatcher.test;

import android.util.Log;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.api.data.ChangeEvent;
import club.fdawei.datawatcher.data.UserInfo;
import club.fdawei.datawatcher.data.fields_UserInfo;

public class UserInfoWatcher {

    private static final String TAG = "Tester";

    @DataWatch(field = fields_UserInfo.name)
    public void onNameChange(ChangeEvent<UserInfo, String> event) {
        Log.i(TAG, "onNameChange, old=" + event.getOldValue() + ", new=" + event.getNewValue());
    }

    @DataWatch(field = fields_UserInfo.age, thread = DataWatch.Thread.MAIN)
    public void onAgeChange(ChangeEvent<UserInfo, Integer> event) {
        Log.i(TAG, "onAgeChange, old=" + event.getOldValue() + ", new=" + event.getNewValue());
    }

    @DataWatch(field = fields_UserInfo.location, thread = DataWatch.Thread.WORK_THREAD)
    public void onLocationChange(ChangeEvent<UserInfo, String> event) {
        Log.i(TAG, "onLocationChange, old=" + event.getOldValue() + ", new=" + event.getNewValue());
    }
}
