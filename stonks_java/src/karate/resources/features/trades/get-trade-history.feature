Feature: Trade history

  Background:
    * url baseUrl

  Scenario: Get empty trade history
    Given path 'api/trades/history'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.content == '#[]'
    And match response.data.totalElements == 0
    And match response.data.totalPages == 0

  Scenario: Get trade history after a trade
    # Ensures at least one trade exists by calling the helper before asserting history.
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    Given path 'api/trades/history'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.content == '#[]'
    And match response.data.totalElements == '#number'
    * assert response.data.totalElements > 0
