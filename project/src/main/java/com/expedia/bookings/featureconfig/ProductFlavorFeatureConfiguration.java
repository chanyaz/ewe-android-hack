package com.expedia.bookings.featureconfig;

public class ProductFlavorFeatureConfiguration {
	public static BaseFeatureConfiguration getInstance() {
		return ProductFlavorFeatureConfigurationHolder.INSTANCE;
	}

	private static class ProductFlavorFeatureConfigurationHolder {
		private static final BaseFeatureConfiguration INSTANCE = getProductFlavorFeatureConfiguration();
	}

	private static BaseFeatureConfiguration getProductFlavorFeatureConfiguration() {
		return new FeatureConfiguration();
	}
}
