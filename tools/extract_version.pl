#! /usr/bin/perl
#
#

if ($#ARGV < 0) {
  print "Usage: extract_version.pl <path/to/build.gradle>\n";
  exit;
}

my $fullContent = "";

$patch //= 0;
$build //= 0;

open (BUILDFILE, $ARGV[0]);
while (<BUILDFILE>) {
  $fullContent = $fullContent . $_;
}
close BUILDFILE;

if ($fullContent =~ /.*?def major = (\d+).*?def minor = (\d+).*?def patch = (\d+).*?def build = (\d+)/s) {
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
