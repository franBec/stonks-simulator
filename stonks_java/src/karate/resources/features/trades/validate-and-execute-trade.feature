Feature: Trade validation and execution

  Background:
    * url baseUrl

  Scenario: Validate a BUY trade
    Given path 'api/trades/validate'
    And request
      """
      {
        "action": "BUY",
        "symbol": "GMEE",
        "quantity": 5,
        "price": 150.00,
        "cashBalance": 10000.00
      }
      """
    When method post
    Then status 200
    And match response.status == 200
    And match response.data.status == 'ACCEPTED'
    And match response.data.message == 'TRADE VALIDATED - PROCEED TO EXECUTION'
    And match response.data.totalCost == 750.0
    And match response.data.remainingCash == 9250.0
    And match response.data.errorCode == null

  Scenario: Execute a BUY trade
    # Reuses the helper to keep execution logic DRY.
    * def tradeResult = call read('classpath:features/helpers/buy-gmee.feature')
