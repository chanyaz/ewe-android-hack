#! /usr/bin/perl
#
#

if ($#ARGV < 1) {
  print "Usage: extract_version.pl <path/to/build.gradle> <flavor>\n";
  exit;
}

my $fullContent = "";
my $flavor = $ARGV[1];

$patch //= 0;
$build //= 0;

open (BUILDFILE, $ARGV[0]);
while (<BUILDFILE>) {
  $fullContent = $fullContent . $_;
}
close BUILDFILE;

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

    print $version;
  }
}

