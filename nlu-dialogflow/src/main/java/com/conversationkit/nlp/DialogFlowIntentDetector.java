/*
 * The MIT License
 *
 * Copyright 2019 Synclab Consulting LLC.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.conversationkit.nlp;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.QueryParameters;
import com.google.cloud.dialogflow.v2beta1.QueryResult;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DialogFlowIntentDetector implements IntentDetector<DialogFlowIntent> {

    private final String projectId;
    private final SessionsSettings sessionsSettings;
    private static final Logger logger = Logger.getLogger(DialogFlowIntentDetector.class.getName());

    public DialogFlowIntentDetector(GoogleCredentials credentials, String projectId) throws IOException {
        this.projectId = projectId;
        this.sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public Optional<DialogFlowIntent> detectIntent(String text, String languageCode, String sessionId) {
        // Instantiates a client

//        return CompletableFuture.supplyAsync(() -> {
//            try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
//                return sessionsClient;
//            } catch (IOException ex) {
//                logger.log(Level.WARNING, "Unable to communicate with DialogFlow", ex);
//                throw new RuntimeException(ex);
//            }
//        }).thenApplyAsync((sessionsClient) -> {
//            SessionName session = SessionName.of(projectId, sessionId);
//            logger.log(Level.FINE, "DialogFlow Session Path {0} ", session.toString());
//
//            // Set the text (hello) and language code (en-US) for the query
//            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
//
//            // Build the query with the TextInput
//            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
//
//            //QueryParameters.Builder queryParamBuilder = QueryParameters.newBuilder();
//
//            DetectIntentRequest request = DetectIntentRequest.newBuilder()
//                    .setQueryInput(queryInput)
//                    .setSession(session.toString())
//                    //.setQueryParams(queryParamBuilder.build())
//                    .build();
//
//            // Performs the detect intent request
//            DetectIntentResponse response = sessionsClient.detectIntent(request);
//
//            // Display the query result
//            QueryResult queryResult = response.getQueryResult();
//            return Optional.of(new DialogFlowIntent(queryResult));
//        }).exceptionally((e)->{
//            logger.log(Level.WARNING,"Error detecting intent: {0}",e.getMessage());
//            return Optional.empty();
//        });
        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
            SessionName session = SessionName.of(projectId, sessionId);
            logger.log(Level.FINE, "DialogFlow Session Path {0} ", session.toString());

            // Set the text (hello) and language code (en-US) for the query
            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

            // Build the query with the TextInput
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            //QueryParameters.Builder queryParamBuilder = QueryParameters.newBuilder();
            DetectIntentRequest request = DetectIntentRequest.newBuilder()
                    .setQueryInput(queryInput)
                    .setSession(session.toString())
                    //.setQueryParams(queryParamBuilder.build())
                    .build();

            // Performs the detect intent request
            DetectIntentResponse response = sessionsClient.detectIntent(request);

            // Display the query result
            QueryResult queryResult = response.getQueryResult();
            return Optional.of(new DialogFlowIntent(queryResult));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Unable to communicate with DialogFlow", ex);
            return Optional.empty();

        }
    }

}
