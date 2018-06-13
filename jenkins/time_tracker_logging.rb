#!/usr/bin/ruby

require "csv"
require "json"
require "net/http"

class TimeTrackerLogging
  attr_reader :gradle_task
  attr_reader :total_time

  def build_time_logging
    gradle_task = ARGV[0]
    if !ENV["BUILD_NUMBER"].to_s.empty?
      @gradle_task = "ci_" + gradle_task
    end
    @total_time = read_csv_to_get_total_time
    send_build_time_to_client_log(total_time, gradle_task)
  end

  def read_csv_to_get_total_time
    time = 0
    # Read all line
    lines = CSV.open("build/times.csv").readlines

    # Collect the keys
    keys = lines.delete lines.first

    # Extract total build time.
    build_time_log = lines.map do |values|
      Hash[keys.zip(values)]
    end

    build_time_log.each { |value|
      time = time + value["ms"].to_i
    }

    time
  end

  def send_build_time_to_client_log(total_time, gradle_task)
    # Log build time to client log, gradle_task contains the task information.
    # For example, clean_installExpediaDebug or build_cache_clean_installExpediaDebug
    begin
      url = "https://www.expedia.com/cl/1x1.gif?live=true&pageName=App.Build.Time&domain=www.expedia.com&requestToUser=#{total_time}&DeviceType=#{gradle_task}"
      uri = URI(url)
      response = Net::HTTP.get(uri)
    rescue StandardError
      false
    end
  end

  TimeTrackerLogging.new.build_time_logging
end
