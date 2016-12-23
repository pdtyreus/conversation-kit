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
package com.synclab.conversationkit.impl.edge;

import com.synclab.conversationkit.model.IConversationEdge;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationState;

/**
 * A simple IConversationEdge implementation that always matches. This 
 * implementation would be best used for connecting multiple 
 * {@link com.synclab.conversationkit.model.SnippetType}.STATEMENT nodes.
 * 
 * @author pdtyreus
 * @param <S> an implementation of IConversationState
 */
public class StatementEdge<S extends IConversationState> implements IConversationEdge<S> {

    private final IConversationNode endNode;

    public StatementEdge(IConversationNode endNode) {
        this.endNode = endNode;
    }

    public IConversationNode getEndNode() {
        return endNode;
    }

    @Override
    public String toString() {
        return "ConversationEdge {" + endNode.renderContent(null) + '}';
    }

    public boolean isMatchForState(S state) {
        return true;
    }

    public S onMatch(S state) {
        return state;
    }

}
