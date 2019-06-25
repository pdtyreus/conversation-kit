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

import java.util.Map;
import java.util.function.Function;

/**
 * The conversation state is a data store designed to persist a user's progress
 * through a conversation, help customize the messages sent by the bot to the 
 * user, and to save data from user responses during the conversation. In many
 * cases the implementation will be backed by a database or other permanent
 * storage.
 * <p>
 * The state implementation must also know how to construct itself from a {@link Map} and
 * therefore this interface extends {@link Function}.
 * @author pdtyreus
 * @param <S> generic type of the state
 */
public interface IConversationState <S extends IConversationState> extends Function<Map,S> {
    /**
     * @return the id of the node representing the last message sent to the user
     */
    public Integer getCurrentNodeId();
    /**
     * @return the number of consecutive times the engine has misunderstood the user's input.
     */
    public Integer getMisunderstoodCount();
    /**
     * @return a unique string representation of the current user
     */
    public String getUserId();
    /**
     * @return the current state as a Map
     */
    public Map getStateAsMap();
}
