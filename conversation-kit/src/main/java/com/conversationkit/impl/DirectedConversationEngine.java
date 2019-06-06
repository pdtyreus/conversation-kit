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
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationEngine;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.ConversationNodeRepository;
import com.conversationkit.model.IConversationState;
import com.conversationkit.nlp.IntentDetector;
import com.conversationkit.redux.Dispatcher;
import com.conversationkit.redux.Reducer;
import com.conversationkit.redux.Redux;
import com.conversationkit.redux.Store;
import com.conversationkit.redux.impl.CompletableFutureMiddleware;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of IConversationEngine that holds the current state of a conversation
 * and determines the flow by interpreting user intent and matching it to the conversation graph.
 * <p>
 * The engine creates a Redux {@link Store} when initialized to hold the state of the conversation
 * for a given user. The state is only mutated 
 * by {@link DirectedConversationEngine#dispatch }ing actions. This provides
 * predictable state that is easier to test.
 * <p>
 * The {@link IntentDetector} is responsible for matching the user input with an 
 * edge on the conversation graph. Most likely you will want to wrap a commercial
 * NLU service like Amazon Lex or Google DialogFlow.
 * <p>
 * The general flow of {@link #handleIncomingMessage(java.lang.String)} in this implementation is as follows:
 * <ol>
 * <li>Receive a message and update the state with that message.</li>
 * <li>Delegate to the IntentDetector to try to determine the user's intent.</li>
 * <li>Loop over all outbound edges for the current node and find the first edge that 
 * matches the intent and has {@link IConversationEdge#validate }
 * that returns true.</li>
 * <li>Dispatch any side effects for the matched edge and process on the Redux middleware chain.</li>
 * <li>Update the state with the new node id.</li>
 * </ol>
 * <p>
 * If any of the above steps fail, {@link #handleIncomingMessage} will return an error.
 * 
 * @author pdtyreus
 * @param <S> type of IConversationState
 * @param <I> type of IConversationIntent
 */
public class DirectedConversationEngine<S extends IConversationState, I extends IConversationIntent> implements Dispatcher, IConversationEngine {

    private static Logger logger = Logger.getLogger(DirectedConversationEngine.class.getName());
    protected final ConversationNodeRepository nodeRepository;
    protected final IntentDetector<I> intentDetector;
    protected final List<IConversationEdge> fallbackEdges = new ArrayList();
    protected final Store<S> store;

    public final static String CONVERSATION_STATE_KEY = "conversation-kit";

    public DirectedConversationEngine(IntentDetector<I> intentDetector, ConversationNodeRepository nodeRepository, S state) {
        this(intentDetector, nodeRepository, state, new HashMap());
    }

    public DirectedConversationEngine(IntentDetector<I> intentDetector, ConversationNodeRepository nodeRepository, S state, Map<String, Reducer> reducers) {
        this.nodeRepository = nodeRepository;
        this.intentDetector = intentDetector;
        reducers.put(CONVERSATION_STATE_KEY, new ConversationReducer());
        Reducer reducer = Redux.combineReducers(reducers);
        store = Redux.createStore(reducer, state.getStateAsMap(), state, new CompletableFutureMiddleware());
    }

    public void addFallbackEdge(IConversationEdge edge) {
        fallbackEdges.add(edge);
    }

    public S getState() {
        return store.getState();
    }

    private Optional<IConversationEdge> findEdgeMatchingIntent(I intent, Optional<IConversationNode> currentNode) {
        if (currentNode.isPresent()) {
            Iterable<IConversationEdge> edges = currentNode.get().getEdges();
            for (IConversationEdge edge : edges) {
                if (edge.getIntentId().equals(intent.getIntentId())) {
                    logger.log(Level.INFO, "Found unvalidated matching edge with end node {0} for intent {1}", Arrays.asList(edge.getEndNodeId(), intent.getIntentId()).toArray());
                    boolean valid = edge.validate(intent, store.getState());
                    if (valid) {
                        logger.log(Level.INFO, "Edge with end node {0} for intent {1} validates.", Arrays.asList(edge.getEndNodeId(), intent.getIntentId()).toArray());
                        return Optional.of(edge);
                    } else {
                        logger.log(Level.INFO, "Edge with end node {0} for intent {1} does not validate.", Arrays.asList(edge.getEndNodeId(), intent.getIntentId()).toArray());
                    }
                }
            }
        }
        logger.log(Level.INFO, "No matching connected edge for intent {0}", intent.getIntentId());

        for (IConversationEdge edge : fallbackEdges) {
            if (edge.getIntentId().equals(intent.getIntentId())) {
                logger.log(Level.INFO, "Found unvalidated matching fallback edge with end node {0} for intent {1}", Arrays.asList(edge.getEndNodeId(), intent.getIntentId()).toArray());
                boolean valid = edge.validate(intent, store.getState());
                if (valid) {
                    logger.log(Level.INFO, "Fallback edge with end node {0} for intent {1} validates.", Arrays.asList(edge.getEndNodeId(), intent.getIntentId()).toArray());
                    return Optional.of(edge);
                } else {
                    logger.log(Level.INFO, "Fallback edge with end node {0} for intent {1} does not validate.", Arrays.asList(edge.getEndNodeId(), intent.getIntentId()).toArray());
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public CompletableFuture<MessageHandlingResult> handleIncomingMessage(String message) {

        Integer currentNodeId = store.getState().getCurrentNodeId();
        final Optional<IConversationNode> currentNode
                = (currentNodeId != null)
                        ? Optional.ofNullable(nodeRepository.getNodeById(currentNodeId))
                        : Optional.empty();

        dispatch(new ConversationAction<>(ActionType.MESSAGE_RECEIVED, message));
        return intentDetector.detectIntent(message,"en-US",store.getState().getUserId())
                .thenApply((intent) -> {
                    MessageHandlingResult result = new MessageHandlingResult();
                    if (intent.isPresent()) {
                        I conversationIntent = intent.get();
                        dispatch(new ConversationAction(ActionType.INTENT_UNDERSTANDING_SUCCEEDED, conversationIntent));
                        Optional<IConversationEdge> outboundEdge = findEdgeMatchingIntent(conversationIntent, currentNode);
                        if (!outboundEdge.isPresent()) {
                            dispatch(new ConversationAction(ActionType.EDGE_MATCH_FAILED));

                            result.ok = false;
                            result.errorCode = ErrorCode.EDGE_MATCHING_FAILED;
                        } else {
                            List<Object> sideEffects = outboundEdge.get().getSideEffects(conversationIntent, store.getState());
                            for (Object effect : sideEffects) {
                                logger.log(Level.INFO, "Dispatching side effect {0}.", effect);
                                dispatch(effect);
                            }
                            IConversationNode nextNode = nodeRepository.getNodeById(outboundEdge.get().getEndNodeId());
                            dispatch(new ConversationAction<>(ActionType.EDGE_MATCH_SUCCEEDED, nextNode));
                            result.ok = true;
                        }

                    } else {
                        dispatch(new ConversationAction(ActionType.INTENT_UNDERSTANDING_FAILED));
                        result.ok = false;
                        result.errorCode = ErrorCode.INTENT_UNDERSTANDING_FAILED;

                    }

                    return result;
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
    public S dispatch(Object action) {
        return store.dispatch(action);
    }

}
