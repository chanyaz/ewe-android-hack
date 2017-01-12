module Fastlane
  module Actions
    module SharedValues
    end

    class ImportLocalizedReleaseNotesAction < Action
      def self.run(params)

        require 'csv'
        require 'fileutils'

        data = CSV.read(params[:csv_file])

        data[0].each_with_index do |lang, index|
          if !lang
            lang = "en_US"
          end

          lang = convert_lang(lang)

          filename = "#{params[:metadata_path]}/#{lang}/changelogs/#{params[:version_code]}.txt"
          dirname = File.dirname(filename)
          unless File.directory?(dirname)
            FileUtils.mkdir_p(dirname)
          end

          File.open(filename, "w") { |file|
            data[1..-1].each do |row|
              file.puts(row[index])
            end
          }
        end
      end

      class << self
        private

        def convert_lang(lang)
          language_mapping = {
            'es-LA' => 'es-419',
            'id-ID' => 'id',
            'nb-NO' => 'no-NO',
            'th-TH' => 'th',
            'vi-VN' => 'vi'
          }

          lang = lang.gsub(/_/, "-")

          if language_mapping.key?(lang)
            lang = language_mapping[lang]
          end

          lang
        end
      end
      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Prepare localized release notes for use by supply."
      end

      def self.details
        # Optional:
        # this is your chance to provide a more detailed description of this action
        "Extracts localized release notes from a CSV file into separate files for each language."
      end

      def self.available_options
        # Define all options your action supports. 
        
        # Below a few examples
        [
          FastlaneCore::ConfigItem.new(key: :csv_file,
                                       env_name: "FL_IMPORT_LOCALIZED_RELEASE_NOTES_CSV_FILE",
                                       description: "Path to the CSV file that contains the release notes",
                                       type: String,
                                       optional: false
                                       ),
          FastlaneCore::ConfigItem.new(key: :version_code,
                                       env_name: "FL_IMPORT_LOCALIZED_RELEASE_NOTES_VERSION_CODE",
                                       description: "Version Code for the APK to which these release notes apply",
                                       type: String,
                                       optional: false
                                       ),
          FastlaneCore::ConfigItem.new(key: :metadata_path,
                                       env_name: "FL_IMPORT_LOCALIZED_RELEASE_NOTES_METADATA_PATH",
                                       description: "Path to the folder under which the release notes should be stored",
                                       type: String,
                                       default_value: File.join("fastlane", "metadata", "android")
                                       )
        ]
      end

      def self.output
        # Define the shared values you are going to provide
      end

      def self.return_value
        # If you method provides a return value, you can describe here what it does
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
