# Edge cases and negative scenarios that test validation rules and API robustness.
# Each scenario is independent and operates on the initial empty state.

Feature: Edge cases and negative scenarios

  Background:
    * url baseUrl

  # ------------------------------------------------------------
  # Trade rejection scenarios
  # ------------------------------------------------------------

  Scenario: SELL without owning returns INSUFF_SHARES
    Given path 'api/trades'
    And request { action: 'SELL', symbol: 'GMEE', quantity: 5 }
    When method post
    Then status 200
    And match response.data.status == 'REJECTED'
    And match response.data.errorCode == 'S223'
    And match response.data.message == 'JOB ABEND S223 - INSUFF SHARES'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 0

  Scenario: BUY unknown symbol returns UNKNOWN_SYMBOL
    Given path 'api/trades'
    And request { action: 'BUY', symbol: 'XXXX', quantity: 1 }
    When method post
    Then status 200
    And match response.data.status == 'REJECTED'
    And match response.data.errorCode == 'S001'
    And match response.data.message == 'JOB ABEND S001 - UNKNOWN SYMBOL XXXX'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 0

  Scenario: BUY with zero quantity returns INVALID_QTY
    Given path 'api/trades'
    And request { action: 'BUY', symbol: 'GMEE', quantity: 0 }
    When method post
    Then status 200
    And match response.data.status == 'REJECTED'
    And match response.data.errorCode == 'S224'
    And match response.data.message == 'JOB ABEND S224 - INVALID QTY'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 0

  Scenario: BUY with insufficient funds returns INSUFF_FUNDS
    Given path 'api/trades'
    And request { action: 'BUY', symbol: 'GMEE', quantity: 999999 }
    When method post
    Then status 200
    And match response.data.status == 'REJECTED'
    And match response.data.errorCode == 'S222'
    And match response.data.message == 'JOB ABEND S222 - INSUFF FUNDS'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 0

  # ------------------------------------------------------------
  # API robustness scenarios
  # ------------------------------------------------------------

  Scenario: Invalid endpoint returns 404
    Given path 'api/nonexistent'
    When method get
    Then status 404
    And match response contains { instance: '#string', status: 404, title: '#string', timestamp: '#string', trace: '#string' }

  Scenario: Invalid intensity level returns error
    Given path 'api/intensity-level'
    And request { level: 'BANANAS' }
    When method post
    Then status 500
    And match response contains { instance: '#string', status: 500, title: '#string', detail: '#string', timestamp: '#string', trace: '#string' }

  Scenario: Invalid chaos event type returns error
    Given path 'api/chaotic-events'
    And request { type: 'BANANAS', severity: 'HIGH' }
    When method post
    Then status 500
    And match response contains { instance: '#string', status: 500, title: '#string', detail: '#string', timestamp: '#string', trace: '#string' }
