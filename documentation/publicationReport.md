# Report: publication

## Unique identifier

Combination of `publicationId` and `channelIdentifier`.

NB! We have noticed deviations where `channelIdentifier` is not unique.
We are working to fix this.
In the meantime, the unique identifier is the combination
of `publicationId` and `channelType`.

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
- `publicationTitle`
  - type: string
  - description: The title of the publication
  - example: A publication title
- `publicationCategory`
  - type: string
  - enum: See [instanceTypes](https://github.com/BIBSYSDEV/nva-publication-api/tree/main/publication-model/src/main/java/no/unit/nva/model/instancetypes)
  - description: The instance type of the publication
  - example: AcademicArticle
- `publicationDate`
  - type: string
  - description: The publication date of the publication. Might only include
    the year or the full date with format yyyy-mm-dd
  - example: 2015-5-24
- `channelType`
  - type: string
  - enum: [Journal, Series, Publisher]
  - description: The type of the publication channel
  - example: Journal
- `channelIdentifier`
  - type: string
  - description: The identifier (pid) of the publication channel
  - example: F96836B7-3D46-45E5-8B26-E0635CB45C19
- `channelName`
  - type: string
  - description: The name of the publication channel
  - example: Journal of Something
- `channelOnlineIssn`
  - type: string
  - description: The online issn of the publication channel
  - example: 1234-5678
- `channelPrintIssn`
  - type: string
  - description: The print issn of the publication channel
  - example: 1234-5678
- `channelLevel`
  - type: string
  - enum: [Unassigned, LevelZero, LevelOne, LevelTwo]
  - description: The scientific level of the publication channel
  - example: LevelOne
- `publicationIdentifier`
  - type: string
  - description: The sortable identifier of the publication
  - example: 018a8931a9d8-910a8ae0-77fd-4dba-ab12-efe82279b44e
- `modifiedDate`
  - type: string
  - description: The last modified date of the publication. ISO 8601 format
  - example: 2023-09-12T11:41:57.112226Z
