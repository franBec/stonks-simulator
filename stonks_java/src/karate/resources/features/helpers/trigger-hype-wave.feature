# Helper: Trigger a HYPE_WAVE chaos event on GMEE with HIGH severity.

Feature: Helper — Trigger HYPE_WAVE on GMEE

  Background:
    * url baseUrl

  Scenario: Trigger event
    Given path 'api/chaotic-events'
    And request
      """
      {
        "type": "HYPE_WAVE",
        "severity": "HIGH",
        "targetSymbol": "GMEE"
      }
      """
    When method post
    Then status 200
    And match response.status == 200
    And match response.data.type == 'HYPE_WAVE'
    And match response.data.severity == 'HIGH'
    And match response.data.targetSymbol == 'GMEE'
    And match response.data.eventId == '#uuid'
    # Expose event to caller
    * def chaosEvent = response.data
