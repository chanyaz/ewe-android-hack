#! /usr/bin/perl
#
#

if ($#ARGV < 2) {
  print "Usage: change_version.pl <path/to/build.gradle> <flavor> <version>\n";
  exit;
}

my $fullContent = "";
my $flavor = $ARGV[1];
my ($major, $minor, $patch, $build) = split(/\./, $ARGV[2]);

$patch //= 0;
$build //= 0;

open (BUILDFILE, $ARGV[0]);
while (<BUILDFILE>) {
  $fullContent = $fullContent . $_;
}
close BUILDFILE;

if ($fullContent =~ /(\A.*?$flavor \{)([^\}]*)(\}.*?\z)/s) {
  my $prefix = $1;
  my $flavorContent = $2;
  my $postfix = $3;

  $flavorContent =~ s/(\A.*?def major = )\d+(.*?\z)/$1$major$2/s;
  $flavorContent =~ s/(\A.*?def minor = )\d+(.*?\z)/$1$minor$2/s;
  $flavorContent =~ s/(\A.*?def patch = )\d+(.*?\z)/$1$patch$2/s;
  $flavorContent =~ s/(\A.*?def build = )\d+(.*?\z)/$1$build$2/s;

  open(BUILDFILE, '>', $ARGV[0]);
  print BUILDFILE $prefix . $flavorContent . $postfix;
  close BUILDFILE;
}

