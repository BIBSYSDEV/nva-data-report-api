
### How to run a bulk export

This process is intended as an "initial export"/database dump for the first export to the data
platform.
It can also be used if changes in the data model require a full re-import of the data.

The steps below can be outlined briefly as:

- Pre-run
  - Stop incoming live-update events
- Bulk export
  - Generate batches of document keys for export
  - Transform the key batches to csv files
- Post-run
  - Start incoming live-update events

### Pre-run steps

2. Turn off S3 event notifications for bucket `persisted-resources-{accountName}`
   In aws console, go
   to
   <br>_S3_ -> _persisted-resources-{accountName}_ -> _Properties_ -> _Amazon EventBridge_ ->
   _Edit_ -> _Off_

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

2. Verify that `GenerateKeyBatchesHandler` is done processing (i.e. check logs
   and that key batches have been generated S3 bucket
   `data-report-key-batches-{accountName}`
3. Process the key batches and generate csv files for both locations: `resources`
   and `nvi-candidates`.
   Manually trigger `CsvBulkTransformerHandler` with the following input:

   ```json
   {
      "detail": {
         "location": "resources|nvi-candidates"
      }
   }
   ```
   
4. Verify that `CsvBulkTransformerHandler` is done processing (i.e. check logs)
and that csv files have been generated S3 bucket
`data-report-csv-export-{accountName}`

### Post-run steps

1. Turn on S3 event notifications for bucket `persisted-resources-{accountName}`.
   In aws console, go
   to
   <br> _S3_ -> _persisted-resources-{accountName}_ -> _Properties_ -> _Amazon EventBridge_ ->
   _Edit_ -> _On_
