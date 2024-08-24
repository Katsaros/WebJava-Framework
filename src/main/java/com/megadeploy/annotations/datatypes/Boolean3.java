package com.megadeploy.annotations.datatypes;

public class Boolean3 {
    public static final Boolean3 TRUE = new Boolean3(true);
    public static final Boolean3 FALSE = new Boolean3(false);
    public static final Boolean3 EMPTY = new Boolean3(null);

    private final Boolean value;

    private Boolean3(Boolean value) {
        this.value = value;
    }

    public boolean isTrue() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isFalse() {
        return Boolean.FALSE.equals(value);
    }

    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "EMPTY";
        }
        return value.toString().toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Boolean3) {
            Boolean3 other = (Boolean3) obj;
            return this.value == other.value;
        }
        if (obj instanceof Boolean) {
            return this.value != null && this.value.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    public static Boolean3 fromBoolean(Boolean bool) {
        return bool == null ? EMPTY : bool ? TRUE : FALSE;
    }
}