# Helper: Set chaos level to MODERATE.

Feature: Helper — Set chaos level to MODERATE

  Background:
    * url baseUrl

  Scenario: Set level
    Given path 'api/intensity-level'
    And request { level: 'MODERATE' }
    When method post
    Then status 200
    And match response.status == 200
    And match response.data == 'MODERATE'
