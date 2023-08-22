package best.lettuce.modules.property;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.function.Supplier;

public abstract class Property {
    @Expose @SerializedName("name")
    public String name;

    protected Supplier<Boolean> shown = () -> true;

    public abstract <t> t getConfigValue();

    public boolean isShown() {
        return this.shown.get();
    }
}
