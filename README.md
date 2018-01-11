# Glacier CLI [![Build Status](https://travis-ci.org/cameronhunter/glacier-cli.svg)](https://travis-ci.org/cameronhunter/glacier-cli)

A command line client to [Amazon Glacier](http://aws.amazon.com/glacier), an extremely low-cost storage service that provides secure and durable storage for data archiving and backup.

## Usage

```bash
$ glacier --help

Usage: glacier [command]

Commands:

  upload <vault> <file...>
  download <vault> <archive_id> <output_file>
  delete <vault> <archive_id>
  inventory <vault>
  vaults

Options:

  -h, --help                                Show this message
  -a, --accessKey <AWS_ACCESS_KEY>          AWS Access Key ID
  -s, --secretKey <AWS_SECRET_ACCESS_KEY>   AWS Secret Access Key ID
  -r, --region <AWS_REGION>                 AWS Region
```

## Commands

Upload `file1.zip` and `file2.zip` to vault `pictures`

```bash
$ glacier-upload pictures file1.zip file2.zip
```

Download archive with id `xxx` from vault `pictures` to file `pic.tar` (takes >4 hours)

```bash
$ glacier-download pictures xxx pic.tar
```

Delete archive with id `xxx` from vault `pictures`

```bash
$ glacier-delete pictures xxx
```

Get the inventory for vault `pictures` (takes >4 hours)

```bash
$ glacier-inventory pictures
```

Upload `file1.zip` and `file2.zip` to vault `pictures`

```bash
$ glacier-upload pictures file1 file2
```

List vaults

```bash
$ glacier-vaults
```

## Advanced Configuration

Provide your AWS credentials by setting `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_REGION` environment variables or using properties files:

`~/.aws/credentials`:
```
[default]
aws_access_key_id={YOUR_ACCESS_KEY_ID}
aws_secret_access_key={YOUR_SECRET_ACCESS_KEY}
```

`~/.aws/config`:
```
[default]
region={YOUR_AWS_REGION}
```
