#!/usr/bin/env bash
BRAND_PATH_NAME=("main" "airAsiaGo" "cheapTickets" "ebookers" "lastMinute" "mrjet" "orbitz" "samsung" "travelocity" "voyages" "wotif")
DENSITY_PATHS=("hdpi" "mdpi" "xhdpi" "xxhdpi" "xxxhdpi")
asset_name=$1

function removeAssets() {
  for brand in "${BRAND_PATH_NAME[@]}"
     do
       rm "project/src/$brand/res/drawable/$asset_name"
       for density_path in "${DENSITY_PATHS[@]}"
         do
           ASSET_PATH="project/src/$brand/res/drawable-$density_path/$asset_name"
           rm $ASSET_PATH
       done
  done
}

removeAssets