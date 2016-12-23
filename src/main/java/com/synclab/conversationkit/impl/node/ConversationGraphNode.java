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
package com.synclab.conversationkit.impl.node;

import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.SnippetType;
import java.util.ArrayList;
import java.util.List;

/**
 * A flexible implementation of IConversationNode that can specify but not 
 * require suggested answers to questions. However, no checks are made to ensure
 * that the suggested responses actually match an outbound edge.
 * 
 * @author pdtyreus
 * @param <S> an implementation of IConversationState to store the state of the 
 * conversation for the current user
 */
public class ConversationGraphNode<S extends IConversationState> extends ConversationNode<S> {

    protected final List<String> suggestedResponses;
    protected final String content;
    
    public ConversationGraphNode(int id, SnippetType type, String content) {
        super(id, type);
        this.suggestedResponses = new ArrayList();
        this.content = content;
    }

    public String renderContent(S state) {
        return content;
    }

    public Iterable<String> getSuggestedResponses() {
        return suggestedResponses;
    }
    
    public void addSuggestedResponse(String response) {
        if (getType() == SnippetType.STATEMENT) {
            throw new IllegalArgumentException("STATEMENTS cannot have suggested responses");
        }
        suggestedResponses.add(response);
    }
}
