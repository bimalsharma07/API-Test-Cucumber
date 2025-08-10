Feature: Genderize API Testing

  Scenario: Predict gender of a single name
    Given I have the name "Emily"
    When I send a request to Genderize API
    Then the response should contain gender "female"

  Scenario Outline: Predict gender with country context
    Given I have the name "<name>" and country "<country_id>"
    When I send a request to Genderize API
    Then the response should contain gender "<expected_gender>"

    Examples:
      | name   | country_id | expected_gender |
      | Andrea | IT         | male            |
      | Andrea | US         | female          |

  Scenario: Predict gender for multiple names
    Given I have multiple names "John, Sarah, Alex"
    When I send a request to Genderize API
    Then the response should contain genders for all names

  Scenario: Edge case - empty name
    Given I have an empty name
    When I send a request to Genderize API
    Then the response should return an error or empty result
