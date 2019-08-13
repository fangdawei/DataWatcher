# DataWatcher
基于观察者模式的数据发布订阅框架

| datawatcher-annotation | datawatcher-api | datawatcher-processor | datawatcher-plugin |
| :---: | :---: | :---: | :---: |
| [ ![Download](https://api.bintray.com/packages/fangdawei/maven/datawatcher-annotation/images/download.svg?version=1.1.2) ](https://bintray.com/fangdawei/maven/datawatcher-annotation/1.1.2/link) | [ ![Download](https://api.bintray.com/packages/fangdawei/maven/datawatcher-api/images/download.svg?version=1.1.2) ](https://bintray.com/fangdawei/maven/datawatcher-api/1.1.2/link) | [ ![Download](https://api.bintray.com/packages/fangdawei/maven/datawatcher-processor/images/download.svg?version=1.1.2) ](https://bintray.com/fangdawei/maven/datawatcher-processor/1.1.2/link) | [ ![Download](https://api.bintray.com/packages/fangdawei/maven/datawatcher-plugin/images/download.svg?version=1.1.2) ](https://bintray.com/fangdawei/maven/datawatcher-plugin/1.1.2/link) |

#### 使用方式
* 配置

项目根目录下的build.gradle中添加配置

```
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath "club.fdawei.datawatcher:datawatcher-plugin:?"
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
```

app的build.gradle中添加配置

```
apply plugin: 'datawatcher-plugin'

dependencies {
    implementation "club.fdawei.datawatcher:datawatcher-annotation:?"
    implementation "club.fdawei.datawatcher:datawatcher-api:?"
    annotationProcessor "club.fdawei.datawatcher:datawatcher-processor:?"
}
```

其他Module的build.gradle中添加配置

```
dependencies {
    implementation "club.fdawei.datawatcher:datawatcher-annotation:?"
    implementation "club.fdawei.datawatcher:datawatcher-api:?"
    annotationProcessor "club.fdawei.datawatcher:datawatcher-processor:?"
}
```

如果使用Kotlin，请用kapt代替annotationProcessor

* 使用

定义数据源

```
@DataSource
public class UserInfo {
    private long id;

    private String name = "david";

    private int age = 18;

    private String location;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
```

通过 @FieldIgnore 可以指定需要忽略不监听的字段

默认会自动查找数据源中的setter方法，设置 @DataSource 中的 setterAutoFind = false 将禁用自动查找，此时可以通过 @FieldSetter 显示指定setter方法

监听数据变化

```
public class Observer {

    public void init(UserInfo userInfo) {
        //bind数据，开始监听
        DataWatcher.bind(this, userInfo);
    }

    public void destroy() {
        //unbind数据，停止监听
        DataWatcher.unbindAll(this);
    }

    @WatchData(data = UserInfo.class, field = "name", thread = WatchData.Thread.MAIN)
    public void onNameChanged(ChangeEvent<UserInfo, String> event) {
        
    }

    @WatchData(data = UserInfo.class, field = "age", thread = WatchData.Thread.MAIN, notifyWhenBind = false)
    public void onAgeChanged(ChangeEvent<UserInfo, Integer> event) {
        
    }

    @WatchData(data = UserInfo.class, field = "location", thread = WatchData.Thread.MAIN)
    public void onLocationChanged(ChangeEvent<UserInfo, String> event) {
        
    }
}
```

@WatchData 中的 thread 指定回调方法执行的线程，notifyWhenBind 指定是否需要在bind数据时立即调用回调方法

