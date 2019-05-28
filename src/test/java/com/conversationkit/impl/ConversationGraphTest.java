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

import com.conversationkit.builder.ConversationNodeBuilder;
import com.conversationkit.builder.JsonEdgeBuilder;
import com.conversationkit.builder.JsonGraphBuilder;
import com.conversationkit.impl.edge.ConversationEdge;
import com.conversationkit.impl.node.ConversationNode;
import com.conversationkit.model.IConversationEngine.MessageHandlingResult;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.ConversationNodeRepository;
import com.conversationkit.nlp.RegexIntentDetector;
import com.conversationkit.redux.Action;
import com.conversationkit.redux.Reducer;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    public static class TestState extends MapBackedConversationState<TestState> {

        public TestState(Map source) {
            super(source, DirectedConversationEngine.CONVERSATION_STATE_KEY);
        }

        private Map getMathMap() {

            return (Map) source.get("math");

        }

        public String getAnswer() {
            return (String) getMathMap().get("answer");
        }

        @Override
        public TestState apply(Map t) {
            return new TestState(t);
        }

    }

    private static class StringValidator implements BiFunction<IConversationIntent, TestState, Boolean> {

        private final int slot;
        private final Set<String> matches;

        public StringValidator(int slot, Iterable<String> matches) {
            this.slot = slot;
            this.matches = new HashSet();
            for (String match : matches) {
                this.matches.add(match);
            }
        }

        public StringValidator(int slot, String match) {
            this.slot = slot;
            this.matches = new HashSet();
            this.matches.add(match);
        }

        @Override
        public Boolean apply(IConversationIntent intent, TestState state) {
            final String answer = (String) intent.getSlots().get(slot + "");

            if (matches.contains(answer)) {
                return true;
            }

            return false;
        }

    }

    @Test
    public void testDirectedConversation() throws IOException {

        logger.info("** Initializing Templated Regex / JavaScript Conversation for testing");

        BiFunction<IConversationIntent, TestState, Boolean> answerInvalidator = (intent, state) -> {

            final String answer = (String) intent.getSlots().get("0");

            if ("6".equals(answer) || "six".equalsIgnoreCase(answer)) {
                return true;
            }

            return false;
        };

        BiFunction<IConversationIntent, TestState, Boolean> answerValidator = (intent, state) -> {

            return true;
        };

        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/directed_conversation.json"));

        JsonEdgeBuilder<ConversationEdge> edgeBuilder = (String intentId, String label, JsonObject metadata, Integer target) -> {

            if ((metadata != null) && (metadata.get("validator") != null) && (metadata.get("effect") != null)) {
                JsonObject validator = metadata.get("validator").asObject();

                BiFunction<IConversationIntent, TestState, Action> answerSideEffect = (intent, state) -> {

                    final String answer = (String) intent.getSlots().get("0");
                    PayloadAction<String> action = PayloadAction.build("SET_ANSWER", Optional.of(answer));

                    return action;
                };

                BiFunction<IConversationIntent, TestState, Boolean> v;
                String type = validator.getString("type", "unknown");
                if (type.equals("string")) {
                    List<String> matches = new ArrayList();
                    for (JsonValue val : validator.get("matches").asArray()) {
                        matches.add(val.asString());
                    }
                    v = new StringValidator(
                            validator.getInt("slot", 0),
                            matches);
                } else {
                    //always return true
                    v = (i, s) -> {
                        return true;
                    };
                }

                return new ConversationEdge(target, intentId, v, answerSideEffect);

            } else {
                return new ConversationEdge(target, intentId);
            }

        };

        ConversationNodeRepository<ConversationNode> index = JsonGraphBuilder.readJsonGraph(reader, new ConversationNodeBuilder(), edgeBuilder);

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
                new TestState(initialState),
                reducers);

        logger.info("** Testing conversation");

        ConversationNode currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
        StringBuilder convo = new StringBuilder();
        convo.append("\n");
        Formatter formatter = new Formatter(convo);
        for (JsonValue message : currentNode.getMetadata().get("message").asArray()) {
            OutputUtil.formatOutput(formatter, message.asString());
        }

        try {
            OutputUtil.formatInput(formatter, "five");
            MessageHandlingResult result = engine.handleIncomingMessage("five").get();

            assertEquals(true, result.ok);
            assertEquals(5, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (JsonValue message : currentNode.getMetadata().get("message").asArray()) {
                String m = message.asString().replace("{{answer}}", engine.getState().getAnswer());
                OutputUtil.formatOutput(formatter, m);
            }

            OutputUtil.formatInput(formatter, "yes");
            result = engine.handleIncomingMessage("yes").get();

            assertEquals(true, result.ok);
            assertEquals(1, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (JsonValue message : currentNode.getMetadata().get("message").asArray()) {
                String m = message.asString().replace("{{answer}}", engine.getState().getAnswer());
                OutputUtil.formatOutput(formatter, m);
            }

            OutputUtil.formatInput(formatter, "6");
            result = engine.handleIncomingMessage("6").get();

            assertEquals(true, result.ok);
            assertEquals(4, engine.getState().getCurrentNodeId().intValue());
            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (JsonValue message : currentNode.getMetadata().get("message").asArray()) {
                String m = message.asString().replace("{{answer}}", engine.getState().getAnswer());
                OutputUtil.formatOutput(formatter, m);
            }

        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }

        logger.info(convo.toString());

    }
}
