# Report: affiliation

## Unique identifier

Combination of `publicationId`, `contributorId` and `affiliationId`

## Description

- `publicationId`
  - type: URI
  - description: The resource uri of the publication
  - example: <https://api.nva.unit.no/publication/{sortableIdentifier}>
- `status`
  - type: string
  - enum: [PUBLISHED, UNPUBLISHED, DELETED]
  - description: The status of the publication
  - example: PUBLISHED
- `publicationIdentifier`
  - type: string
  - description: The sortable identifier of the publication
  - example: 018a8931a9d8-910a8ae0-77fd-4dba-ab12-efe82279b44e
- `contributorId`
  - type: integer
  - description: The Cristin identifier of the contributor
  - example: 123456
- `contributorName`
  - type: string
  - description: The name of the contributor
  - example: John Doe
- `affiliationId`
  - type: URI
  - description: The resource uri of the affiliated organization
  - example: <https://api.nva.unit.no/cristin/organization/10.0.0.0>
- `affiliationName`
  - type: string
  - description: The name of the affiliated organization
  - example: University of Some City
- `institutionId`
  - type: URI
  - description: The resource uri of the institution
  - example: <https://api.nva.unit.no/cristin/organization/10.0.0.0>
- `facultyId`
  - type: URI
  - description: The resource uri of the faculty
  - example: <https://api.nva.unit.no/cristin/organization/10.1.0.0>
- `departmentId`
  - type: URI
  - description: The resource uri of the department
  - example: <https://api.nva.unit.no/cristin/organization/10.1.1.0>
- `groupId`
  - type: URI
  - description: The resource uri of the group
  - example: <https://api.nva.unit.no/cristin/organization/10.1.1.1>
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
