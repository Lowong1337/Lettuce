package best.lettuce.modules.property.impl;

import best.lettuce.modules.property.Property;

public class HeaderProperty extends Property {

    public HeaderProperty(String name, boolean state) {
        this.name = name;
    }

    public HeaderProperty(String name) {
        this(name, false);
    }

    @Override
    public <t> t getConfigValue() {
        return null;
    }
}