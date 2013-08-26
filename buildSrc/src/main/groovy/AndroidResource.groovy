class AndroidResource {

    /**
     * AndroidResource contains data for creating an Android Resource XML tag, where
     * new AndroidResource("string", "app_name", "Expedia") when written to XML yields:
     * <string name="app_name>Expedia</string>
     */

    private mType
    private mName
    private mValue

    AndroidResource(type, name, value) {
        mType = type
        mName = name
        mValue = value
    }

    def getType() {
        return mType
    }

    def getName() {
        return mName
    }

    def getValue() {
        return mValue
    }
}