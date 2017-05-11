#! /usr/bin/perl
#
#

if ($#ARGV < 1) {
  print "Usage: change_version.pl <path/to/build.gradle> <flavor-version> <flavor-version>...<flavor-version> \n";
  exit;
}

my @params = @ARGV[1..@ARGV-1];
my %flavor_versions;

foreach my $param(@params) {
  my ($flavor,$version)= split(/-/, $param);
  $flavor_versions{$flavor} = $version;
}

$patch //= 0;
$build //= 0;

for my $flavor(keys %flavor_versions) {

  my $fullContent = "";
  open(BUILDFILE, '+<', $ARGV[0]);
  while (<BUILDFILE>) {
    $fullContent = $fullContent . $_;
  }
  seek(BUILDFILE,0,0);

  my ($major, $minor, $patch, $build) = split(/\./, $flavor_versions{$flavor});
  for ($major, $minor, $patch, $build) { $_ = 0 if $_ eq ''; }

  if ($fullContent =~ /(\A.*?$flavor \{)([^\}]*)(\}.*?\z)/s) {
    my $prefix = $1;
    my $flavorContent = $2;
    my $postfix = $3;

    $flavorContent =~ s/(\A.*?def major = )\d+(.*?\z)/$1$major$2/s;
    $flavorContent =~ s/(\A.*?def minor = )\d+(.*?\z)/$1$minor$2/s;
    $flavorContent =~ s/(\A.*?def patch = )\d+(.*?\z)/$1$patch$2/s;
    $flavorContent =~ s/(\A.*?def build = )\d+(.*?\z)/$1$build$2/s;

    print BUILDFILE $prefix . $flavorContent . $postfix;
  }
  close BUILDFILE;
}
