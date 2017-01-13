require 'screengrab/android_environment'

module Fastlane
  module Actions
    module SharedValues
    end

    class GetApkVersionCodeAction < Action
      def self.run(params)
        android_environment = Screengrab::AndroidEnvironment.new(params[:android_home], nil)
        FastlaneCore::CommandExecutor.execute(command: "#{android_environment.aapt_path} dump badging #{params[:apk_file]} | grep versionCode | sed -e \"s/.*versionCode='\\([0-9][0-9]*\\)'.*/\\1/\"", print_all: true, print_command: true)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Gets the versionCode from a given APK file"
      end

      def self.details
        # Optional:
        # this is your chance to provide a more detailed description of this action
      end

      def self.available_options
        # Define all options your action supports.
        [
          FastlaneCore::ConfigItem.new(key: :android_home,
                                       env_name: "FL_GET_APK_VERSION_CODE_ANDROID_HOME",
                                       description: "Location of the Android SDK",
                                       type: String,
                                       default_value: ENV["ANDROID_HOME"]
                                       ),
          FastlaneCore::ConfigItem.new(key: :apk_file,
                                       env_name: "FL_GET_APK_VERSION_CODE_APK_FILE",
                                       description: "The APK file from which to get the version code",
                                       type: String,
                                       optional: false)
        ]
      end

      def self.output
        # Define the shared values you are going to provide
      end

      def self.return_value
        # If you method provides a return value, you can describe here what it does
        "Returns the versionCode from the APK file"
      end

      def self.authors
        # So no one will ever forget your contribution to fastlane :) You are awesome btw!
        ["scottdweber"]
      end

      def self.is_supported?(platform)
        platform == :android
      end
    end
  end
end
