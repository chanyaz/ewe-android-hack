package com.expedia.bookings.featureconfig;

public class ProductFlavorFeatureConfiguration {
	public static IProductFlavorFeatureConfiguration getInstance() {
		return ProductFlavorFeatureConfigurationHolder.INSTANCE;
	}

	private static class ProductFlavorFeatureConfigurationHolder {
		private static final IProductFlavorFeatureConfiguration INSTANCE = getProductFlavorFeatureConfiguration();
	}

	private static IProductFlavorFeatureConfiguration getProductFlavorFeatureConfiguration() {
		return new FeatureConfiguration();
	}
}
