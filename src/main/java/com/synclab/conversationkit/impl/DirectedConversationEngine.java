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

import com.synclab.conversationkit.model.IConversationEngine;
import com.synclab.conversationkit.model.IConversationEdge;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationNodeIndex;
import com.synclab.conversationkit.model.IConversationSnippet;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.SnippetType;
import com.synclab.conversationkit.model.UnmatchedResponseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DirectedConversationEngine<S extends IConversationState> implements IConversationEngine<S> {

    private static Logger logger = Logger.getLogger(DirectedConversationEngine.class.getName());
    protected final IConversationNodeIndex<S> nodeIndex;

    public DirectedConversationEngine(IConversationNodeIndex<S> nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    @Override
    public Iterable<IConversationSnippet> startConversationFromState(S state) {
        List<IConversationSnippet> nodes = new ArrayList();
        IConversationNode<S> nextNode = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        nodes.add(nextNode);
        boolean matchFound = true;
        while (matchFound && (nextNode.getType() == SnippetType.STATEMENT)) {
            //if nothing has matched, we are done
            matchFound = false;
            for (IConversationEdge<S> edge : nextNode.getEdges()) {
                //find the first edge that matches and move to that node
                if (!matchFound) {
                    logger.fine(String.format("evaluating STATEMENT edge %s", edge));
                    if (edge.isMatchForState(state)) {
                        matchFound = true;
                        state = moveToNextNode(state, edge);
                        nextNode = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
                        nodes.add(nextNode);
                    }
                }
            }
        }
        return nodes;
    }

    @Override
    public S updateStateWithResponse(S state, String response) throws UnmatchedResponseException {
        IConversationNode<S> currentSnippet = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());

        if (currentSnippet.getType() == SnippetType.QUESTION) {
            state.setMostRecentResponse(response);
            logger.fine(String.format("processing response '%s' for node of type %s", response, currentSnippet.getType()));
            boolean matchFound = false;

            for (IConversationEdge<S> edge : currentSnippet.getEdges()) {
                if (!matchFound) {
                    logger.fine(String.format("inspecting possible edge %s, already matched %s", edge, matchFound));

                    if (edge.isMatchForState(state)) {
                        matchFound = true;
                        moveToNextNode(state, edge);
                    }
                }
            }

            if (!matchFound) {
                throw new UnmatchedResponseException();
            }
        } else {
            logger.warning("trying to add response to conversation but current node is not a QUESTION");
            throw new UnmatchedResponseException();
        }

        return state;
    }

    private S moveToNextNode(S state, IConversationEdge<S> edge) {
        IConversationNode nextNode = edge.getEndNode();
        state.setCurrentNodeId(nextNode.getId());
        if (state.getMostRecentResponse() != null) {
            logger.info(String.format("response '%s' matches edge '%s'", state.getMostRecentResponse(), edge));
        } else {
            logger.info(String.format("edge '%s' matches", edge));
        }
        logger.fine(String.format("adding node '%s' of type %s", nextNode.renderContent(state), nextNode.getType()));
        edge.onMatch(state);
        return state;
    }
}
