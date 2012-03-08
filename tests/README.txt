build with ant
==============

Since our tests use the logging from ../lib/Utils you may be tempted to include
it as a library. DO NOT DO THIS!

You will end up duplicating the implementations and you will get an IllegalAcess
from the VM.

So to build the test suite with ant, first compile EH as normal then you need to
symlink and build as follows:

    cd $ExpediaHotelsRoot
    ln -s lib/Utils/bin/classes.jar project/libs/utils.jar
    cd tests
    ant debug
    rm ../project/libs/utils.jar

You cannot build the main Expedia package with the symlink in place, it will
think things are duplicated.

build with eclipse
==================

The problem is similar but easier and less hacky to fix. Just configure Utils to
be an "exported" jar in the ExportAndBuildPath section of the eclipse config.
Help on this is far easier to find than the previous.

