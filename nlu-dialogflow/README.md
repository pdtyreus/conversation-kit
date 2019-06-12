# DialogFlow NLU for conversation-kit
**Bindings to use DialogFlow as the NLU implementation for detecting conversation intents.**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Installation

The artifacts are available on Maven Central

```xml
<dependency>
  <groupId>com.conversationkit</groupId>
  <artifactId>nlu-dialogflow</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Usage

To use [DialogFlow](https://dialogflow.com) you will to set up an account and download the JSON
file that contains the [private access key](https://dialogflow.com/docs/reference/v2-auth-setup). You
can then include the JSON file with your project and load the credentials.

```java
try (InputStream input = DialogFlowIntentDetectorIT.class.getResourceAsStream("/credentials.json")) {
    String projectId = "YOUR_DIALOG_FLOW_PROJECT_ID"
    GoogleCredentials credentials = GoogleCredentials.fromStream(input)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    DialogFlowIntentDetector instance = new DialogFlowIntentDetector(credentials, projectId);
} catch (IOException ex) {
    ...
}
``` 

## Testing

To run the integration tests you will need to have an configured agent with DialogFlow 
and API access credentials as a JSON file.

Update the `integration-test.properties` to match your project id and the name
of the file containing the credentials.

```properties
projectId=conversation-kit-test-agent-gs
credentialsFile=/dialogflow-it.json
```

Then run

```sh
mvn integration-test
```