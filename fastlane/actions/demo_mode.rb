module Fastlane
  module Actions
    module SharedValues
    end

    class DemoModeAction < Action
      def self.run(params)
        adb = Helper::AdbHelper.new(adb_path: "adb")
        if params[:enable]
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command enter", serial: "")
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command battery -e plugged false", serial: "")
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command battery -e level 100", serial: "")
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4", serial: "")
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e datatype none -e level 4", serial: "")
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false", serial: "")
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0900", serial: "")
        else
          result = adb.trigger(command: "shell am broadcast -a com.android.systemui.demo -e command exit", serial: "")
        end
        return result
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Enables or disables demo mode on a connected Android device"
      end

      def self.details
        # Optional:
        # this is your chance to provide a more detailed description of this action
      end

      def self.available_options
        # Define all options your action supports. 
        [
          FastlaneCore::ConfigItem.new(key: :enable,
                                       env_name: "FL_DEMO_MODE_ENABLE", # The name of the environment variable
                                       description: "Whether to enable demo mode", # a short description of this parameter
                                       is_string: false)
        ]
      end

      def self.output
        # Define the shared values you are going to provide
        # Example
      end

      def self.return_value
        # If you method provides a return value, you can describe here what it does
        "The output of the final adb command"
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
