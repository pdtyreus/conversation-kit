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
package com.conversationkit.impl;

import com.conversationkit.impl.action.ActionType;
import java.util.Optional;

/**
 * A Redix Action implementation that carries a typed payload and is limited to 
 * conversation-based enums as its Action <code>type</code>.
 * @author pdtyreus
 * @param <S> type of payload
 */
public class ConversationAction<S> implements PayloadAction<S> {

    private final ActionType type;
    private final Optional<S> payload;

    public ConversationAction(ActionType type, S payload) {
        this.type = type;
        this.payload = Optional.ofNullable(payload);
    }
    
    public ConversationAction(ActionType type) {
        this.type = type;
        this.payload = Optional.empty();
    }

    @Override
    public String getType() {
        return type.name();
    }
    
    public Optional<S> getPayload() {
        return this.payload;
    }
    
    public ActionType getActionType() {
        return this.type;
    }
}
