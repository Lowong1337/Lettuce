package best.lettuce.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfigProperty {

    @Expose @SerializedName("name")
    public String name;

    @Expose @SerializedName("value")
    public Object value;

    public ConfigProperty(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
