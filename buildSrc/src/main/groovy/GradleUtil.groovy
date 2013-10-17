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
        def shouldRunProguard = false

        if (project.hasProperty("runProguard") || GradleUtil.isDefined(jenkinsBuildNumber)) {
            shouldRunProguard = true
        }

        // We add this so we can absolutely disable proguard on jenkins builds
        // if we need to. Like for instrumentation tests
        if (project.hasProperty("disableProguard")) {
            shouldRunProguard = false
        }

        return shouldRunProguard;
    }

}
