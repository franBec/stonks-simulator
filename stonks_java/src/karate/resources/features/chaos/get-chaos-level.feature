Feature: Chaos level

  Background:
    * url baseUrl

  Scenario: Get current chaos level
    Given path 'api/chaos/level'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data == 'PAPER_HANDS'

  Scenario: Set chaos level to MODERATE
    # Reuses the helper to keep state-change logic DRY.
    * call read('classpath:features/helpers/set-chaos-moderate.feature')
