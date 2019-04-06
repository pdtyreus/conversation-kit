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

import com.conversationkit.builder.JsonEdgeBuilder;
import com.conversationkit.builder.JsonGraphBuilder;
import com.conversationkit.builder.JsonNodeBuilder;
import com.conversationkit.impl.DirectedConversationEngine.MessageHandlingResult;
import com.conversationkit.impl.edge.ConversationEdge;
import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationNodeIndex;
import com.conversationkit.model.IConversationState;
import com.conversationkit.nlp.RegexIntentDetector;
import com.conversationkit.redux.Action;
import com.conversationkit.redux.Redux;
import com.conversationkit.redux.Store;
import com.conversationkit.redux.impl.CompletableFutureMiddleware;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author pdtyreus
 */
public class ConversationGraphTest {

    private static final Logger logger = Logger.getLogger(ConversationGraphTest.class.getName());

    public static class TestState extends MapBackedConversationState {

        public TestState(Map source) {
            super(source, DirectedConversationEngine.CONVERSATION_STATE_KEY);
        }

    }

    @Test
    public void testDirectedConversation() throws IOException {

        logger.info("** Initializing Templated Regex / JavaScript Conversation for testing");

        BiFunction<IConversationIntent, Store, Boolean> answerInvalidator = (intent, store) -> {

            final String answer = (String) intent.getSlots().get("0");
            PayloadAction<String> action = PayloadAction.build("SET_ANSWER", Optional.of(answer));

            if ("6".equals(answer) || "six".equalsIgnoreCase(answer)) {
                store.dispatch(action);
                return true;
            }

            return false;
        };

        BiFunction<IConversationIntent, Store, Boolean> answerValidator = (intent, store) -> {

            final String answer = (String) intent.getSlots().get("0");
            PayloadAction<String> action = PayloadAction.build("SET_ANSWER", Optional.of(answer));

            store.dispatch(action);

            return true;
        };

        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/directed_conversation.json"));

        JsonEdgeBuilder<ConversationEdge, DialogTreeNode> edgeBuilder = (String intentId, JsonObject metadata, DialogTreeNode target) -> {
            if (target.getId() == 4) {
                return new ConversationEdge(target, intentId, answerInvalidator);
            } else if (target.getId() == 5) {
                return new ConversationEdge(target, intentId, answerValidator);
            } else {
                return new ConversationEdge(target, intentId);
            }
        };

        JsonNodeBuilder<DialogTreeNode> nodeBuilder = (Integer id, String type, JsonObject metadata) -> {

            List<String> messages = new ArrayList();
            if (metadata.get("message") != null) {
                if (metadata.get("message").isArray()) {
                    for (JsonValue node : metadata.get("message").asArray()) {
                        messages.add(node.asString());
                    }
                } else {
                    messages.add(metadata.get("message").asString());
                }
            } else {
                throw new IOException("No \"message\" metadata for node " + id);
            }

            //make the node into something
            DialogTreeNode conversationNode = new DialogTreeNode(id, messages);

            return conversationNode;
        };

        IConversationNodeIndex<DialogTreeNode> index = JsonGraphBuilder.readJsonGraph(reader, nodeBuilder, edgeBuilder);
        
        Map intentMap = new LinkedHashMap();
        intentMap.put("YES", RegexIntentDetector.YES);
        intentMap.put("NUMBER_ANSWER","([a-z]+|\\d)");
        
        
        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);

        HashMap initialConversationState = new HashMap();
        initialConversationState.put("nodeId", 1);

        HashMap initialCustomState = new HashMap();
        //initialCustomState.put("right", false);

        Map initialState = new HashMap();
        initialState.put(DirectedConversationEngine.CONVERSATION_STATE_KEY, initialConversationState);
        initialState.put("math", initialCustomState);

        DirectedConversationEngine<TestState, IConversationIntent> engine = new DirectedConversationEngine<>(
                intentDetector,
                index,
                initialState, (map) -> {
                    return new TestState(map);
                });

        logger.info("** Testing conversation");

        DialogTreeNode currentNode = index.getNodeAtIndex(engine.getState().getCurrentNodeId());
        StringBuilder convo = new StringBuilder();
        convo.append("\n");
        Formatter formatter = new Formatter(convo);
        for (String message : currentNode.getMessages()) {
            OutputUtil.formatInput(formatter, message);
        }

        try {
            MessageHandlingResult result = engine.handleIncomingMessage("five").get();

            assertEquals(true, result.ok);
            assertEquals(5, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeAtIndex(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                OutputUtil.formatInput(formatter, message);
            }
            
            result = engine.handleIncomingMessage("yes").get();

            assertEquals(true, result.ok);
            assertEquals(1, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeAtIndex(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                OutputUtil.formatInput(formatter, message);
            }
            
            assertEquals(true, result.ok);
            result = engine.handleIncomingMessage("six").get();

            assertEquals(4, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeAtIndex(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                OutputUtil.formatInput(formatter, message);
            }

        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }

        logger.info(convo.toString());

//        try {
//            StringBuilder convo = new StringBuilder();
//            Formatter formatter = new Formatter(convo);
//
//            String message = "hello";
//            OutputUtil.formatInput(formatter, message);
//            engine.handleIncomingMessage(store, store.getState(), message).get();
//
//            convo.append("\n");
//            IConversationNode node = index.getNodeAtIndex(ConversationReducer.selectCurrentNodeId(store.getState()));
//            OutputUtil.formatOutput(formatter, node.getValue());
//
//            message = "4";
//            OutputUtil.formatInput(formatter, message);
//            engine.handleIncomingMessage(store, store.getState(), message).get();
//            convo.append("\n");
//            node = index.getNodeAtIndex(ConversationReducer.selectCurrentNodeId(store.getState()));
//            OutputUtil.formatOutput(formatter, node.getValue());
//
//            assertEquals(5, ConversationReducer.selectCurrentNodeId(store.getState()).intValue());
//            message = "yup";
//            OutputUtil.formatInput(formatter, message);
//            logger.info(convo.toString());
//        } catch (ExecutionException | InterruptedException e) {
//            fail(e.getMessage());
//        }
    }
}
