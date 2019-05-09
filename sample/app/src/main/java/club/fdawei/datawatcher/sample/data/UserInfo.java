package club.fdawei.datawatcher.sample.data;

import java.io.Serializable;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.FieldIgnore;
import club.fdawei.datawatcher.annotation.FieldSetter;

/**
 * Created by david on 2019/4/4.
 */

@DataSource
public class UserInfo {

    @FieldIgnore
    private long id;

    private String name = "david";

    private int age = 18;

    private String location;

    public UserInfo() {
        this.id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @FieldSetter(field = "name")
    public void updateName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @FieldSetter(field = "age")
    public void updateAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
