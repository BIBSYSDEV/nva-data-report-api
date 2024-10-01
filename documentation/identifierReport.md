# Report: identifier

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
- `additionalIdentifierSource`
  - type: string
  - description: The source of the additional identifier
  - example: Cristin
- `additionalIdentifier`
  - type: string
  - description: The additional identifier value
  - example: 2-s2.0-80051784603
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
