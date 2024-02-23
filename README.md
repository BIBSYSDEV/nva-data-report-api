# NVA Data Report API

This repository contains the NVA data report API.

## How to run a bulk upload

### Pre-run steps

1. Remove all objects from S3 bucket `loader-input-files-{accountName}`
2. Turn off S3 event notifications for bucket `persisted-resources-{accountName}`
   In aws console, go
   to
   <br>_S3_ -> _persisted-resources-{accountName}_ -> _Properties_ -> _Amazon EventBridge_ ->
   _Edit_ -> _Off_
3. Press `ResetDatabaseButton` (Trigger `DatabaseResetHandler`). This might take around a minute to
   complete.
4. Verify that database is empty. Example sparql queries:
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
     "loadId": "{copy the loadId from the logs, its an UUID}"
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