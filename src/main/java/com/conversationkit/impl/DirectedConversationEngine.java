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
package com.conversationkit.impl;

import com.conversationkit.model.IConversationEngine;
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationNodeIndex;
import com.conversationkit.model.IConversationSnippet;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import com.conversationkit.model.UnexpectedResponseException;
import com.conversationkit.model.UnmatchedResponseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DirectedConversationEngine<R, S extends IMapBackedState> implements IConversationEngine<S> {

    private static Logger logger = Logger.getLogger(DirectedConversationEngine.class.getName());
    protected final IConversationNodeIndex<R, S> nodeIndex;

    public DirectedConversationEngine(IConversationNodeIndex<R, S> nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    @Override
    public Iterable<IConversationSnippet> startConversationFromState(S state) {
        List<IConversationSnippet> nodes = new ArrayList();
        IConversationNode<R, S> nextNode = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        if (nextNode.getContentType() != SnippetContentType.NOTHING) {
            nodes.add(nextNode);
        }
        boolean matchFound = true;
        while (matchFound && (nextNode.getType() == SnippetType.STATEMENT)) {
            //if nothing has matched, we are done
            matchFound = false;
            int count = 0;
            for (IConversationEdge<R, S> edge : nextNode.getEdges()) {
                count++;
            }
            logger.fine(String.format("node %d has %d outbound edge(s)", nextNode.getId(),count));
            for (IConversationEdge<R, S> edge : nextNode.getEdges()) {
                //find the first edge that matches and move to that node
                if (!matchFound) {
                    logger.fine(String.format("evaluating STATEMENT edge %s", edge));
                    Optional<R> response = Optional.empty();
                    if (edge.isMatchForState(response, state)) {
                        matchFound = true;
                        state = moveToNextNode(state, edge);
                        nextNode = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
                        if (nextNode.getContentType() != SnippetContentType.NOTHING) {
                            nodes.add(nextNode);
                        }
                    }
                }
            }
        }
        return nodes;
    }

    @Override
    public S updateStateWithResponse(S state, String response) throws UnmatchedResponseException, UnexpectedResponseException {
        IConversationNode<R, S> currentNode = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        logger.info(String.format("node %d [%s] processing response '%s'", state.getCurrentNodeId(), currentNode.getType(), response));
            
        if (currentNode.getType() == SnippetType.QUESTION) {
            Optional<R> transformedResponse = currentNode.transformResponse(Optional.ofNullable(response));
            Optional<Map<String, Object>> newState = currentNode.transformState(transformedResponse, state);

            if (newState.isPresent()) {
                for (Map.Entry<String, Object> entry : newState.get().entrySet()) {
                    logger.fine(String.format("updating state key '%s' to '%s'", entry.getKey(), entry.getValue()));
                    state.put(entry.getKey(), entry.getValue());
                }
            }

            logger.info(String.format("node %d transformed response '%s' to '%s'", state.getCurrentNodeId(), response, (transformedResponse.isPresent() ? transformedResponse.get() : null)));
            boolean matchFound = false;

            for (IConversationEdge<R, S> edge : currentNode.getEdges()) {
                if (!matchFound) {
                    logger.fine(String.format("inspecting possible edge %s", edge));

                    if (edge.isMatchForState(transformedResponse, state)) {
                        matchFound = true;
                        moveToNextNode(state, edge);
                    }
                }
            }

            if (!matchFound) {
                throw new UnmatchedResponseException();
            }
        } else {
            throw new UnexpectedResponseException(String.format("received response '%s' to node %d which is not a QUESTION", response, state.getCurrentNodeId()));
        }

        return state;
    }

    private S moveToNextNode(S state, IConversationEdge<R, S> edge) {
        IConversationNode nextNode = edge.getEndNode();
        state.setCurrentNodeId(nextNode.getId());
        logger.fine(String.format("match %s", edge));
        logger.fine(String.format("next node is %d '%s' of type %s", nextNode.getId(), nextNode.renderContent(state), nextNode.getType()));
        return state;
    }
}
