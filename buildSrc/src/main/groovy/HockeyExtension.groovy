class HockeyExtension {

    // This is an "extension" class that gets attached to a buildType. This allows for easy configuration
    // of HockeyApp builds. After adding this extension, one can set the enabled value and later query the
    // buildType for whether or not hockeyApp is enabled and perform tasks if necessary.
    //
    // http://stackoverflow.com/questions/17697154/gradle-android-plugin-add-custom-flavor-attribute

    boolean enabled

    HockeyExtension() {
        enabled = false;
    }
}
