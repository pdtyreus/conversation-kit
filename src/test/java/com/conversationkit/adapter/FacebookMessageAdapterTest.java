/*
 * The MIT License
 *
 * Copyright 2016 Synclab Consulting LLC.
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
package com.conversationkit.adapter;

import com.conversationkit.adapter.FacebookMessageAdapter;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.conversationkit.model.IConversationSnippet;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import java.util.Arrays;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author pdtyreus
 */
public class FacebookMessageAdapterTest extends TestCase {
    
    private FacebookMessageAdapter instance = new FacebookMessageAdapter();
    private static final Logger logger = Logger.getLogger(FacebookMessageAdapterTest.class.getName());
    
    public FacebookMessageAdapterTest(String testName) {
        super(testName);
    }

    public void testSnippetToJson() {
        final String text =  "Facebook Messenger";
        
        IConversationSnippet snippet = new IConversationSnippet(){

            @Override
            public String renderContent(IConversationState state) {
                return text;
            }

            @Override
            public SnippetType getType() {
                return SnippetType.STATEMENT;
            }

            @Override
            public SnippetContentType getContentType() {
                return SnippetContentType.TEXT;
            }

            @Override
            public Iterable getSuggestedResponses(IConversationState state) {
                return null;
            }
            
        };
        
        String phoneNumber = "+1415000000";
        String result = instance.snippetToJson(snippet, null, phoneNumber);
        logger.info(result);
        JsonValue value = Json.parse(result);
        assertEquals(phoneNumber, value.asObject().get("recipient").asObject().get("id").asString());
        assertEquals(text, value.asObject().get("message").asObject().get("text").asString());
        
        snippet = new IConversationSnippet(){

            @Override
            public String renderContent(IConversationState state) {
                return text;
            }

            @Override
            public SnippetType getType() {
                return SnippetType.QUESTION;
            }

            @Override
            public SnippetContentType getContentType() {
                return SnippetContentType.TEXT;
            }

            @Override
            public Iterable getSuggestedResponses(IConversationState state) {
                return Arrays.asList("yes","no");
            }
            
        };
        
        result = instance.snippetToJson(snippet, null, phoneNumber);
        logger.info(result);
        value = Json.parse(result);
        assertEquals(phoneNumber, value.asObject().get("recipient").asObject().get("id").asString());
        assertEquals(text, value.asObject().get("message").asObject().get("text").asString());
        assertEquals(2, value.asObject().get("message").asObject().get("quick_replies").asArray().size());
        
        final String url = "image_url";
        
        snippet = new IConversationSnippet(){

            @Override
            public String renderContent(IConversationState state) {
                return url;
            }

            @Override
            public SnippetType getType() {
                return SnippetType.STATEMENT;
            }

            @Override
            public SnippetContentType getContentType() {
                return SnippetContentType.IMAGE;
            }

            @Override
            public Iterable getSuggestedResponses(IConversationState state) {
                return null;
            }
            
        };
        
        phoneNumber = "+1415000000";
        result = instance.snippetToJson(snippet, null, phoneNumber);
        logger.info(result);
        value = Json.parse(result);
        assertEquals(phoneNumber, value.asObject().get("recipient").asObject().get("id").asString());
        assertEquals(url, value.asObject().get("message").asObject().get("attachment").asObject().get("payload").asObject().get("url").asString());
        assertEquals("image", value.asObject().get("message").asObject().get("attachment").asObject().get("type").asString());
    }
    
}
