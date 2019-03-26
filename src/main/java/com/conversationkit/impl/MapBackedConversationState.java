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

import com.conversationkit.model.IConversationState;
import java.util.Map;

/**
 *
 * @author pdtyreus
 */
public class MapBackedConversationState implements IConversationState {

    protected final Map source;
    private final String conversationKey;

    public MapBackedConversationState(Map source) {
        this.source = source;
        conversationKey = null;
    }

    public MapBackedConversationState(Map source, String conversationKey) {
        this.source = source;
        this.conversationKey = conversationKey;
    }

    private Map getConversationMap() {
        if (conversationKey == null) {
            return source;
        } else {
            return (Map) source.get(conversationKey);
        }
    }

    @Override
    public Integer getCurrentNodeId() {
        return (Integer) getConversationMap().get("nodeId");
    }

    @Override
    public String getIntentId() {
        return (String) getConversationMap().get("intentId");
    }

    @Override
    public String getEdgeId() {
        return (String) getConversationMap().get("edgeId");
    }

    @Override
    public Integer getMisunderstoodCount() {
        return (Integer) getConversationMap().get("misunderstoodCount");
    }

}
