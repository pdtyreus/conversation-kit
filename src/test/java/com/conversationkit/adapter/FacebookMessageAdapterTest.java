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

import com.conversationkit.impl.node.ConversationNodeButton;
import com.conversationkit.impl.node.ResponseSuggestingNode;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.conversationkit.model.SnippetContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author pdtyreus
 */
public class FacebookMessageAdapterTest {
    
    private FacebookMessageAdapter instance = new FacebookMessageAdapter();
    private static final Logger logger = Logger.getLogger(FacebookMessageAdapterTest.class.getName());


    @Test
    public void testSnippetToJson() {
        final String text =  "Facebook Messenger";
        
//        ResponseSuggestingNode snippet = new ResponseSuggestingNode(1,text,SnippetContentType.TEXT);
//        
//        
//        String phoneNumber = "+1415000000";
//        String result = instance.responseToJson(snippet.getValue(), phoneNumber, snippet.getContentType(), snippet.getSuggestedResponses());
//        logger.info(result);
//        JsonValue value = Json.parse(result);
//        assertEquals(phoneNumber, value.asObject().get("recipient").asObject().get("id").asString());
//        assertEquals(text, value.asObject().get("message").asObject().get("text").asString());
//        
//        snippet = new ResponseSuggestingNode(1,text,SnippetContentType.TEXT);
//        snippet.addSuggestedResponse("yes");
//        snippet.addSuggestedResponse("no");
//        
//        result = instance.responseToJson(snippet.getValue(), phoneNumber, snippet.getContentType(), snippet.getSuggestedResponses());
//        logger.info(result);
//        value = Json.parse(result);
//        assertEquals(phoneNumber, value.asObject().get("recipient").asObject().get("id").asString());
//        assertEquals(text, value.asObject().get("message").asObject().get("text").asString());
//        assertEquals(2, value.asObject().get("message").asObject().get("quick_replies").asArray().size());
//        
//        final String url = "image_url";
//        
//        snippet = new ResponseSuggestingNode(1,url,SnippetContentType.IMAGE);
//        
//        phoneNumber = "+1415000000";
//        result = instance.responseToJson(snippet.getValue(), phoneNumber, snippet.getContentType(), snippet.getSuggestedResponses());
//        logger.info(result);
//        value = Json.parse(result);
//        assertEquals(phoneNumber, value.asObject().get("recipient").asObject().get("id").asString());
//        assertEquals(url, value.asObject().get("message").asObject().get("attachment").asObject().get("payload").asObject().get("url").asString());
//        assertEquals("image", value.asObject().get("message").asObject().get("attachment").asObject().get("type").asString());
//        
//        snippet = new ResponseSuggestingNode(1,text,SnippetContentType.LOCATION_REQUEST);
//        snippet.addSuggestedResponse("decline");
//        
//        phoneNumber = "+1415000000";
//        result = instance.responseToJson(snippet.getValue(), phoneNumber, snippet.getContentType(), snippet.getSuggestedResponses());
//        logger.info(result);
//        value = Json.parse(result);
//        assertEquals(text, value.asObject().get("message").asObject().get("text").asString());
//        assertEquals(2, value.asObject().get("message").asObject().get("quick_replies").asArray().size());
//        assertEquals("location", value.asObject().get("message").asObject().get("quick_replies").asArray().get(0).asObject().get("content_type").asString());
//        
//        snippet = new ResponseSuggestingNode(1,text,SnippetContentType.BUTTONS);
//        Map<String,Object> atts = new HashMap();
//        atts.put("webview_height_ratio","compact");
//        snippet.addButton(new ConversationNodeButton("web_url","View Item","https://petersfancyapparel.com/classic_white_tshirt",atts));
//        result = instance.responseToJson(snippet.getValue(), phoneNumber, snippet.getContentType(), snippet.getSuggestedResponses(), snippet.getButtons());
//        logger.info(result);
//        value = Json.parse(result);
//        assertEquals("button", value.asObject().get("message").asObject().get("attachment").asObject().get("payload").asObject().get("template_type").asString());
//        assertEquals(1, value.asObject().get("message").asObject().get("attachment").asObject().get("payload").asObject().get("buttons").asArray().size());
    }
    
}
