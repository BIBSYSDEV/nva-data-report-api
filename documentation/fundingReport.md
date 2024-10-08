# Report: funding

## Unique identifier

Combination of `publicationId` and `fundingId`

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
- `fundingSource`
  - type: string
  - description: The source of the funding
  - example: NFR
- `fundingId`
  - type: URI
  - description: The resource uri of the funding
  - example: <https://api.nva.unit.no/verified-funding/nfr/123456>
- `fundingName`
  - type: string
  - description: The name of the funding
  - example: Norwegian Research Council
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
