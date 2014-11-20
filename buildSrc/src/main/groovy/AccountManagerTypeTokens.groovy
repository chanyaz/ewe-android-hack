class AccountManagerTypeTokens {

    // This class manages the generation of a unique AccountManager type token. It also encapsulates
    // logic required for production app compatibility purposes.

    // Production apps before the dawn of this class refer to "expedia.tuid" and "vsc.tuid"
    static def packageNameTokenPairsThatCannotChange = [
        'com.expedia.bookings' : 'expedia.tuid',
        'com.expedia.bookings.vsc' : 'vsc.tuid',
        'com.travelocity.android' : 'travelocity.tuid',
    ]

    static final def TOKEN_SUFFIX = ".tuid"

    static def getToken(buildVariant) {
        def packageName = GradleUtil.getPackageName(buildVariant)

        if (packageNameTokenPairsThatCannotChange.containsKey(packageName)) {
            return packageNameTokenPairsThatCannotChange[packageName]
        }
        else {
            def buildTypeName = buildVariant.buildType.name
            def flavorName = buildVariant.productFlavors.get(0).name
            return buildTypeName + "." + flavorName + TOKEN_SUFFIX
        }
    }

}
