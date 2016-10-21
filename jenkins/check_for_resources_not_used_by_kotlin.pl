$allKotlinContent = `find . -name *.kt -exec cat {} \\;`;
my @unusedResources;

while(<>) {
  if (s/^\s+R/R/) {
    chomp ;
    if (/,$/) {
      chop ;
    }
    
    if ($allKotlinContent !~ /$_/) {
      push @unusedResources, $_
    }
  }
}

if (scalar(@unusedResources) > 0) {
  print "The following resources in DummyFileToHandleKotlinLintError are not referenced in any Kotlin files:\n";
  print join(",\n", @unusedResources) . "\n";
  exit 1
}

exit 0