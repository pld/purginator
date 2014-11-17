# purginator

`purginator` was designed to enabled dynamically throttled operations against a
local or remote DynamoDB database. The initial use case was to purge several
hundred thousand records from a DynamoDB database that was in production use,
without exceeding provisioned write capacity.

## Usage

```bash
$ lein run -m purginator.core -h
Usage: purginator [options] action file

Options:
  -t, --table TABLE                                   DynamoDB table name
  -k, --primary-key KEY                               Primary key name
      --batch-size SIZE        25                     Writes per batch
      --write-rate RATE        2000                   Writes per second
      --endpoint URL           http://localhost:8000  Local DynamoDB endpoint
      --remote                                        Execute using AWS environment variables against a remote DynamoDB instance
      --access-key AWS-ID      AWS_ACCESS_KEY_ID      AWS Access Key ID
      --secret-key AWS-SECRET  AWS_SECRET_ACCESS_KEY  AWS Secret Key
  -h, --help

Actions:
  put      Populate specified DynamoDB table with primary keys from file
  delete   Remove items with primary keys listed in file from specified DynamoDB table
```

`purginator` takes the arguments and actions as outlined above. The input file should consist of primary keys, one per line, like this:

```text
foo
bar
baz
...
```

To populate a local test table (which must already exist):

```bash
$ lein run \
    -t lookup_table \
    -k primary_key \
    put test-resources/keys.txt
```

To delete items from a local test table:

```bash
$ lein run \
    -t lookup_table \
    -k primary_key \
    delete test-resources/keys.txt
```

## Environment

AWS credentials can be provided using the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`.

## Building

```bash
$ lein uberjar
```


## License

Copyright © 2014 Intent Media
