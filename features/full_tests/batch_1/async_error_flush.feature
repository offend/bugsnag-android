Feature: Flushing errors does not send duplicates

@Flaky
Scenario: Only 1 request sent if connectivity change occurs before launch
    When I run "AsyncErrorConnectivityScenario" and relaunch the app
    And I configure Bugsnag for "AsyncErrorConnectivityScenario"
    Then I wait to receive an error
    And the error is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the event "context" equals "AsyncErrorConnectivityScenario"

@Flaky
Scenario: Only 1 request sent if multiple connectivity changes occur
    When I run "AsyncErrorDoubleFlushScenario"
    Then I wait to receive an error
    And the error is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the event "context" equals "AsyncErrorDoubleFlushScenario"

@Flaky
Scenario: Only 1 request sent if connectivity change occurs after launch
    When I run "AsyncErrorLaunchScenario"
    Then I wait to receive an error
    And the error is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the event "context" equals "AsyncErrorLaunchScenario"
