# Glacier CLI [![Build Status](https://secure.travis-ci.org/cameronhunter/glacier-cli.png)](http://travis-ci.org/cameronhunter/glacier-cli)

A command line client to [Amazon Glacier](http://aws.amazon.com/glacier), an extremely low-cost storage service that provides secure and durable storage for data archiving and backup.

## Getting Started

Download a [release](https://github.com/brocklaporte/glacier-cli/downloads), extract the files and add the `bin` directory to your `PATH`. If you want to build the project yourself you can run `mvn clean package`. Requires Java 6 or later.

## Configuration

Provide your AWS credentials by setting `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables or by passing a properties file with the `-credentials` option.

```
secretKey=…
accessKey=…
```

## Usage

Upload `file1.zip` and `file2.zip` to vault `pictures`

```bash
glacier-upload pictures file1.zip file2.zip
```

Download archive with id `xxx` from vault `pictures` to file `pic.tar` (takes >4 hours)

```bash
glacier-download pictures xxx pic.tar
```

Delete archive with id `xxx` from vault `pictures`

```bash
glacier-delete pictures xxx
```

Get the inventory for vault `pictures` (takes >4 hours)

```bash
glacier-inventory pictures
```

Upload `file1.zip` and `file2.zip` to vault `pictures` in European region

```bash
glacier-upload -region eu-west-1 pictures file1 file2
```

List vaults in European region

```bash
glacier-vaults -region eu-west-1
```

## Command line options

```
-region      <region>  Defaults to 'us-east-1'
-credentials <file>    Defaults to '$HOME/AwsCredentials.properties'
```

## Thanks to

[Cameron Hunter](https://github.com/cameronhunter) for his fork and adaptation of [Carlos Sanchez's](https://github.com/carlossg) [glacier-cli](https://github.com/carlossg) project.
