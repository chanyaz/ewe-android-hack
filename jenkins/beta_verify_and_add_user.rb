require 'rubygems'
require 'rest_client'
require 'net/ldap'
require 'base64'
require 'pp'
require "csv"
require 'fileutils'


class BuildTestUserList

  attr_reader :ad_alias
  attr_reader :google_alias

  def initialize
    # Unbuffer output
    $stdout.sync = true
    exit_val = 0
    @ad_alias = ARGV[0]
    @google_alias = ARGV[1]

    ldap_authentication
    update_user_list
  end

  def ldap_authentication
    begin
      dn = ENV['LDAP_DN']
      ec_pwd = ENV['LDAP_TOKEN']

      pwd = Base64.decode64(ec_pwd)
      ldap = Net::LDAP.new :host => "ldap.sea.corp.expecn.com",
         :port => 389,
         :auth => {
               :method => :simple,
               :username => dn,
               :password => pwd
         }
      filter = Net::LDAP::Filter.eq("sAMAccountName", @ad_alias)
      treebase = "DC=SEA,DC=CORP,DC=EXPECN,DC=com"

      puts "Searching LDAP for user email (associated with alias #{@ad_alias})"

      ad_email = ""
      ldap.search(:base => treebase, :filter => filter, :attributes => ['mail']) { |item|
        ad_email = item.mail.first
      }
      if ad_email == ""
        puts "ERROR: Cannot find email address for user #{@ad_alias} in Active Directory. Aborting."
        exit 1
      else
      puts "Found email (#{ad_email}) associated with alias #{@ad_alias}"
      end
    rescue  Exception => e
      puts "ERROR: Failed to get user data from LDAP - #{e.message} : #{ e.class }"
      exit 1
    end
  end

  def update_user_list
    begin
        job_name = ENV['JOB_NAME']
        user_list_file = "test-users.csv"

        s3_current_bucket = "s3://ewe-mobile-ci/#{job_name}/current/"
        s3_current_file = "#{s3_current_bucket}#{user_list_file}"

        s3_backup_bucket = "s3://ewe-mobile-ci/#{job_name}/backup/"

        #puts "Current bucket = #{s3_current_bucket}"
        #puts "Backup bucket = #{s3_backup_bucket}"

        `aws s3 cp #{s3_current_file} .`

        backup_dir = "backup"
        FileUtils.mkdir_p(backup_dir)
        FileUtils.cp(user_list_file, backup_dir)

        if File.file?(user_list_file)
            CSV.open(user_list_file, "a") do |csv|
                csv << [@ad_alias, @google_alias]
            end
        else
            puts "Could not find User list file #{user_list_file}"
            exit 1
        end

        `aws s3 cp backup/test-users.csv #{s3_backup_bucket}`
        `aws s3 cp test-users.csv #{s3_current_bucket}`

        puts "Successfully updated User list"
    rescue  Exception => e
      puts "ERROR: Failed to update CSV file - #{e.message} : #{ e.class }"
      exit 1
    end
  end
  
  BuildTestUserList.new
end
