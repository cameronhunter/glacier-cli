# Glacier CLI [![Build Status](https://secure.travis-ci.org/cameronhunter/glacier-cli.png)](http://travis-ci.org/cameronhunter/glacier-cli)

A command line client to [Amazon Glacier](http://aws.amazon.com/glacier). More info at the [AWS Glacier development docs](http://docs.amazonwebservices.com/amazonglacier/latest/dev/).

## Configuration

Provide your AWS credentials by setting `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables or by creating `$HOME/AwsCredentials.properties` with your AWS keys

```
secretKey=…
accessKey=…
```

## Examples

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
-region	<region>	Specify URL as the web service URL to use. Defaults to 'us-east-1'
-credentials	<file>	Specify a properties file containing your AWS credentials. Defaults to '$HOME/AwsCredentials.properties'
```

## Building

```bash
mvn clean package
```

License
-------
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
