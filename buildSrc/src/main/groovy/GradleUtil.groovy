class GradleUtil {

    static def getPackageName(variant) {
        def suffix = variant.buildType.packageNameSuffix
        def packageName = variant.productFlavors.get(0).packageName
        if (isDefined(suffix)) {
            packageName += suffix
        }
        return packageName
    }

    static def isDefined(s) {
        return s != null && !s.isEmpty() && s != "null"
    }

}