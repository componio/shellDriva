package resourcetypes;

public enum OpenCmsVersion {

    V850("8.5.0"), V851("8.5.1"), V852("8.5.2"), V901("9.0.1"), V950("9.5.0");
    private final String value;

    OpenCmsVersion(String value) {
        this.value = value;
    }

    
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static OpenCmsVersion getEnum(String value) {
        for (OpenCmsVersion v : values()) {
            if (v.getValue().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
