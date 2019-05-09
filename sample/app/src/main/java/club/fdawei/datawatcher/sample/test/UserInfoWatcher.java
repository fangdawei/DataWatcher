package club.fdawei.datawatcher.sample.test;

import android.util.Log;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.api.data.ChangeEvent;
import club.fdawei.datawatcher.sample.data.UserInfo;

class UserInfoWatcher {

    private static final String TAG = "Tester";

    @DataWatch(data = UserInfo.class, field = "name")
    public void onNameChange(ChangeEvent<UserInfo, String> event) {
        Log.i(TAG, "onNameChange, old=" + event.getOldValue() + ", new=" + event.getNewValue());
    }

    @DataWatch(data = UserInfo.class, field = "age", thread = DataWatch.Thread.MAIN)
    public void onAgeChange(ChangeEvent<UserInfo, Integer> event) {
        Log.i(TAG, "onAgeChange, old=" + event.getOldValue() + ", new=" + event.getNewValue());
    }

    @DataWatch(data = UserInfo.class, field = "location", thread = DataWatch.Thread.WORK_THREAD)
    public void onLocationChange(ChangeEvent<UserInfo, String> event) {
        Log.i(TAG, "onLocationChange, old=" + event.getOldValue() + ", new=" + event.getNewValue());
    }
}
