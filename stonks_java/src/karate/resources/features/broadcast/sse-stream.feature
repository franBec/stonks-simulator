Feature: SSE broadcast stream

  Background:
    * url baseUrl

  @manual
  Scenario: SSE stream connects successfully
    # NOTE: Full SSE event parsing is tricky in Karate because SSE is a long-lived,
    # server-pushed stream. This scenario asserts that the endpoint is reachable and
    # returns the correct content type. For deep event verification, test manually
    # with:  curl -N --max-time 10 http://localhost:8080/api/stream
    Given path 'api/stream'
    And header Accept = 'text/event-stream'
    When method get
    Then status 200
    And match header Content-Type contains 'text/event-stream'
