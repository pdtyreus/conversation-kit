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
package com.synclab.conversationkit.adapter;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.synclab.conversationkit.model.IConversationSnippet;
import com.synclab.conversationkit.model.IConversationState;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class FacebookMessageAdapter implements IMessageAdapter {
    
    private static final Logger logger = Logger.getLogger(FacebookMessageAdapter.class.getName());

    /**
     * Generates the JSON string to indicate that the bot is typing.
     * @param userId Facebook user ID or phone number
     * @return String representation of JSON payload
     */
    public String typingOnToJson(String userId) {

        JsonObject json = Json.object()
                .add("recipient", Json.object()
                        .add("id", userId)).asObject();
        
        json.add("sender_action", "typing_on");
        
        logger.fine(json.toString());
        
        return json.toString();
    }
    
    @Override
    public String snippetToJson(IConversationSnippet snippet, IConversationState state, Object... arguments) {
        if (arguments.length == 0) {
            throw new IllegalArgumentException("The user's phone number or facebookId should be sent as the first argument.");
        }

        JsonObject json = Json.object()
                .add("recipient", Json.object()
                        .add("id", (String) arguments[0])).asObject();

        JsonValue quickReplies = null;
        if (snippet.getSuggestedResponses(state) != null) {
            List<String> quickReplyList = new ArrayList();
            for (Object suggestedResponse : snippet.getSuggestedResponses(state)) {
                quickReplyList.add((String) suggestedResponse);
            }
            if (!quickReplyList.isEmpty()) {
                quickReplies = Json.array();
                for (String suggestedResponse : quickReplyList) {
                    quickReplies.asArray().add(Json.object()
                            .add("content_type", "text")
                            .add("title", suggestedResponse)
                            .add("payload", suggestedResponse));
                }
            }
        }

        String type = "";
        switch (snippet.getContentType()) {
            case TEXT:
                JsonObject message = Json.object()
                        .add("text", snippet.renderContent(state));
                if (quickReplies != null) {
                    message.add("quick_replies", quickReplies);
                }
                json.add("message", message);
                return json.toString();
            case AUDIO:
                type = "audio";
                break;
            case VIDEO:
                type = "video";
                break;
            case FILE:
                type = "file";
                break;
            case IMAGE:
                type = "image";
                break;

        }
        JsonObject message = Json.object()
                .add("attachment", Json.object()
                        .add("type", type)
                        .add("payload", Json.object()
                                .add("url", snippet.renderContent(state))));
        if (quickReplies != null) {
            message.add("quick_replies", quickReplies);
        }
        json.add("message", message);
        
        logger.fine(json.toString());
        
        return json.toString();

    }

}
