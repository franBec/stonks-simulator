# Helper: Execute a SELL trade for 5 shares of GMEE.
# Callers should first ensure a BUY has been executed (via buy-gmee.feature).

Feature: Helper — Execute SELL 5 GMEE

  Background:
    * url baseUrl

  Scenario: Execute SELL
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
    And match response.data.message == '#regex ^TRADE EXECUTED - SELL.*'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 0
    And match response.data.totalCost == '#number'
    And match response.data.errorCode == null
    * assert response.data.newCashBalance > 0
    # Expose trade result to caller
    * def tradeResult = response.data
