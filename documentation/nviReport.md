# Report: nvi

## Unique identifier

Combination of `publicationId`, `contributorIdentifier` and `affiliationId`

## Description

- `publicationId`
  - type: URI
  - description: The resource uri of the publication
  - example: <https://api.nva.unit.no/publication/{sortableIdentifier}>
- `contributorIdentifier`
  - type: integer
  - description: The Cristin identifier of the contributor
  - example: 123456
- `affiliationId`
  - type: URI
  - description: The resource uri of the affiliated organization
  - example: <https://api.nva.unit.no/cristin/organization/10.10.0.0>
- `institutionId`
  - type: URI
  - description: The resource uri of the top level organization the contributor
  is affiliated with
  - example: <https://api.nva.unit.no/cristin/organization/10.0.0.0>
- `institutionPoints`
  - type: double
  - description: The number of points the institution has been awarded for the publication
  - example: 1.0923
- `pointsForAffiliation`
  - type: double
  - description: The number of points the contributor has been awarded for the publication
  - example: 0.0923
- `institutionApprovalStatus`
  - type: string
  - enum: [New, Pending, Approved, Rejected]
  - description: The approval status of the institution
  - example: Pending
- `globalApprovalStatus`
  - type: string
  - enum: [Pending, Approved, Rejected, Dispute]
  - description: The approval status of the publication
  - example: Approved
- `reportedPeriod`
  - type: string
  - description: If the publication is not reported, the value is NotReported.
  If the publication is reported, the value is the year of the reporting period
  - example: 2023
- `totalPoints`
  - type: double
  - description: The total points awarded for the publication
  - example: 3.0923
- `publicationTypeChannelLevelPoints`
  - type: double
  - description: The number of points awarded for the publication type and
  channel level
  - example: 1.000
- `authorShareCount`
  - type: integer
  - description: The number combinations of authors and affiliations for the
  publication
  - example: 3
- `internationalCollaborationFactor`
  - type: double
  - description: A factor that is part of the calculation of the points awarded
  for the publication
  - example: 1.000
- `isApplicable`
  - type: boolean
  - description: A flag indicating if the publication is applicable for nvi
  candidacy or not
  - example: true
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
