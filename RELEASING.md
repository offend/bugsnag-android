# Releasing a New Version

If you are a project maintainer, you can build and release a new version of
`bugsnag-android` as follows:

## One-time setup

-   Create a GPG key if you haven't got one already (`gpg --gen-key`). The build system requires a GPG key ring set up using GPG 1.x, but many systems now ship with GPG 2.x. As a workaround, after creating your key you can manually create the `secring.gpg` file by running `gpg --export-secret-keys >~/.gnupg/secring.gpg`
-   Create a [Bintray](https://bintray.com/signup/oss) account, and ask a Bugsnag admin to add you to the organisation
-   Create a [Sonatype JIRA](https://issues.sonatype.org) account
-   Ask in the [Bugsnag Sonatype JIRA ticket](https://issues.sonatype.org/browse/OSSRH-5533) to become a contributor
-   Ask an existing contributor (likely Simon) to confirm in the ticket
-   Wait for Sonatype them to confirm the approval
-   Create a file `~/.gradle/gradle.properties` with the following contents:

    ```ini
    # Your credentials for https://oss.sonatype.org/
    # NOTE: An equals sign (`=`) in any of these fields will break the parser
    # NOTE: Do not wrap any field in quotes

    NEXUS_USERNAME=your-nexus-username
    NEXUS_PASSWORD=your-nexus-password
    nexusUsername=your-nexus-username
    nexusPassword=your-nexus-password

    # Your credentials for Bintray
    # From https://bintray.com/profile
    bintray_user=your-bintray-username
    # Get your Bintray API key from https://bintray.com/profile/edit > API Key
    bintray_api_key=your-api-key

    # GPG key details
    # Your key must be added to a public key server, such as http://keys.gnupg.net:
    # 1. Get your key id by running `gpg --list-keys --keyid-format=short`. It
    #    should be 8-character hexadecimal.
    # 2. Export your key using `gpg --armor --export <key-id>`
    # 3. Upload to a server using `gpg --keyserver hkp://keys.gnupg.net --send-keys <key-id>`
    signing.keyId=<key-id>
    signing.password=your-gpg-key-passphrase
    signing.secretKeyRingFile=/Users/{username}/.gnupg/secring.gpg
    ```

## Every time

### Pre-release Checklist

- [ ] Has the full test suite been triggered on Buildkite and does it pass?
- [ ] Does the build pass on the CI server?
- [ ] Are all Docs PRs ready to go?
- [ ] Do the installation instructions work when creating an example app from scratch?
- [ ] Has all new functionality been manually tested on a release build?
  - [ ] Ensure the example app sends an unhandled error
  - [ ] Ensure the example app sends a handled error
  - [ ] If a response is not received from the server, is the report queued for later?
  - [ ] If no network connection is available, is the report queued for later?
  - [ ] On a throttled network, is the request timeout reasonable, and the main thread not blocked by any visible UI freeze? (Throttling can be achieved by setting both endpoints to "https://httpstat.us/200?sleep=5000")
  - [ ] Are queued reports sent asynchronously?
- Native functionality checks:
  - [ ] Rotate the device before notifying. Is the orientation at the time
    persisted in the report on the dashboard?
  - [ ] Rotate the device before causing a native crash. Is the orientation at
    the time of the crash persisted in the report on the dashboard?
  - [ ] Wait a few seconds before a native crash. Does the reported duration in
    foreground match your expectation? Is the value for "inForeground" correct?
  - [ ] Do the function names demangle correctly when using notify?
- [ ] Have the installation instructions been updated on the [dashboard](https://github.com/bugsnag/dashboard-js/tree/master/js/dashboard/components/integration_instructions) as well as the [docs site](https://github.com/bugsnag/docs.bugsnag.com)?
- [ ] Do the installation instructions work for a manual integration?

### Making the release

- Make a PR to release the following changes to master, creating a release
  branch from the "next" branch if this is a feature release:
  - [ ] Update the version number and dex count badge by running `make VERSION=[number] bump`
  - [ ] Inspect the updated CHANGELOG, README, and version files to ensure they are correct
- Once merged:
  - Pull the latest changes (checking out master if necessary) and build by running `./gradlew assembleRelease`
  - Release to GitHub:
    - [ ] Run `git tag vX.X.X && git push origin --tags`
    - [ ] Create a release from your new tag on [GitHub Releases](https://github.com/bugsnag/bugsnag-android/releases)
  - [ ] Release to Maven Central and Bintray by running `./gradlew assembleRelease publish bintrayUpload`
  - [ ] "Promote" the release build on Maven Central:
    - Go to the [sonatype open source dashboard](https://oss.sonatype.org/index.html#stagingRepositories)
    - Click the search box at the top right, and type “com.bugsnag”
    - Select the com.bugsnag staging repository
    - Click the “close” button in the toolbar, no message
    - Click the “refresh” button
    - Select the com.bugsnag closed repository
    - Click the “release” button in the toolbar
  - Open the Bintray repositories and publish the new artifacts:
    - [ ] [SDK repo](https://bintray.com/bugsnag/maven/bugsnag-android/_latestVersion)
    - [ ] [NDK repo](https://bintray.com/bugsnag/maven/bugsnag-android-ndk/_latestVersion)
- Merge outstanding docs PRs related to this release


### Post-release Checklist

_(May take some time to propagate to maven central and bintray)_

- [ ] Have all Docs PRs been merged?
- [ ] Can a freshly created example app send an error report from a release build using the released artefact?
- [ ] Do the existing example apps send an error report using the released artifact?
- [ ] Make releases to downstream libraries, if appropriate (generally for bug fixes)
