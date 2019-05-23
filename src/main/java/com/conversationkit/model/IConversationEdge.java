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

/**
 * A conversation edge is a directed connection between two nodes on the 
 * conversation graph. Each edge has exactly one start node and one end node, 
 * but a node frequently has multiple outbound edges. Each edge represents a particular
 * intent {@link IConversationIntent} by the user. The conversation
 * implementation will delegate to a NLU engine to determine a user's intent from 
 * his or her input and then look at each outbound edge from a node to decide which
 * edge to use to continue traversing the conversation graph.
 * 
 * @author pdtyreus
 * @param <S> an implementation of to store the current state of the conversation
 * for the current user
 */
public interface IConversationEdge<I extends IConversationIntent, S extends IConversationState> {

    public Integer getEndNodeId();
    
    public String getIntentId();
    
    /**
     * Additional logic to perform before continuing the conversation along this edge. It's possible for
     * a node to have multiple edges with the same intentId. Each edge could have different preconditions
     * that must be met in order to match. The validate function allows the engine to check the preconditions
     * against the current state and return true or false. If the edge intent matches but the validate
     * fails, the engine will look at the next edge with the same intent.
     * @param intent
     * @param state
     * @return 
     */
    public boolean validate(I intent, S state);
    
    /**
     * Side effects that should occur if this edge is validated. Side effects are
     * ways to change the state before the next step in the conversation. The type
     * of Object returned depends on the {@link Redux} {@link Middleware}s that are installed.
     * <p>
     * Side effects should be used to perform actions like loading additional data
     * from a web service or database that is required to respond to the user. The state
     * that is passed in should not be modified directly. Instead the list of actions
     * returned will be passed sequentially to the Redux middleware chain and executed in order.
     * @param intent the validated intent
     * @param state immutable copy of the state
     * @return a list of actions to perform before the next conversation step
     */
    public List<Object> getSideEffects(I intent, S state);
}
