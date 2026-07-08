Feature: Helper — Reset portfolio

  Background:
    * url baseUrl

  Scenario: Reset portfolio
    Given path 'api/portfolio/reset'
    When method post
    Then status 200
