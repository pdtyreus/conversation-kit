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
import com.conversationkit.impl.edge.ConversationEdge;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Formatter;
import java.util.HashMap;
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
            PayloadAction<String> action = PayloadAction.build("SET_ANSWER", Optional.of("6"));

            store.dispatch(action);

            return true;
        };

        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/directed_conversation.json"));

        JsonEdgeBuilder edgeBuilder = (String intentId, JsonObject metadata, IConversationNode target) -> {
            if (target.getId() == 4) {
                return new ConversationEdge(target, intentId, answerInvalidator);
            } else if (target.getId() == 5) {
                return new ConversationEdge(target, intentId, answerValidator);
            } else {
                return new ConversationEdge(target, intentId);
            }
        };

        IConversationNodeIndex index = JsonGraphBuilder.readJsonGraph(reader, JsonGraphBuilder.DEFAULT_NODE_BUILDER, edgeBuilder);
        RegexIntentDetector intentDetector = new RegexIntentDetector(new HashMap());

        HashMap initialConversationState = new HashMap();
        initialConversationState.put("nodeId", 1);

        HashMap initialCustomState = new HashMap();
        //initialCustomState.put("right", false);

        Map initialState = new HashMap();
        initialState.put(DirectedConversationEngine.CONVERSATION_STATE_KEY, initialConversationState);
        initialState.put("math", initialCustomState);

        DirectedConversationEngine<TestState, IConversationIntent> engine = new DirectedConversationEngine<>(intentDetector, index, initialState, (map) -> {
            return new TestState(map);
        });

        logger.info("** Testing conversation");

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
