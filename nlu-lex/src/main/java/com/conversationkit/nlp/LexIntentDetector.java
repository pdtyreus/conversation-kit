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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClient;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.amazonaws.services.lexruntime.model.PostTextResult;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class LexIntentDetector implements IntentDetector<LexIntent> {

    private final AmazonLexRuntime client;
    private final String botName;
    private final String botAlias;
    private static final Logger logger = Logger.getLogger(LexIntentDetector.class.getName());

    public LexIntentDetector(AWSCredentialsProvider credentialProvider, Regions region, String botName, String botAlias) throws IOException {
        client = AmazonLexRuntimeClient.builder()
                .withCredentials(credentialProvider)
                .withRegion(region)
                .build();
        this.botName = botName;
        this.botAlias = botAlias;
    }

    @Override
    public Optional<LexIntent> detectIntent(String text, String languageCode, String sessionId) {
        PostTextRequest request = new PostTextRequest();
        request.setBotAlias(botAlias);
        request.setBotName(botName);
        request.setInputText(text);
        request.setUserId(sessionId);

        PostTextResult result = client.postText(request);
        if (result.getIntentName() == null) {
            logger.log(Level.FINE, "[Lex] did not detect an intent.");
            return Optional.empty();
        } else {
            logger.log(Level.FINE, "[Lex] detected intent {0} ", result.getIntentName());
            return Optional.of(new LexIntent(result));
        }
    }

}
