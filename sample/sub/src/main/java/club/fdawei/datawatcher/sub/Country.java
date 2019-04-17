package club.fdawei.datawatcher.sub;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.FieldIgnore;

/**
 * Created by david on 2019/4/9.
 */
@DataSource
public class Country {

    @FieldIgnore
    private long id;

    private String name;

    public Country() {
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

    @DataSource
    public static class Province {

        @FieldIgnore
        private long id;

        private String name;

        public Province() {
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

        @DataSource
        public static class City {

            @FieldIgnore
            private long id;

            private String name;

            public long getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
