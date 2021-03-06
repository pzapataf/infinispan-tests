import java.io.Serializable;

/**
 * Test class
 */
public class Something implements Serializable {
    private String key;
    private String value;

    public Something(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Something{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
