# Java Test Utilities

Contains code for use in tests, including exception checkers, methods to add data to
[Auth2](https://github.com/kbase/auth2) testmode, and controllers for 3rd party data stores.

## Including the library in your build

See https://jitpack.io/#kbase/java_test_utilities/ for instructions on how to include JitPack built
dependencies in your build.

## JavaDoc

JavaDoc is available at
```
https://javadoc.jitpack.io/com/github/kbase/java_test_utilities/<version>/javadoc/
```

For example:

https://javadoc.jitpack.io/com/github/kbase/java_test_utilities/0.1.0/javadoc/

## Adding and releasing code

* Adding code
  * All code additions and updates must be made as pull requests directed at the develop branch.
    * All tests must pass and all new code must be covered by tests.
    * All new code must be documented appropriately
      * Javadoc
      * General documentation if appropriate
      * Release notes
* Releases
  * The main branch is the stable branch. Releases are made from the develop branch to the main
    branch.
  * Tag the version in git and github.
  * Create a github release.
  * Check that the javadoc is appropriately built on JitPack.

## Testing

```
./gradlew test
```

## TODO

* Add tests
* Javadoc

## Original home

https://github.com/kbase/java_common