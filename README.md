# NVA Data Report API

This repository contains the NVA data report API.

See [reportTypes](reportTypes.md) for a list of reports and data types.

## How to run a bulk upload

The steps below can be outlined briefly as:

- Pre-run
    - Stop incoming live-update events
    - Delete data from previous runs
    - Delete all data in database
- Bulk upload
    - Generate batches of document keys for upload
    - Transform the data to a format compatible with the bulk-upload action
    - Initiate bulk upload
    - Verify data integrity
- Post-run
    - Start incoming live-update events

### Pre-run steps

1. Remove all objects from S3 bucket `loader-input-files-{accountName}`
2. Turn off S3 event notifications for bucket `persisted-resources-{accountName}`
   In aws console, go
   to
   <br>_S3_ -> _persisted-resources-{accountName}_ -> _Properties_ -> _Amazon EventBridge_ ->
   _Edit_ -> _Off_
3. Press `ResetDatabaseButton` (Trigger `DatabaseResetHandler`). This might take around a minute to
   complete.
4. Verify that database is empty. You can use SageMaker notebook to query the database*. Example
   sparql queries:
   ```
   SELECT (COUNT(DISTINCT ?g) as ?gCount) WHERE {GRAPH ?g {?s ?p ?o}}
   ```
   or
   ```
   SELECT ?g ?s ?p ?o WHERE {GRAPH ?g {?s ?p ?o}} LIMIT 100
   ```

### Bulk upload steps

1. Generate key batches for both locations: `resources` and `nvi-candidates`. Manually trigger
   `GenerateKeyBatchesHandler` with the following input:
   ```json
   {
      "detail": {
         "location": "resources|nvi-candidates"
      }
   }
   ```
2. Verify that `GenerateKeyBatchesHandler` is done processing (i.e. check logs) and that key batches
   have been generated S3 bucket `data-report-key-batches-{accountName}`
3. Trigger `BulkTransformerHandler`
4. Verify that `BulkTransformerHandler` is done processing (i.e. check logs) and that nquads
   have been generated S3 bucket `loader-input-files-{accountName}`
5. Trigger `BulkDataLoader`
6. To check progress for bulk upload to Neptune. Trigger `BulkDataLoader` with the following input:
    ```json
    {
     "loadId": "{copy loadId UUID from test log}"
    }
    ```
7. Verify that expected count is in database. Query for counting distinct named graphs:
   ```
   SELECT (COUNT(DISTINCT ?g) as ?gCount) WHERE {GRAPH ?g {?s ?p ?o}}
   ```

### Post-run steps

1. Turn on S3 event notifications for bucket `persisted-resources-{accountName}`.
   In aws console, go
   to
   <br> _S3_ -> _persisted-resources-{accountName}_ -> _Properties_ -> _Amazon EventBridge_ ->
   _Edit_ -> _On_

*Note: You can use SageMaker notebook to query the database. Notebook can be opened from the AWS
console through _SageMaker_ -> _Notebooks_ -> _Notebook instances_ -> _Open JupyterLab_
