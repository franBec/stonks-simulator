Feature: Trigger and list chaos events

  Background:
    * url baseUrl

  Scenario: Trigger a manual chaos event
    # Reuses the helper to keep event-trigger logic DRY.
    * def eventResult = call read('classpath:features/helpers/trigger-hype-wave.feature')

  Scenario: List active chaos events
    Given path 'api/chaos/events'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data == '#[]'
    # Each event should have required fields
    And match response.data[*].eventId == '#[] #uuid'
    And match response.data[*].type == '#[] #notnull'
    And match response.data[*].severity == '#[] #notnull'
