# Base options for all proguard configs

-optimizationpasses 6
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontobfuscate
-verbose

# Need this so that enums dont get optimized in a way which messes up compilation
-optimizations !code/allocation/variable

-libraryjars android.jar

