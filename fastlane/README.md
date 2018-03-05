fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android screenshots
```
fastlane android screenshots
```
Run a marketing screenshot sweep
### android release
```
fastlane android release
```
Upload release to Google Play
### android branch_and_bump
```
fastlane android branch_and_bump
```
Create a new release branch and bump the version on the base branch. Optionally protects the newly created release branch.
### android automation_integrationTests
```
fastlane android automation_integrationTests
```
Runs tests with usage of emulator plugin

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
