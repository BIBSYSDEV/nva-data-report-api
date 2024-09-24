# Report: contributor

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
- `contributorIdentifier`
  - type: integer
  - description: The Cristin identifier of the contributor
  - example: 123456
- `contributorName`
  - type: string
  - description: The name of the contributor
  - example: John Doe
- `contributorSequenceNumber`
  - type: integer
  - description: The sequence number of the contributor in the publication
  - example: 1
- `contributorRole`
  - type: string
  - enum:
    See [roles](https://github.com/BIBSYSDEV/nva-publication-api/blob/main/publication-model/src/main/java/no/unit/nva/model/role/Role.java)
    in data model
  - description: The role of the contributor in the publication
  - example: Creator
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
