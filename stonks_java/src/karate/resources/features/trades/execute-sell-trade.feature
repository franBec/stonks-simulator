Feature: Sell trade

  Background:
    * url baseUrl

  Scenario: Execute a SELL trade
    # Guarantees a BUY position exists by calling the helper first, then sells it.
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    Given path 'api/trades'
    And request
      """
      {
        "action": "SELL",
        "symbol": "GMEE",
        "quantity": 5
      }
      """
    When method post
    Then status 200
    And match response.status == 200
    And match response.data.status == 'ACCEPTED'
    And match response.data.message == 'TRADE EXECUTED - SELL GMEE'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 0
    And match response.data.totalCost == '#number'
    And match response.data.errorCode == null
    * assert response.data.newCashBalance > 0
