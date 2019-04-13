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

import com.conversationkit.builder.DialogTreeNodeBuilder;
import com.conversationkit.builder.JsonEdgeBuilder;
import com.conversationkit.builder.JsonGraphBuilder;
import com.conversationkit.impl.ConversationGraphTest.TestState.TestStateBuilder;
import com.conversationkit.impl.edge.ConversationEdge;
import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.model.IConversationEngine.MessageHandlingResult;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.ConversationNodeRepository;
import com.conversationkit.nlp.RegexIntentDetector;
import com.conversationkit.redux.Action;
import com.conversationkit.redux.Reducer;
import com.conversationkit.redux.Store;
import com.eclipsesource.json.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import org.junit.Test;

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

        private Map getMathMap() {

            return (Map) source.get("math");

        }

        public String getAnswer() {
            return (String) getMathMap().get("answer");
        }
        
         public static class TestStateBuilder extends ConversationStateBuilder<TestState> {

            public TestStateBuilder(Map initialState) {
                super(initialState);
            }

            @Override
            public TestState buildFromState(Map state) {
                return new TestState(state);
            }
             
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

        JsonEdgeBuilder<ConversationEdge> edgeBuilder = (String intentId, JsonObject metadata, Integer target) -> {
            if (target == 4) {
                return new ConversationEdge(target, intentId, answerInvalidator);
            } else if (target == 5) {
                return new ConversationEdge(target, intentId, answerValidator);
            } else {
                return new ConversationEdge(target, intentId);
            }
        };

        ConversationNodeRepository<DialogTreeNode> index = JsonGraphBuilder.readJsonGraph(reader, new DialogTreeNodeBuilder(), edgeBuilder);

        Map intentMap = new LinkedHashMap();
        intentMap.put("YES", RegexIntentDetector.YES);
        intentMap.put("NUMBER_ANSWER", "(one)|(two)|(three)|(four)|(five)|(six)|(\\d)");

        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);

        HashMap initialConversationState = new HashMap();
        initialConversationState.put("nodeId", 1);

        HashMap initialCustomState = new HashMap();
        //initialCustomState.put("right", false);

        Map initialState = new HashMap();
        initialState.put(DirectedConversationEngine.CONVERSATION_STATE_KEY, initialConversationState);
        initialState.put("math", initialCustomState);

        Reducer mathReducer = (Action action, Map currentState) -> {
            switch (action.getType()) {
                case "SET_ANSWER":
                    Map nextState = new HashMap();
                    nextState.putAll(currentState);
                    nextState.put("answer", ((PayloadAction<String>) action).getPayload().get());
                    return nextState;
                default:
                    return currentState;
            }
        };

        Map<String, Reducer> reducers = new HashMap();
        reducers.put("math", mathReducer);

        DirectedConversationEngine<TestState, IConversationIntent> engine = new DirectedConversationEngine<>(
                intentDetector,
                index,
                new TestStateBuilder(initialState),
                reducers);

        logger.info("** Testing conversation");

        DialogTreeNode currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
        StringBuilder convo = new StringBuilder();
        convo.append("\n");
        Formatter formatter = new Formatter(convo);
        for (String message : currentNode.getMessages()) {
            OutputUtil.formatOutput(formatter, message);
        }

        try {
            OutputUtil.formatInput(formatter, "five");
            MessageHandlingResult result = engine.handleIncomingMessage("five").get();

            assertEquals(true, result.ok);
            assertEquals(5, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                message = message.replace("{{answer}}", engine.getState().getAnswer());
                OutputUtil.formatOutput(formatter, message);
            }

            OutputUtil.formatInput(formatter, "yes");
            result = engine.handleIncomingMessage("yes").get();

            assertEquals(true, result.ok);
            assertEquals(1, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                message = message.replace("{{answer}}", engine.getState().getAnswer());
                OutputUtil.formatOutput(formatter, message);
            }

            OutputUtil.formatInput(formatter, "6");
            result = engine.handleIncomingMessage("6").get();

            assertEquals(true, result.ok);
            assertEquals(4, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                message = message.replace("{{answer}}", engine.getState().getAnswer());
                OutputUtil.formatOutput(formatter, message);
            }

        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }

        logger.info(convo.toString());

    }
}
