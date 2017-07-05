module Fastlane
  module Actions
    module SharedValues
    end

    class ProtectBranchAction < Action
      def self.run(params)
        require 'excon'
        require 'base64'

        UI.message("Protecting branch '#{params[:branch]}' of '#{params[:repo]}'")

        url = "#{params[:api_url]}/repos/#{params[:repo]}/branches/#{params[:branch]}/protection"
        headers = {
          'User-Agent' => 'fastlane-protect_branch',
          'Accept' => 'application/vnd.github.loki-preview+json'
        }
        headers['Authorization'] = "Basic #{Base64.strict_encode64(params[:api_token])}" if params[:api_token]

        data = {
          'required_status_checks' => {
            'include_admins' => true,
            'strict' => false,
            'contexts' => params[:required_checks].split(',')
          },
          'required_pull_request_reviews' => {
            'include_admins' => true
          },
          'enforce_admins' => true,
          'restrictions' => nil
        }

        response = Excon.put(url, headers: headers, body: data.to_json)

        if response[:status] == 200
          UI.success("Successfully protected branch #{params[:branch]}.")
        elsif response[:status] != 200
          UI.error("GitHub responded with #{response[:status]}: #{response[:body]}")
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Protect a branch on GitHub (uses experimental API)"
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(key: :api_token,
                                       env_name: "GITHUB_PROTECT_BRANCH_API_TOKEN",
                                       description: "Personal API Token for GitHub - generate one at https://github.com/settings/tokens",
                                       sensitive: true,
                                       default_value: ENV["GITHUB_API_TOKEN"],
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :repo,
                                       env_name: "GITHUB_PROTECT_BRANCH_REPO",
                                       description: "The name of the repository on which you want to protect a branch",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :branch,
                                       env_name: "GITHUB_PROTECT_BRANCH_BRANCH",
                                       description: "The name of the branch you want to protect",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :required_checks,
                                       env_name: "GITHUB_PROTECT_BRANCH_REQUIRED_CHECKS",
                                       description: "A comma separated list of required checks",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :api_url,
                                       env_name: "GITHUB_PROTECT_BRANCH_API_URL",
                                       description: "The URL of Github API - used when the Enterprise (default to `https://api.github.com`)",
                                       type: String,
                                       default_value: 'https://api.github.com',
                                       optional: true)
        ]
      end

      def self.author
        ["scottdweber"]
      end

      def self.is_supported?(platform)
        return true
      end

      def self.example_code
        [
          'protect_branch(
            api_token: ENV["GITHUB_TOKEN"],
            repo: "fastlane/fastlane",
            branch: "r/fastlane-1.111.0",
            required_checks: "tests,checkstyle",
            api_url: "http://yourdomain/api/v3" # optional, for Github Enterprise, defaults to "https://api.github.com"
          )'
        ]
      end

      def self.category
        :source_control
      end
    end
  end
end