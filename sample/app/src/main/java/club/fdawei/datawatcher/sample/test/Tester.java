package club.fdawei.datawatcher.sample.test;

import android.util.Log;

import java.util.Random;

import club.fdawei.datawatcher.api.DataWatcher;
import club.fdawei.datawatcher.sample.data.UserInfo;

public class Tester {

    private static final String TAG = "Tester";

    private static final int DATA_TEST_TASK_COUNT = 50;
    private static final int BIND_TEST_TASK_COUNT = 20;

    public volatile boolean running = false;
    private UserInfo commonData = new UserInfo();

    public void start() {
        Log.i(TAG, "start");
        running = true;

        for(int i = 0; i < DATA_TEST_TASK_COUNT; i++) {
            new Thread(new DataTestRunnable()).start();
        }
        for(int i = 0; i < BIND_TEST_TASK_COUNT; i++) {
            new Thread(new BindTestRunnable()).start();
        }
    }

    public void stop() {
        running = false;
        Log.i(TAG, "stop");
    }

    class DataTestRunnable implements Runnable {

        private UserInfo innerData = new UserInfo();
        private UserInfoWatcher innerWatcher = new UserInfoWatcher();
        private Random random = new Random(System.currentTimeMillis());

        @Override
        public void run() {
            DataWatcher.bind(innerWatcher, innerData);
            DataWatcher.bind(innerWatcher, commonData);
            while (running) {
                int delay = 500 + random.nextInt(1000);
                commonData.setName("" + delay);
                commonData.setAge(delay);
                commonData.setLocation("" + delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                delay = 100 + random.nextInt(1000);
                innerData.setName("" + delay);
                innerData.setAge(delay);
                innerData.setLocation("" + delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            DataWatcher.unbind(innerWatcher, innerData);
            DataWatcher.unbind(innerWatcher, commonData);
        }
    }

    class BindTestRunnable implements Runnable {

        private UserInfoWatcher innerWatcher = new UserInfoWatcher();
        private Random random = new Random(System.currentTimeMillis());

        @Override
        public void run() {

            while (running) {
                DataWatcher.bind(new UserInfoWatcher(), commonData);
                DataWatcher.bind(innerWatcher, commonData);
                try {
                    Thread.sleep(500 + random.nextInt(2000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DataWatcher.unbindAll(innerWatcher);
            }
        }
    }
}
