require 'csv'
require 'rest_client'
require 'net/ldap'
require 'base64'

class ReconcileAndGetBetaUserList

	attr_reader :job_name_for_bucket

	def initialize
    	$stdout.sync = true
    	@job_name_for_bucket = ARGV[0]
    	@user_list_file = "test-users.csv"

    	reconcile
    	create_csv_with_google_id
	end

	def reconcile
		begin
        	s3_current_bucket = "s3://ewe-mobile-ci/#{@job_name_for_bucket}/current/"
        	s3_current_file = "#{s3_current_bucket}#{@user_list_file}"

        	`aws s3 cp #{s3_current_file} .`

        	users = CSV.read(@user_list_file)

        	dn = "CN=s-ewebuildprop,OU=Service Accounts,OU=Non-User,DC=SEA,DC=CORP,DC=EXPECN,DC=com"
      		ec_pwd = "JS5qcDVMVXRgazpsUXlTJQ=="

      		pwd = Base64.decode64(ec_pwd)
      		ldap = Net::LDAP.new :host => "ldap.sea.corp.expecn.com",
         		:port => 389,
         		:auth => {
               		:method => :simple,
               		:username => dn,
               		:password => pwd
         	}
      		
      		treebase = "DC=SEA,DC=CORP,DC=EXPECN,DC=com"

      		delete_list = []
        	users.each_with_index {|val, index| 
        		ldap_alias = val[0]
        		puts "Searching LDAP for user email (associated with alias #{ldap_alias})"

        		filter = Net::LDAP::Filter.eq("sAMAccountName", ldap_alias)
        		ad_email = ""
      			ldap.search(:base => treebase, :filter => filter, :attributes => ['mail']) { |item|
        			ad_email = item.mail.first
      			}
      			if ad_email == ""
        			puts "ERROR: Cannot find email address for user #{@ldap_alias} in Active Directory."
        			delete_list << index
      			else
      				puts "Found email (#{ad_email}) associated with alias #{@ldap_alias}"
      			end
        	}
        	
        	if !delete_list.empty?
        		total_deleted = 0
	        	delete_list.each do |delete_index|
	        		row_to_delete = delete_index - total_deleted
	        		puts "Deleting entry for #{users[row_to_delete]}"
	    			users.delete_at(row_to_delete)
	    			total_deleted = total_deleted + 1
				end

				CSV.open('test-users.csv', 'w') do |csv_object|
	  				users.each do |row_array|
	    				csv_object << row_array
	  				end
				end

				`aws s3 cp test-users.csv #{s3_current_bucket}`
			else
				puts "Nothing to remove"
        	end
        	

		rescue Exception => e
			puts "ERROR: Failed to reconcile - #{e.message} : #{ e.class }"
      		exit 1
		end

		
	end

	def create_csv_with_google_id
		users = CSV.read(@user_list_file)
		
		CSV.open('test-users-google-ids.csv', 'w') do |csv_object|
			users.each do |row_array|
				csv_object << [row_array[1]]
			end
		end
	end

	ReconcileAndGetBetaUserList.new
end
