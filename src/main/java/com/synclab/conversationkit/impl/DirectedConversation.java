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
package com.synclab.conversationkit.impl;

import com.synclab.conversationkit.model.IConversation;
import com.synclab.conversationkit.model.IConversationEdge;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationNodeIndex;
import com.synclab.conversationkit.model.IConversationSnippet;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.UnmatchedResponseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DirectedConversation<S extends IConversationState> implements IConversation<S> {

    private static Logger logger = Logger.getLogger(DirectedConversation.class.getName());
    protected final IConversationNodeIndex<S> nodeIndex;

    public DirectedConversation(IConversationNodeIndex<S> nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public List<IConversationSnippet> startConversationFromState(S state) {
        List<IConversationSnippet> nodes = new ArrayList();
        IConversationNode<S> current = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        nodes.add(current);
        boolean continueTraverse = true;
        while (continueTraverse && !current.getEdges().isEmpty()) {
            //if nothing has matched, we are done
            continueTraverse = false;
            for (IConversationEdge<S> edge : current.getEdges()) {
                //find the first edge that matches and move to that node
                logger.fine(String.format("inspecting possible edge %s", edge));
                if (edge.isMatchForState(state)) {
                    logger.info(String.format("edge '%s' matches", edge));
                    current = edge.getEndNode();
                    nodes.add(current);
                    state.setCurrentNodeId(current.getId());
                    continueTraverse = true;
                }
            }
        }
        return nodes;
    }

    public S updateStateWithResponse(S state, String response) throws UnmatchedResponseException {
        IConversationNode<S> currentSnippet = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        state.setCurrentResponse(response);
        logger.fine(String.format("processing response '%s' for node of type %s with %d possible answers", response, currentSnippet.getType(), currentSnippet.getEdges().size()));
        boolean matchFound = false;

        for (IConversationEdge<S> allowedAnswer : currentSnippet.getEdges()) {
            logger.fine(String.format("inspecting possible answer %s", allowedAnswer));

            if (allowedAnswer.isMatchForState(state)) {
                IConversationNode nextNode = allowedAnswer.getEndNode();
                state.setCurrentNodeId(nextNode.getId());
                logger.info(String.format("response '%s' matches answer %s", response, allowedAnswer));
                matchFound = true;
                state = allowedAnswer.onMatch(state);
            }
        }

        if (!matchFound) {
            throw new UnmatchedResponseException();
        } else {
            state.setCurrentResponse(null);
        }

        return state;
    }
}
