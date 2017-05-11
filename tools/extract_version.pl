#! /usr/bin/perl
#
#

if ($#ARGV < 1) {
  print "Usage: extract_version.pl <path/to/build.gradle> <flavor 1> <flavor 2>..<flavor n>\n";
  exit;
}

my $fullContent = "";
my @flavors = @ARGV[1..@ARGV-1];
my @version_numbers;

$patch //= 0;
$build //= 0;

open (BUILDFILE, $ARGV[0]);
while (<BUILDFILE>) {
  $fullContent = $fullContent . $_;
}
close BUILDFILE;

foreach my $flavor (@flavors) {
  if ($fullContent =~ /(\A.*?$flavor \{)([^\}]*)(\}.*?\z)/s) {
    my $flavorContent = $2;

    if ($flavorContent =~ /.*?def major = (\d+).*?def minor = (\d+).*?def patch = (\d+).*?def build = (\d+)/s) {
      my $major = $1;
      my $minor = $2;
      my $patch = $3;
      my $build = $4;

      $version = $major . "." . $minor;
      if ($patch != 0 || $build != 0) {
        $version .= "." . $patch;
        if ($build != 0) {
          $version .= "." . $build;
        }
      }
      if($version eq "") { break; }
      push @version_numbers, $version;
    }
    else {
      break;
    }
  }
  else {
    break;
  }
}

if(scalar @version_numbers eq scalar @flavors) {
  print join(',',@version_numbers);
}
else {
  print "";
}