import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidExpediaPlugin implements Plugin<Project> {
    // Path to file in src tree with ContentProvider authority declarations
    final def contentProviderBuildStringsPath = "project/src/main/res/values/gradle_content_provider_authorities.xml"
    def rootDir = ""
    def jenkinsBuildNumber = "$System.env.BUILD_NUMBER"

    void apply(Project project) {
        def hasAppPlugin = project.plugins.hasPlugin AppPlugin
        def hasLibraryPlugin = project.plugins.hasPlugin LibraryPlugin
        def log = project.logger

        rootDir = project.rootDir

        // Ensure the Android plugin has been added in app and not library
        if (!hasAppPlugin || hasLibraryPlugin) {
            throw new IllegalStateException("This plugin only supports having the 'android' plugin applied, not library projects")
        }

        BasePlugin plugin = project.plugins.getPlugin(AppPlugin);
        def variants = project.android.applicationVariants

        // This block iterates over each build variant and injects tasks to
        // (1) Overwrite AndroidManifest values
        // (2) Write Android resource values overrides
        variants.all { variant ->
            // Android values resources to be written for override
            def resourceOverridePairs = []
            def contentProviderOverridePairs = []

            def uniqueName = variant.productFlavors.get(0).name + variant.buildType.name.capitalize()

            // Create a build variant-unique task for overriding Android resource value pairs.
            def resourceTask = "customResourceOverride$uniqueName"
            project.task(resourceTask) << {
                generateUniqueContentProviderStrings(variant, contentProviderBuildStringsPath, contentProviderOverridePairs)
                generateHomescreenLauncherAppNameLabel(project, variant, resourceOverridePairs)
                generateAccountManagerTypeToken(variant, resourceOverridePairs)

                writeOverridesToStringResourceFile(resourceOverridePairs, contentProviderOverridePairs, getCustomOverrideResDir(variant))
            }

            // Insert our task between mergeResources and processResources. This ensures that the file exists
            // and also that we modify before the values are consumed.
            variant.outputs[0].processResources.dependsOn resourceTask
            project.tasks[resourceTask].dependsOn variant.mergeResources

            // Create a task for overwriting and injecting content in the generated AndroidManifest
            def manifestTask = "overwriteGeneratedManifest$uniqueName"
            project.task(manifestTask) << {
                def manifestFile = variant.outputs[0].processManifest.manifestOutputFile
                def manifest = new XmlParser().parse(manifestFile)
                def applicationNode = manifest.application.get(0)

                def namespace = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", 'android')
                def packageName = GradleUtil.getPackageName(variant)

                modifyPushNotificationPermission(manifest, namespace, packageName)
                modifyContentProviderAuthorities(applicationNode, namespace, contentProviderOverridePairs)

                // Write the Manifest modifications back to file
                def xmlWriter = new XmlNodePrinter(new PrintWriter(new FileWriter(manifestFile)))
                xmlWriter.setPreserveWhitespace(true)
                xmlWriter.print(manifest)
            }

            // Insert our task between processManifest and processResources. This ensures that the Manifest
            // has been generated and that we modify it before it's values get consumed.
            variant.outputs[0].processResources.dependsOn manifestTask
            project.tasks[manifestTask].dependsOn variant.outputs[0].processManifest

        }
    }

    /////////////////// HOMESCREEN LAUNCHER LABEL ///////////////////

    def generateHomescreenLauncherAppNameLabel(project, buildVariant, resourceOverridePairs) {
        def appNameLabel = GradleUtil.getAppName(project, buildVariant)
        if (GradleUtil.isDefined(appNameLabel)) {
            resourceOverridePairs.add(new AndroidResource("string", "launcher_name", appNameLabel))
        }
    }

    /////////////////// ACCOUNTMANAGER ///////////////////

    def generateAccountManagerTypeToken(buildVariant, resourceOverridePairs) {
        def accountToken = AccountManagerTypeTokens.getToken(buildVariant)
        resourceOverridePairs.add(new AndroidResource("string", "expedia_account_token_type_tuid_identifier", accountToken))
        resourceOverridePairs.add(new AndroidResource("string", "expedia_account_type_identifier", GradleUtil.getPackageName(buildVariant)))
    }

    /////////////////// CONTENTPROVIDERS AND THE LIKE ///////////////////

    def generateUniqueContentProviderStrings(buildVariant, contentProviderBuildStringsPath, contentProviderOverridePairs) {
        def prefix = GradleUtil.getPackageName(buildVariant) + "."
        def xmlStringsFileAsNode = new XmlParser().parse(new File(rootDir, contentProviderBuildStringsPath))
        xmlStringsFileAsNode.each {
            contentProviderOverridePairs.add(new AndroidResource("string", it.attributes()["name"], prefix + it.text()))
        }
    }

    def modifyContentProviderAuthorities(applicationNode, namespace, contentProviderOverridePairs) {
        applicationNode.provider.each { provider ->
            contentProviderOverridePairs.each { androidResource ->
                def manifestKey = "@string/" + androidResource.getName()
                if (provider.attributes()[namespace.authorities] == manifestKey) {
                    provider.attributes()[namespace.authorities] = androidResource.getValue()
                }
            }
        }
    }

    /////////////////// PUSH NOTIFICATIONS ///////////////////

    def modifyPushNotificationPermission(manifestNode, namespace, packageName) {
        // Push notification permission string
        manifestNode.permission.each { permission ->
            if (permission.attributes()[namespace.name] == "com.expedia.bookings.permission.C2D_MESSAGE") {
                def orig = permission.attributes()[namespace.name]
                permission.attributes()[namespace.name] = orig.replaceAll("com.expedia.bookings", packageName)
            }
        }
        manifestNode."uses-permission".each { usesPermission ->
            if (usesPermission.attributes()[namespace.name] == "com.expedia.bookings.permission.C2D_MESSAGE") {
                def orig = usesPermission.attributes()[namespace.name]
                usesPermission.attributes()[namespace.name] = orig.replaceAll("com.expedia.bookings", packageName)
            }
        }
        manifestNode.application.receiver.each { receiver ->
            if (receiver.attributes()[namespace.permission] == "com.google.android.c2dm.permission.SEND") {
                receiver."intent-filter".category.each { category ->
                    def orig = category.attributes()[namespace.name]
                    category.attributes()[namespace.name] = orig.replaceAll("com.expedia.bookings", packageName)
                }
            }
        }
    }

    /////////////////// RESOURCE OVERRIDE FILE WRITE ///////////////////

    def writeOverridesToStringResourceFile(resourceOverridePairs, contentProviderOverridePairs, overrideResValuesDir) {
        createDir(overrideResValuesDir)

        def writer = new StringWriter()
        writer.write("""<?xml version="1.0" encoding="utf-8"?>\n""")

        def xml = new groovy.xml.MarkupBuilder(writer)
        xml.setDoubleQuotes(true)
        xml.resources() {
            resourceOverridePairs.each { androidResource ->
                "${androidResource.getType()}"(name: androidResource.getName(), androidResource.getValue())
            }

            contentProviderOverridePairs.each { androidResource ->
                "${androidResource.getType()}"(name: androidResource.getName(), androidResource.getValue())
            }
        }

        def file = new File(overrideResValuesDir + "string_values_overrides.xml")
        file.write(writer.toString())
    }

    /////////////////// UTILITIES ///////////////////

    def getCustomOverrideResDir(buildVariant) {
        return buildVariant.mergeResources.outputDir.getAbsolutePath() + "/values-v1/"
    }

    def createDir(dir) {
        def file = new File(dir)
        file.mkdirs()
    }
}
