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

import com.conversationkit.redux.Store;

/**
 * A conversation edge is a directed connection between two nodes on the 
 * conversation graph. Each edge has exactly one start node and one end node, 
 * but a node frequently has multiple outbound edges. The conversation
 * implementation will look at each outbound edge from a node to decide which
 * edge to use to continue traversing the conversation graph.
 * 
 * @author pdtyreus
 * @param <S> an implementation of to store the current state of the conversation
 * for the current user
 */
public interface IConversationEdge<I extends IConversationIntent> {
    /**
     * Returns the next node in the conversation graph along this edge. The 
     * conversation will proceed along this edge if the conversation state 
     * matches the criteria stored in the edge.
     * @return the node at the end of this edge
     */
    public IConversationNode getEndNode();
    
    public String getIntentId();
    
    public boolean validate(I intent, Store store);
}
