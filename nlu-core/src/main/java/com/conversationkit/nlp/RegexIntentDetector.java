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

import com.conversationkit.model.IConversationIntent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A primitive intent detector that just tries to match the input string to RegEx patterns.
 * @author pdtyreus
 */
public class RegexIntentDetector implements IntentDetector<IConversationIntent> {

    public static final String YES = "\\bk\\b|\\bok\\b|\\byes\\b|\\byep\\b|\\byeah\\b|\\bsome\\b|\\a little\\b|\\ba bit\\b";
    public static final String NO = "\\bno\\b|\\bnope\\b|\\bnah\\b|\\bnone\\b|\\bnot really\\b";

    private final Map<String, Pattern> intentRegexMap;
    private static final Logger logger = Logger.getLogger(RegexIntentDetector.class.getName());

    public RegexIntentDetector(Map<String, String> intentRegexMap) {
        this.intentRegexMap = new HashMap();
        for (Map.Entry<String, String> entry : intentRegexMap.entrySet()) {
            this.intentRegexMap.put(entry.getKey(), Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE));
        }
    }

    @Override
    public CompletableFuture<Optional<IConversationIntent>> detectIntent(String text, String languageCode, String sessionId) {

        for (Map.Entry<String, Pattern> entry : intentRegexMap.entrySet()) {
            Matcher matcher = entry.getValue().matcher(text);
            if (matcher.find()) {
                logger.info(String.format("Matched intent %s with regex %s", entry.getKey(), entry.getValue()));

                Map<String, Object> slots = new HashMap();
                int i = 0;
                matcher.reset();
                while (matcher.find()) {
                    slots.put(i + "", matcher.group());
                }

                IConversationIntent intent = new IConversationIntent() {

                    @Override
                    public String getIntentId() {
                        return entry.getKey();
                    }

                    @Override
                    public Map<String, Object> getSlots() {
                        return slots;
                    }

                };

                return CompletableFuture.completedFuture(Optional.of(intent));
            }

        }
        logger.info(String.format("No matching intent for %s", text));
        return CompletableFuture.completedFuture(Optional.empty());
    }

}
