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
 * Interface encapsulating the basic API for traversing a conversation.
 * Implementation details may vary, but generally the conversation is started
 * from a given state and runs until it requires a response from the user. The
 * user response is then used to update the conversation state, and the
 * conversation is restarted (continued) with the updated state.
 * @author pdtyreus
 * @param <S> an implementation of to store the current state of the conversation
 * for the current user
 */
public interface IConversationEngine<S extends IConversationState> {
    /**
     * Follows the conversation graph starting at the current node defined by the state
     * parameter.  Implementation details may vary, but generally this method will
     * return all the <code>IConversationSnippet</code>s along the graph until
     * it reaches a node that requires a response from the user. The <code>state</code>
     * is updated to reflect the new <code>currentNodeId</code>.
     * 
     * @param state initial state
     * @return IConversationSnippets to display to the user
     */
    public Iterable<IConversationSnippet> startConversationFromState(S state);
    
    /**
     * Updates the conversation with a response from the user and advances the 
     * currentNodeId if a matching edge is found.
     * 
     * @param state the current IConversationState
     * @param response the user's response
     * @return the updated state
     * @throws UnmatchedResponseException if no outbound edges from the current node match the response
     * @throws UnexpectedResponseException if the conversation is in a state where it is not expecting a response (e.g. the current node is not a QUESTION)
     */
    public S updateStateWithResponse(S state, String response) throws UnmatchedResponseException, UnexpectedResponseException;
}
