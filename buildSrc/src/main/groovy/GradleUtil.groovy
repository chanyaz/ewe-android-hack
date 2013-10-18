import org.gradle.api.Project

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

    static def shouldEnableProguard(Project project) {
        def jenkinsBuildNumber = "$System.env.BUILD_NUMBER"
        def boolean shouldRunProguard = false

        // If BUILD_NUMBER defined turn on proguard
        if (GradleUtil.isDefined(jenkinsBuildNumber)) {
            shouldRunProguard = true
        }

        // This setting supercedes BUILD_NUMBER
        if (project.hasProperty("runProguard")) {
            shouldRunProguard = project.getProperty("runProguard").toBoolean()
        }

        return shouldRunProguard;
    }

}
