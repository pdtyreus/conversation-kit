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
import com.conversationkit.redux.Reducer;
import com.conversationkit.redux.Redux;
import com.conversationkit.redux.Store;
import com.conversationkit.redux.impl.CompletableFutureMiddleware;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DirectedConversationEngine implements Dispatcher {

    private static Logger logger = Logger.getLogger(DirectedConversationEngine.class.getName());
    protected final IConversationNodeIndex nodeIndex;
    protected final IntentDetector<String> intentDetector;
    protected final Map<String, Supplier<Action>> intentHandlers = new HashMap();
    protected final Map<String, Integer> fallbackIntentMap = new HashMap();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    protected final Store store;

    public final static String CONVERSATION_STATE_KEY = "conversation-kit";

    public DirectedConversationEngine(IntentDetector intentDetector, IConversationNodeIndex nodeIndex, Map initialState) {
        this(intentDetector,nodeIndex,initialState,new HashMap());
    }

    public DirectedConversationEngine(IntentDetector intentDetector, IConversationNodeIndex nodeIndex, Map initialState, Map<String, Reducer> reducers) {
        this.nodeIndex = nodeIndex;
        this.intentDetector = intentDetector;
        reducers.put(CONVERSATION_STATE_KEY, new ConversationReducer());
        Reducer reducer = Redux.combineReducers(reducers);
        store = Redux.createStore(reducer, initialState);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    public void registerIntentFulfillment(String intentId, Supplier<Action> action) {
        this.intentHandlers.put(intentId, action);
    }

    public void registerFallback(String intentId, Integer targetNodeId) {
        this.fallbackIntentMap.put(intentId, targetNodeId);
    }

    public Map<String, Object> selectState(String key) {
        return (Map<String, Object>) store.getState().get(key);
    }

    private CompletableFuture<IConversationNode> handleIntent(final IConversationNode nextNode, Optional<Supplier<Action>> optionalFuture) {

        if (optionalFuture.isPresent()) {
            return CompletableFuture.supplyAsync(optionalFuture.get(),executorService)
                    .thenApply((Action a) -> {
                        store.dispatch(a);
                        return nextNode;
                    });
        } else {
            return CompletableFuture.completedFuture(nextNode);
        }
    }

    private CompletableFuture<IConversationNode> handleIncomingIntent(String intentId) {
        Integer currentNodeId = ConversationReducer.selectCurrentNodeId(selectState(CONVERSATION_STATE_KEY));
        Optional<IConversationNode> currentNode = Optional.empty();
        if (currentNodeId != null) {
            currentNode = Optional.ofNullable(nodeIndex.getNodeAtIndex(currentNodeId));
        }
        Optional<IConversationNode> nextNode = Optional.empty();
        Optional<Supplier<Action>> handler = Optional.ofNullable(intentHandlers.get(intentId));
        if (handler.isPresent()) {
            logger.info(String.format("Found custom handler for intent %s", intentId));
        }

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
            return handleIntent(nextNode.get(), handler);

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
            return handleIntent(nextNode.get(), handler);
        }
    }

    public CompletableFuture<MessageHandlingResult> handleIncomingMessage(String message) {

        store.dispatch(new MessageReceivedAction(message));
        return intentDetector.detectIntent(message)
                .thenCompose((intent) -> {
                    if (intent.isPresent()) {
                        return handleIncomingIntent(intent.get())
                        .thenApply((n) -> {
                            store.dispatch(new IntentFulfillmentSucceededAction(n));
                            MessageHandlingResult result = new MessageHandlingResult();
                            result.ok = true;
                            return result;
                        }).exceptionally((t) -> {
                            //intent handling exception
                            MessageHandlingResult result = new MessageHandlingResult();
                            result.ok = false;
                            result.errorCode = ErrorCode.INTENT_PROCESSING_FAILED;
                            return result;
                        });
                    } else {
                        MessageHandlingResult result = new MessageHandlingResult();
                        result.ok = false;
                        result.errorCode = ErrorCode.INTENT_UNDERSTANDING_FAILED;
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

    @Override
    public Map<String, Object> dispatch(Object action) {
        return store.dispatch(action);
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
