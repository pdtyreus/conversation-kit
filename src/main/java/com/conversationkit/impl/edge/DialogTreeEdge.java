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
package com.conversationkit.impl.edge;

import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationState;

/**
 * A <code>DialogTreeEdge</code> is an implementation of
 * <code>IConversationEdge</code> that connects one
 * <code>IConversationNode</code> that is a <code>QUESTION</code> to the
 * <code>IConversationNode</code> matching the answer.
 * <p>
 * A Dialog Tree is a type of branching conversation often seen in adventure
 * video games. The user is given a choice of what to say and makes subsequent
 * choices until the conversation ends. The responses to the user are scripted
 * based on the choices made. Since the user can only answer questions using one
 * the supplied suggestions from the <code>DialogTreeNode</code>, this edge type
 * does a string match between the answer stored in the edge and the response
 * provided by the user.
 *
 * @author pdtyreus
 * @param <S> an implementation of IConversationState
 */
public class DialogTreeEdge extends ConversationEdge {


    /**
     * Only an exact match for the answer stored in this node will cause the
     * conversion to advance to the endNode. The value of the response will be
     * stored as the stateKey key in the conversation state.
     *
     * @param intent string value to match
     * @param endNode next node in the conversation
     */
    public DialogTreeEdge(String intent, IConversationNode endNode) {
        super(endNode,intent);
    }

    @Override
    public String toString() {
        return "DialogTreeEdge {" + getId() + '}';
    }
}
