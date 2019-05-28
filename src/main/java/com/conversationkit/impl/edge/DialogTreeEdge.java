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

import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.IConversationState;
import java.util.function.BiFunction;

/**
 * A <code>DialogTreeEdge</code> is an implementation of
 * <code>IConversationEdge</code> that connects one
 * <code>IConversationNode</code> that is a question to the
 * <code>IConversationNode</code> matching the answer.
 * <p>
 * A Dialog Tree is a type of branching conversation often seen in adventure
 * video games. The user is given a choice of what to say and makes subsequent
 * choices until the conversation ends. The responses to the user are scripted
 * based on the choices made. Since the user can only answer questions using one
 * the supplied suggestions from the {@link DialogTreeNode}, this edge type does
 * a string match between the answer stored in the edge and the response
 * provided by the user.
 *
 * @author pdtyreus
 */
public class DialogTreeEdge extends ConversationEdge {

    private final String prompt;

    /**
     * Only an exact match for the answer stored in this node will cause the
     * conversion to advance to the endNode.
     *
     * @param prompt string value to match
     * @param intentId intent id
     * @param endNodeId next node id in the conversation
     */
    public DialogTreeEdge(Integer endNodeId, String intentId, String prompt) {
        super(endNodeId, intentId);
        this.prompt = prompt;
    }

    public DialogTreeEdge(Integer endNodeId, String intentId, String prompt, BiFunction<IConversationIntent, IConversationState, Object>... sideEffects) {
        super(endNodeId, intentId, (intent, state) -> {
            return true;
        }, sideEffects);
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "DialogTreeEdge {" + getIntentId() + '}';
    }

    public String getPrompt() {
        return prompt;
    }
}
