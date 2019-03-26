/*
 * The MIT License
 *
 * Copyright 2019 Synclab Consulting LLC.
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

import com.conversationkit.impl.DirectedConversationEngine.ErrorCode;
import com.conversationkit.impl.action.MappedIntentToEdgeAction;
import com.conversationkit.impl.edge.ConversationEdge;
import com.conversationkit.impl.node.ConversationNode;
import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.nlp.RegexIntentDetector;
import com.conversationkit.redux.Action;
import com.conversationkit.redux.Reducer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tyreus
 */
public class DirectedConversationEngineTest {

    public static MapBackedNodeIndex index;
    public static HashMap<String, Object> initialState;

    public static class TestState extends MapBackedConversationState {

        public TestState(Map source) {
            super(source, DirectedConversationEngine.CONVERSATION_STATE_KEY);
        }

        public boolean isRight() {
            Map custom = (Map) source.get("custom");
            return (Boolean) custom.get("right");
        }
    }

    @BeforeClass
    public static void createIndex() {
        ConversationNode top = new DialogTreeNode(1, Arrays.asList("top"), (intent, store) -> {
            System.out.println("rightIntent action work");

            if ("rightIntent".equals(intent)) {
                store.dispatch(new Action() {

                    @Override
                    public String getType() {
                        return "right_handled";
                    }

                    @Override
                    public String toString() {
                        return "rightIntent Action";
                    }

                });
            }

            return new MappedIntentToEdgeAction(intent);

        });
        ConversationNode left = new DialogTreeNode(2, Arrays.asList("left"));
        ConversationNode right = new DialogTreeNode(3, Arrays.asList("right"));
        ConversationEdge leftEdge = new ConversationEdge(left, "leftIntent");
        ConversationEdge rightEdge = new ConversationEdge(right, "rightIntent");
        top.addEdge(leftEdge);
        top.addEdge(rightEdge);

        index = new MapBackedNodeIndex();
        index.addNodeToIndex(1, top);
        index.addNodeToIndex(2, left);
        index.addNodeToIndex(3, right);
    }

    @BeforeClass
    public static void initializeState() {
        HashMap initialConversationState = new HashMap();
        initialConversationState.put("nodeId", 1);

        HashMap initialCustomState = new HashMap();
        initialCustomState.put("right", false);

        initialState = new HashMap();
        initialState.put(DirectedConversationEngine.CONVERSATION_STATE_KEY, initialConversationState);
        initialState.put("custom", initialCustomState);

    }

    @Test
    public void testHandleMessageLeft() {

        Map<String, String> intentMap = new HashMap();
        intentMap.put("leftIntent", "left");
        intentMap.put("rightIntent", "right");
        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);

        DirectedConversationEngine<TestState> engine = new DirectedConversationEngine<>(intentDetector, index, initialState, (map) -> {
            return new TestState(map);
        });

        assertEquals(1, engine.getState().getCurrentNodeId().intValue());

        CompletableFuture<DirectedConversationEngine.MessageHandlingResult> result = engine.handleIncomingMessage("left");

        DirectedConversationEngine.MessageHandlingResult r = result.join();

        assertEquals(true, r.ok);

        assertEquals(2, engine.getState().getCurrentNodeId().intValue());

    }

    @Test
    public void testHandleMessageMiss() {

        Map<String, String> intentMap = new HashMap();
        intentMap.put("leftIntent", "left");
        intentMap.put("rightIntent", "right");
        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);

        DirectedConversationEngine<TestState> engine = new DirectedConversationEngine<>(intentDetector, index, initialState, (map) -> {
            return new TestState(map);
        });

        assertEquals(1, engine.getState().getCurrentNodeId().intValue());

        CompletableFuture<DirectedConversationEngine.MessageHandlingResult> result = engine.handleIncomingMessage("up");

        DirectedConversationEngine.MessageHandlingResult r = result.join();

        assertEquals(false, r.ok);
        assertEquals(ErrorCode.INTENT_UNDERSTANDING_FAILED, r.errorCode);

        assertEquals(1, engine.getState().getCurrentNodeId().intValue());
        assertEquals(1, engine.getState().getMisunderstoodCount().intValue());

        result = engine.handleIncomingMessage("down");

        r = result.join();

        assertEquals(false, r.ok);
        assertEquals(ErrorCode.INTENT_UNDERSTANDING_FAILED, r.errorCode);

        assertEquals(1, engine.getState().getCurrentNodeId().intValue());
        assertEquals(2, engine.getState().getMisunderstoodCount().intValue());

    }

    @Test
    public void testHandleIntentAction() {

        Map<String, String> intentMap = new HashMap();
        intentMap.put("leftIntent", "left");
        intentMap.put("rightIntent", "right");
        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);

        Reducer rightReducer = new Reducer() {

            @Override
            public Map reduce(Action action, Map currentState) {
                if (action.getType().equals("right_handled")) {
                    Map<String, Object> nextState = new HashMap(currentState);
                    nextState.put("right", true);
                    return nextState;
                } else {
                    return currentState;
                }
            }

        };
        Map<String, Reducer> reducerMap = new HashMap();
        reducerMap.put("custom", rightReducer);

        DirectedConversationEngine<TestState> engine = new DirectedConversationEngine<>(intentDetector, index, initialState, (map) -> {
            return new TestState(map);
        });

        assertEquals(1, engine.getState().getCurrentNodeId().intValue());
        assertEquals(false, engine.getState().isRight());

        CompletableFuture<DirectedConversationEngine.MessageHandlingResult> result = engine.handleIncomingMessage("right");
        try {
            DirectedConversationEngine.MessageHandlingResult r = result.join();
            assertEquals(true, r.ok);
        } catch (CompletionException ex) {
            fail(ex.getMessage());
        }

        assertEquals(3, engine.getState().getCurrentNodeId().intValue());
        assertEquals(false, engine.getState().isRight());
    }

}
