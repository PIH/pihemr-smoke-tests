
PIH-EMR Smoke Tests
=======================

The smoke tests can be run the same way regular tests are run, either through your IDE (in IntelliJ select a test 
class and then click on the Run or Debug icon) or Maven ("mvn run install" to build the module and run the tests).

These are the test suites available. Each runs in GitHub Actions on every build of its distribution: the
repo's `smoke-tests.yml` workflow calls
[`execute-pihemr-smoke-tests.yml`](https://github.com/PIH/openmrs-contrib-distro-tools/blob/main/.github/workflows/execute-pihemr-smoke-tests.yml)
in openmrs-contrib-distro-tools, which starts a temporary instance from the distribution's image and seed image
with the PIH Config below and runs the suite against it.

| Suite         | Run by                                                                                               | PIH Config                                         |
|---------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| `zlCentral`   | [zl-emr](https://github.com/PIH/zl-emr/blob/master/.github/workflows/smoke-tests.yml)                 | `haiti,haiti-central,haiti-local-idgen`            |
| `haiti`       | not run by any workflow                                                                              |                                                    |
| `liberia`     | [pihliberia-emr](https://github.com/PIH/pihliberia-emr/blob/master/.github/workflows/smoke-tests.yml) | `liberia,liberia-harper,liberia-harper-kouka`      |
| `sierraleone` | [pihsl-emr](https://github.com/PIH/pihsl-emr/blob/master/.github/workflows/smoke-tests.yml)           | `sierraLeone,sierraLeone-kgh,sierraLeone-kgh-test` |
| `mexico`      | [ces-emr](https://github.com/PIH/ces-emr/blob/master/.github/workflows/smoke-tests.yml)               | `mexico,mexico-ces-ci`                             |

# Setup

## Chromedriver

Chromedriver is bundled with this repo in `/src/test/resources/chromedriver`. It is presently at version 81.
You should use a version of Chrome with the matching version number.

To check your Chrome version in Linux, execute `google-chrome --version`. 
To upgrade Chrome on Ubuntu (including on the [Bamboo server](https://bamboo.pih-emr.org/allPlans.action))
you should be able to use `sudo apt install google-chrome-stable`.

Other versions of Chromedriver can be found [here](https://chromedriver.storage.googleapis.com/index.html).

If you need to use a different version of Chrome driver, you can specify a different exectuable by using the
CHROME_DRIVER_EXECUTABLE environment variable (or see SmokeTestProperties).

## Config

The following environment variables configure connections to the server and database.

* WEBAPP_URL (default `http://localhost:8080/openmrs`)
* DATABASE_URL (default `jdbc:mysql://localhost:3306/openmrs`)
* DATABASE_USERNAME (default `openmrs`)
* DATABASE_PASSWORD (default `openmrs`)
* WEBAPP_NAME (default `openmrs`)

These variables are parsed by
[SmokeTestProperties.java](https://github.com/PIH/pihemr-smoke-tests/blob/master/src/main/java/org/openmrs/module/pihemr/smoke/helper/SmokeTestProperties.java),
which also provides their defaults. It may sometimes be more convenient to just change these default values locally
in lieu of setting the environment variables.

### Dockerized MySQL

If using the MySQL container used by the OpenMRS SDK, use the following settings.

```
DATABASE_URL=jdbc:mysql://localhost:3308/<server_name>
DATABASE_USERNAME=root
DATABASE_PASSWORD=Admin123
```

Where the database name, `<server_name>`, is the SDK server name.

## Additional setup

* The "admin" user must be a provider and have all roles.
  
  Go to the legacy Admin UI --> Manage Users --> admin. Check the box for
  "Create a provider...". Then check every role box (try using Tab
  and Spacebar).

* The "admin" user must have MFA setup using secret question, with their password as the secret answer.

### For Haiti

* The locale must be "ht"/"Kreyol", at least for the "admin" user.
* Patient Identifier Sources for ZL EMR ID must be configured as described in
    the [Mirebalais README](https://github.com/PIH/openmrs-module-mirebalais#step-7-create-a-local-identifier-source).

### For ZlCentral

* The locale must be "ht"/"Kreyol", at least for the "admin" user.
* Patient Identifier Sources for ZL EMR ID must be configured as described in
    the [Mirebalais README](https://github.com/PIH/openmrs-module-mirebalais#step-7-create-a-local-identifier-source).
* Printers must be configured for all locations.
    This can be configured from the Home Page > System Administration > Printer Administration.
    See the configuration on [humci](https://humci.pih-emr.org/mirebalais/printer/printerAdministration.page)
    for reference. You can use tab and the down arrow to quickly set printers for all the locations.

# Running Tests

Tests can be run using `mvn clean verify -U -P<suite>`, where `<suite>` is one of the values in the
table above. It should also be possible to run the tests by navigating to them in IntelliJ and clicking on
the "Run" buttons in the left margin.

The tests must be configured to use a server that is compatible with the suite being run.

# Using Docker

The most reliable way to execute the smoke tests is by using Docker.  There is a Dockerfile bundled in this repository
which configures an Ubuntu image with the latest version of Chrome and a matching Chrome Driver, Maven and Java, and 
the tests within of this codebase.

A new image for this is published to Dockerhub on every commit at partnersinhealth/pihemr-smoke-tests

Some notes about using this image:

* You need to increase the shared memory in Docker due to some of the page sizes loaded by Chrome:  ```--shm-size=256m```
* You should set all of the necessary environment variables either when running using ```--env VAR=VALUE``` or you can use an env file
* You can run the smoke test command directly or you can run bash and then execute the maven command manually
* If you are connecting to a local DB outside of Docker on your own host, you will need to set host networking:  ``` --net=host```
* You can speed up subsequent executions by mounting a volume at /root/.m2 to cache maven artifacts between runs

As an example, I am able to successfully build a new Docker imageand then run smoke tests in this 
against a local Liberia environment, which is running on my machine via the SDK and using a database named "liberia" in another 
MySQL Docker container exposed on port 3308:

```docker build --rm -t smoketests .```

```
docker run \
    -v /tmp/m2:/root/.m2 \
    --shm-size=256m \
    --env WEBAPP_URL=http://localhost:8080/openmrs \
    --env DATABASE_URL=jdbc:mysql://localhost:3308/liberia \
    --env DATABASE_USERNAME=root \
    --env DATABASE_PASSWORD=root \
    --net=host \
    -it \
    smoketests \
    mvn clean verify -U -Pliberia
```

