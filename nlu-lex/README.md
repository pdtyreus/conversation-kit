# Lex NLU for conversation-kit
**Bindings to use Amazon Lex as the NLU implementation for detecting conversation intents.**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Installation

The artifacts are available on Maven Central

```xml
<dependency>
  <groupId>com.conversationkit</groupId>
  <artifactId>nlu-lex</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Usage

To use [Lex](https://aws.amazon.com/lex/) you will to set up an account with AWS and download the CSV
file that contains the [access credentials](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html). 

```java
AWSCredentialsProvider credentialsProvider = new EnvironmentVariableCredentialsProvider();
LexIntentDetector instance = new LexIntentDetector(credentialsProvider, Regions.valueOf(region),YOUR_LEX_BOT_NAME,YOUR_LEX_BOT_ALIAS);
``` 

## Testing

To run the integration tests you will need to have an configured bot with Lex 
and API access credentials as a JSON file.

Update the `integration-test.properties` to match your project id and the name
of the file containing the credentials.

```properties
credentialsFile=/lex-it.json
```

The json file should contain the following values:
```json
{
    "aws_access_key_id": "YOUR_ACCESS_KEY",
    "aws_secret_access_key": "YOUR_SECRET_KEY",
    "region":"US_WEST_2",
    "bot_name":"YOUR_LEX_BOT_NAME",
    "bot_alias":"YOUR_LEX_BOT_ALIAS"
}
```

Then run

```sh
mvn integration-test
```