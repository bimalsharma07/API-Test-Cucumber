Feature: Genderize API Testing

  Scenario: Predict gender of a single name
    Given I have the name "peter"
    When I send a request to Genderize API
    Then the response should contain:
      | field       | value      |
      | name        | peter      |
      | gender      | male       |
      | probability | 1          |
      | count       | 1094417    |

  Scenario Outline: Predict gender with country context
    Given I have the name "kim" and country "<country>"
    When I send a request to Genderize API
    Then the response should contain:
      | field       | value      |
      | name        | kim        |
      | country_id  | <country>  |
      | gender      | <gender>   |
      | probability | <prob>     |
      | count       | <count>    |

    Examples:
      | country | gender   | prob | count   |
      | US      | female   | 0.95 | 28672   |
      | DK      | male     | 0.96 | 3626    |

  Scenario: Predict gender for multiple names
    Given I have multiple names "peter,lois,stewie"
    When I send a request to Genderize API
    Then the response should contain 3 predictions
    And prediction 1 should contain:
      | field       | value      |
      | name        | peter      |
      | gender      | male       |
      | probability | 1          |
      | count       | 1094417    |
    And prediction 2 should contain:
      | field       | value      |
      | name        | lois       |
      | gender      | female     |
      | probability | 0.98       |
      | count       | 50141      |
    And prediction 3 should contain:
      | field       | value      |
      | name        | stewie     |
      | gender      | male       |
      | probability | 0.94       |
      | count       | 361        |

  Scenario: Edge case - no name provided
    Given I have the name ""
    When I send a request to Genderize API
    Then the response should contain:
      | field       | value |
      | count       | 0     |
      | name        | ""    |
      | gender      | null  |
      | probability | 0.0   |

  Scenario: Edge case - wrong name provided
    Given I have the name "peter2"
    When I send a request to Genderize API
    Then the response should contain:
      | field       | value |
      | count       | 0     |
      | name        | peter2|
      | gender      | null  |
      | probability | 0.0   |

  Scenario: Edge case - wrong country ID
    Given I have the name "kim" and country "US2"
    When I send a request to Genderize API
    Then the response should contain:
      | field       | value |
      | count       | 0     |
      | name        | kim   |
      | country_id  | US2   |
      | gender      | null  |
      | probability | 0.0   |
