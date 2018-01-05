#!/bin/bash -eu

BUILD_DIR=$1
GENERATED_DIR=$BUILD_DIR/generated/source/kapt/expediaDebug/com/expedia/bookings/utils

function generate_file() {
cat << EOM
package com.expedia.bookings.utils;

import javax.annotation.Generated;

/**
 * Dummy file so that lint don't complain about unused resources which are used only in kotlin files.
 * Have to delete this file once Lint is able to read kotlin files.
 */
@Generated(
value = "tools/create_dummy_file.sh",
comments = "don't edit this file"
)
public class GeneratedDummyFileToHandleKotlinLintError {

	/**
	 * Add resources which are only used in kotlin files.
	 */
	private static final int[] USED_RESOURCES = {
EOM

RESOURCE_PATTERN="\(android\.\)\?R\.\(string\|layout\|drawable\|color\|raw\|menu\|dimen\|anim\|array\|integer\|plurals\|style\)\.[a-zA-Z0-9_]\+"
grep --only-matching --no-filename -R --include="*\.kt" "$RESOURCE_PATTERN" .. | grep -v "android." | uniq | sed -e "s/^/		com.expedia.bookings./" -e "s/$/,/"

cat << EOM
	};
}
EOM
}
pwd
mkdir -p "$GENERATED_DIR"
generate_file > "$GENERATED_DIR/GeneratedDummyFileToHandleKotlinLintError.java"
wc -l "$GENERATED_DIR/GeneratedDummyFileToHandleKotlinLintError.java"

