# Report: identifier

## Unique identifier

Combination of `publicationId` and `additionalIdentifier`

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
- `additionalIdentifierSourceName`
  - type: string
  - description: The source of the additional identifier
  - example: Cristin
- `additionalIdentifier`
  - type: string
  - description: The additional identifier value
  - example: 2-s2.0-80051784603
- `additionalIdentifierType`
  - type: string
  - enum:
    See [AdditionalIdentifierBase-subtypes](https://github.com/BIBSYSDEV/nva-publication-api/blob/main/publication-model/src/main/java/no/unit/nva/model/additionalidentifiers/AdditionalIdentifierBase.java)
  - description: The type of the additional identifier
  - example: ScopusIdentifier
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
