import org.gradle.api.Project

class GradleUtil {

    static def getPackageName(variant) {
        def suffix = variant.buildType.applicationIdSuffix
        def applicationId = variant.productFlavors.get(0).applicationId
        if (isDefined(suffix)) {
            applicationId += suffix
        }
        return applicationId
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
