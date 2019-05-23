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
package com.conversationkit.model;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface encapsulating the basic API for traversing a conversation.
 *
 * @param <N>
 */
public interface IConversationEngine<N extends IConversationNode> {

    /**
     * Accepts a message from the user, interprets the intent, and advances the conversation
     * to the next node along the edge determined by the conversation graph. 
     * <p>
     * If the engine
     * was able to successfully determine the intent and move along the conversation graph,
     * the {@link MessageHandlingResult} will have a value of <code>ok</code> set to <code>true</code>.
     * Otherwise an <code>errorCode</code> and <code>errorMessage</code> will be returned. An
     * <code>INTENT_UNDERSTANDING_FAILED</code> error will occur if the NLU system was
     * unable to match the input to an intent. An <code>EDGE_MATCHING_FAILED</code> will be
     * returned if no suitable outbound edge could be matched to the given input.
     * @param message
     * @return the result of handling the incoming message.
     */
    public CompletableFuture<MessageHandlingResult> handleIncomingMessage(String message);

    public static class MessageHandlingResult {

        public boolean ok;
        public ErrorCode errorCode;
        public String errorMessage;
    }

    public static enum ErrorCode {

        INTENT_UNDERSTANDING_FAILED, EDGE_MATCHING_FAILED
    }
}
