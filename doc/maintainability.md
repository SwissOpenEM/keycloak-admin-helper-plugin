Maintainability
===============

This project depends on KeyCloak and uses "unstable" APIs.

Some experiences:

* The Update from 26.1.x to 26.2.0 resulted in some changes in this API and needed some fixes to compile again, but it was
an easy and straight-forward task.
* The update from 26.2.0 to 26.2.4 didn't need any change in the plugin.
* The update from 26.2.4 to 26.2.5 didn't need any change in the plugin.

Currently, the version of keycloak is stored on two locations in the source. Make sure you don't mix multiple versions at the same time:
* `pom.xml` (java dependencies)
* `docker/.env` (for testing using `build-deploy-for-test.sh`)