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

    static def getPropertyWithDefault(Project project, String key, String thedefault) {
        if (project.hasProperty(key)) {
            return project.getProperty(key).toString()
        }

        return thedefault
    }

    static def getFeatureName(Project project) {
        return getPropertyWithDefault(project, "featureName", "none")
    }

    static def getAppName(Project project, variant) {
        // Don't touch release builds
        if (variant.buildType.name == "release") {
            return null
        }

        def flavor = variant.productFlavors.get(0).name
        flavor = flavor.capitalize()

        def type = variant.buildType.name
        if (type == "feature") {
            type = GradleUtil.getFeatureName(project)
        }
        type = type.capitalize()

        return flavor + " " + type
    }

    static def getGitRevision() {
        def hash = "git rev-parse --short HEAD".execute().text.trim()
        def hasLocalChanges = "git diff".execute().text
        if (hasLocalChanges) {
            hash = "!" + hash
        }
        return hash
    }
}
