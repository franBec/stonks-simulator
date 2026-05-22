Feature: Paper tape retro formatting

  Background:
    * url baseUrl

  Scenario: Get paper tape entries
    # Ensures at least one trade exists so the paper tape has something to format.
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    Given path 'api/trades/paper-tape'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.content == '#[]'
    And match response.data.totalElements == '#number'
    And match response.data.totalPages == '#number'
    * assert response.data.totalElements > 0
    # When trades exist, each entry should have the retro format
    And match response.data.content[*].sequenceNumber == '#[] #number'
    And match response.data.content[*].formattedLine == '#[] #notnull'
    And match response.data.content[*].executedAt == '#[] #notnull'
