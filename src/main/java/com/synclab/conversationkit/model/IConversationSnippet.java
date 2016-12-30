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
package com.synclab.conversationkit.model;

import java.util.List;

/**
 * A conversation snippet represents a small bit of dialog in a conversation.
 * In the case of a chat bot, this might represent a block of text sent as one
 * message. Snippets can be classified as a <code>STATEMENT</code> 
 * or <code>QUESTION</code>. Generally, if the snippet is a 
 * <code>STATEMENT</code> the conversation will proceed to the next 
 * automatically. If it is a <code>QUESTION</code>, the conversation will 
 * stop and wait for a response from the user. However, this depends on 
 * the <code>IConversationEngine</code> implementation.
 * <p>
 * Conversation snippets can also suggest possible responses. Some chat bot
 * clients like Facebook Messenger support displaying suggested responses in 
 * the interface while others like Slack do not.
 * 
 * @author pdtyreus
 * @param <S> an implementation of to store the current state of the conversation
 * for the current user
 */
public interface IConversationSnippet<S extends IConversationState> {
    
    /**
     * Returns the text to be displayed to the user. The state can be used to
     * modify the content at runtime to, for example, integrate data from
     * previous responses.
     * @param state the user's conversation state
     * @return the text to be displayed to the user.
     */
    public String renderContent(S state);
    /**
     * The snippet type influences the control flow of the IConversation
     * implementation.
     * @return the snippet type
     */
    public SnippetType getType();
    /**
     * The type of media held in the snippet content.
     * @return the snippet content type
     */
    public SnippetContentType getContentType();
    /**
     * Returns the suggested responses if supported by the chat bot client.
     * @return the suggested responses
     */
    public Iterable<String> getSuggestedResponses();
}
