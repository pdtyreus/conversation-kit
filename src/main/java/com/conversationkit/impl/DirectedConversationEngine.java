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

import com.conversationkit.impl.action.ActionType;
import com.conversationkit.impl.action.IntentFulfillmentSucceededAction;
import com.conversationkit.impl.action.MessageReceivedAction;
import com.conversationkit.model.ConversationStructureException;
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationNodeIndex;
import com.conversationkit.nlp.IntentDetector;
import com.conversationkit.redux.Action;
import com.conversationkit.redux.Dispatcher;
import com.conversationkit.redux.Store;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DirectedConversationEngine {

    private static Logger logger = Logger.getLogger(DirectedConversationEngine.class.getName());
    protected final IConversationNodeIndex nodeIndex;
    protected final IntentDetector<String> intentDetector;
    protected final Map<String, CompletableFuture<Action>> intentHandlers = new HashMap();
    protected final Map<String, Integer> fallbackIntentMap = new HashMap();
    protected final ExecutorService executorService;

    public DirectedConversationEngine(IntentDetector intentDetector, IConversationNodeIndex nodeIndex) {
        this.nodeIndex = nodeIndex;
        this.intentDetector = intentDetector;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public DirectedConversationEngine(IntentDetector intentDetector, IConversationNodeIndex nodeIndex, ExecutorService executorService) {
        this.nodeIndex = nodeIndex;
        this.intentDetector = intentDetector;
        this.executorService = executorService;
    }

    public void registerIntentFulfillment(String intentId, CompletableFuture<Action> action) {
        this.intentHandlers.put(intentId, action);
    }

    public void registerFallback(String intentId, Integer targetNodeId) {
        this.fallbackIntentMap.put(intentId, targetNodeId);
    }

    private CompletableFuture<IConversationNode> handleIntent(final Dispatcher store, final IConversationNode nextNode, Optional<CompletableFuture<Action>> action) {

        //IntentFulfillmentSucceededAction success = new IntentFulfillmentSucceededAction(nextNode);
        if (action.isPresent()) {
            return action.get().thenApply((Action a) -> {
                store.dispatch(a);
                return nextNode;
            });
        } else {
            return CompletableFuture.completedFuture(nextNode);
        }
    }

    private CompletableFuture<IConversationNode> handleIncomingIntent(Dispatcher dispatch, Map<String, Object> state, String intentId) {
        Integer currentNodeId = ConversationReducer.selectCurrentNodeId(state);
        Optional<IConversationNode> currentNode = Optional.empty();
        if (currentNodeId != null) {
            currentNode = Optional.ofNullable(nodeIndex.getNodeAtIndex(currentNodeId));
        }
        Optional<IConversationNode> nextNode = Optional.empty();
        Optional<CompletableFuture<Action>> handler = Optional.ofNullable(intentHandlers.get(intentId));
        if (handler.isPresent()) {
            logger.info(String.format("Found custom handler for intent %s", intentId));
        }
        //store.dispatch(new ConversationAction(ActionType.INTENT_FULFILLMENT_REQUESTED));

        if (currentNode.isPresent()) {

            Iterable<IConversationEdge> edges = currentNode.get().getEdges();
            for (IConversationEdge edge : edges) {
                //see if one of the possible edges matches
                if (intentId.equals(edge.getIntentId())) {
                    nextNode = Optional.ofNullable(edge.getEndNode());
                }
            }

            if (nextNode.isPresent()) {
                logger.info(String.format("Found matching next node %d for start node %d and intent %s", nextNode.get().getId(), currentNode.get().getId(), intentId));
            } else {
                Integer fallbackNextNodeId = fallbackIntentMap.get(intentId);
                if (fallbackNextNodeId == null) {
                    logger.info(String.format("No fallbacks mapped for intent %s", intentId));
                } else {
                    logger.info(String.format("Using fallback node %d mapped for start node %d and intent %s", fallbackNextNodeId, currentNode.get().getId(), intentId));
                    nextNode = Optional.ofNullable(nodeIndex.getNodeAtIndex(fallbackNextNodeId));
                }
            }

            if (!nextNode.isPresent()) {
                throw new ConversationStructureException("Node " + currentNode.get().getId() + " has no matching end node for intent " + intentId);
            }
            return handleIntent(dispatch, nextNode.get(), handler);

        } else {
            Integer fallbackNextNodeId = fallbackIntentMap.get(intentId);
            if (fallbackNextNodeId == null) {
                logger.info(String.format("No fallbacks mapped for intent %s", intentId));
            } else {
                logger.info(String.format("Using fallback node %d mapped for intent %s", fallbackNextNodeId, intentId));
                nextNode = Optional.ofNullable(nodeIndex.getNodeAtIndex(fallbackNextNodeId));
            }

            if (!nextNode.isPresent()) {
                throw new ConversationStructureException("No current node and no matching fallback for intent " + intentId);
            }
            return handleIntent(dispatch, nextNode.get(), handler);
        }
    }

    public CompletableFuture<MessageHandlingResult> handleIncomingMessage(Dispatcher store, Map<String, Object> state, String message) {

        //MessageHandlingResult result = new MessageHandlingResult();
        store.dispatch(new MessageReceivedAction(message));
        return intentDetector.detectIntent(message)
                .thenCompose((intent) -> {
                    if (intent.isPresent()) {
                        return handleIncomingIntent(store, state, intent.get())
                        .thenApply((n) -> {
                            store.dispatch(new IntentFulfillmentSucceededAction(n));
                            MessageHandlingResult result = new MessageHandlingResult();
                            result.ok = true;
                            return result;
                        }).exceptionally((t) -> {
                            //intent handling exception
                            MessageHandlingResult result = new MessageHandlingResult();
                            result.ok = false;
                            return result;
                        });
                    } else {
                        MessageHandlingResult result = new MessageHandlingResult();
                        result.ok = false;
                        result.errorCode = ErrorCode.INTENT_PROCESSING_FAILED;
                        return CompletableFuture.completedFuture(result);
                    }

                }).exceptionally((e) -> {
                    //intent processing exception
                    MessageHandlingResult result = new MessageHandlingResult();
                    result.ok = false;
                    result.errorCode = ErrorCode.INTENT_UNDERSTANDING_FAILED;
                    result.errorMessage = e.getMessage();
                    return result;

                });

    }

    public static class MessageHandlingResult {

        public boolean ok;
        public ErrorCode errorCode;
        public String errorMessage;
    }

    public static enum ErrorCode {

        INTENT_UNDERSTANDING_FAILED, INTENT_PROCESSING_FAILED
    }

}
