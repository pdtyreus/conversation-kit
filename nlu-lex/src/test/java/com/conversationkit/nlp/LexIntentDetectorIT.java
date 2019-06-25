/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conversationkit.nlp;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 *
 * @author pdtyreus
 */
public class LexIntentDetectorIT {

    private static final Logger logger = Logger.getLogger(LexIntentDetectorIT.class.getName());

    static BasicAWSCredentials credentials = null;
    static String botName = null;
    static String botAlias = null;
    static String region = null;

    public LexIntentDetectorIT() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = LexIntentDetectorIT.class.getResourceAsStream("/integration-test.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Unable to load integration properties. Skipping integration tests.", ex);
            return;
        }

        try (InputStreamReader input = new InputStreamReader(LexIntentDetectorIT.class.getResourceAsStream(prop.getProperty("credentialsFile")))) {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(input);
            logger.log(Level.INFO, "Using credentials file {0}.", prop.getProperty("credentialsFile"));
            credentials = new BasicAWSCredentials(json.get("aws_access_key_id").asText(), json.get("aws_secret_access_key").asText());
            botName = json.get("bot_name").asText();
            botAlias = json.get("bot_alias").asText();
            region = json.get("region").asText();
            logger.log(Level.INFO, "Loading bot {0} alias {1} in {2}.", Arrays.asList(botName,botAlias,region).toArray());
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Unable to use integration credentials. Skipping integration tests.", ex);
            return;
        }

    }

    @Test
    public void testDetectIntent() throws IOException, InterruptedException, ExecutionException {
        assumeTrue(credentials != null);
        String text = "What does conversation kit do for me?";
        String languageCode = "en-US";
        String sessionId = "integration-test";
        String INTENT_NAME = "CONVERSATION_KIT_IT_INTENT";
        LexIntentDetector instance = new LexIntentDetector(new AWSStaticCredentialsProvider(credentials), Regions.valueOf(region),botName,botAlias);
        Optional<LexIntent> result = instance.detectIntent(text, languageCode, sessionId);
        assertTrue(result.isPresent());
        assertEquals(INTENT_NAME, result.get().getIntentId());
    }
    
    @Test
    public void testDetectIntentMiss() throws IOException, InterruptedException, ExecutionException {
        assumeTrue(credentials != null);
        String text = "How old is Stonehenge?";
        String languageCode = "en-US";
        String sessionId = "integration-test";
        LexIntentDetector instance = new LexIntentDetector(new AWSStaticCredentialsProvider(credentials), Regions.valueOf(region),botName,botAlias);
        Optional<LexIntent> result = instance.detectIntent(text, languageCode, sessionId);
        assertTrue(!result.isPresent());
    }

}
