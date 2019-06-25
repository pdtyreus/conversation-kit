/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conversationkit.nlp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
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
public class DialogFlowIntentDetectorIT {

    private static final Logger logger = Logger.getLogger(DialogFlowIntentDetectorIT.class.getName());

    static GoogleCredentials credentials = null;
    static String projectId = null;

    public DialogFlowIntentDetectorIT() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = DialogFlowIntentDetectorIT.class.getResourceAsStream("/integration-test.properties")) {
            prop.load(input);
            projectId = prop.getProperty("projectId");
            logger.log(Level.INFO, "Initialized project {0} for testing.", projectId);

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Unable to load integration properties. Skipping integration tests.", ex);
            return;
        }

        try (InputStream input = DialogFlowIntentDetectorIT.class.getResourceAsStream(prop.getProperty("credentialsFile"))) {
            logger.log(Level.INFO, "Using credentials file {0}.", prop.getProperty("credentialsFile"));
            credentials = GoogleCredentials.fromStream(input)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
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
        DialogFlowIntentDetector instance = new DialogFlowIntentDetector(credentials, projectId);
        Optional<DialogFlowIntent> result = instance.detectIntent(text, languageCode, sessionId);
        assertTrue(result.isPresent());
        assertEquals(INTENT_NAME, result.get().getIntentId());
    }

    @Test
    public void testDetectIntentMiss() throws IOException, InterruptedException, ExecutionException {
        assumeTrue(credentials != null);
        String text = "How old is Stonehenge?";
        String languageCode = "en-US";
        String sessionId = "integration-test";
        DialogFlowIntentDetector instance = new DialogFlowIntentDetector(credentials, projectId);
        Optional<DialogFlowIntent> result = instance.detectIntent(text, languageCode, sessionId);
        assertTrue(!result.isPresent());
    }
}
