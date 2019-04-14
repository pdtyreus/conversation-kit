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
import com.conversationkit.impl.action.ActionType;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * @author tyreus
 */
public class DialogTreeTest {

    private static final Logger logger = Logger.getLogger(DialogTreeTest.class.getName());

    public static class TestState extends MapBackedConversationState<TestState> {

        public TestState(Map source) {
            super(source, DirectedConversationEngine.CONVERSATION_STATE_KEY);
        }

        private Map getDialogMap() {

            return (Map) source.get("dialog");

        }

        public String getMood() {
            return (String) getDialogMap().get("mood");
        }

        @Override
        public TestState apply(Map t) {
            return new TestState(t);
        }
        


    }

    /**
     * Dispatches an action if the value in the specified slot matches one of
     * the provided strings.
     */
    private static class DialogValidator implements BiFunction<IConversationIntent, Store, Boolean> {

        private final String actionType;
        private final int slot;
        private final Set<String> matches;

        public DialogValidator(String actionType, int slot, Set<String> matches) {
            this.actionType = actionType;
            this.slot = slot;
            this.matches = matches;
        }

        public DialogValidator(String actionType, int slot, String match) {
            this.actionType = actionType;
            this.slot = slot;
            this.matches = new HashSet();
            this.matches.add(match);
        }

        @Override
        public Boolean apply(IConversationIntent intent, Store store) {
            final String answer = (String) intent.getSlots().get(slot + "");
            PayloadAction<String> action = PayloadAction.build(actionType, Optional.of(answer));

            if (matches.contains(answer)) {
                store.dispatch(action);
                return true;
            }

            return false;
        }

    }

    @Test
    public void testTemplatedDialogTree() throws IOException {

        logger.info("** Initializing Templated DialogTree for testing");

        //In practice you would use a real template engine here, but we are making a simple one to minimize dependencies
//        JsonDialogTreeBuilder builder = new JsonDialogTreeBuilder();
//        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/templated_dialog_tree.json"));
//        DirectedConversationEngine<TestCaseUserState> tree = builder.readDialogTree(reader);
//
//        logger.info("** Testing conversation");
//        
//        TestCaseUserState state = new TestCaseUserState();
//        state.setCurrentNodeId(1);
//        state.setName("Daniel");
//        state.setNumber(3);
//
//        Iterable<IConversationSnippet> nodes = tree.startConversationFromState(state);
//        StringBuilder convo = new StringBuilder();
//        Formatter formatter = new Formatter(convo);
//
//        convo.append("\n");
//        for (IConversationSnippet node : nodes) {
//            OutputUtil.formatSnippet(formatter, node, state);
//        }
//
//        String response = "4";
//        OutputUtil.formatResponse(formatter, response);
//        try {
//            tree.updateStateWithResponse(state, response);
//        } catch (UnmatchedResponseException | UnexpectedResponseException e) {
//            fail(e.toString());
//        } 
//        nodes = tree.startConversationFromState(state);
//        for (IConversationSnippet node : nodes) {
//            OutputUtil.formatSnippet(formatter, node, state);
//        }
//
//        assertEquals(4, state.getCurrentNodeId());
//        
//        assertEquals(response, state.get("numFingers").toString());
//
//        logger.info(convo.toString());
    }

    @Test
    public void testBasicDialogTree() throws IOException {

        logger.info("** Initializing Basic DialogTree for testing");

        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/dialog_tree.json"));

        JsonEdgeBuilder<ConversationEdge> edgeBuilder = (String intentId, JsonObject metadata, Integer target) -> {

            if ((metadata != null) && (metadata.get("validator") != null)) {
                JsonObject validator = metadata.get("validator").asObject();
                if (validator != null) {
                    DialogValidator dialogValidator = new DialogValidator(
                            validator.getString("actionType", ""),
                            validator.getInt("slot", 0),
                            validator.getString("matches", ""));

                    return new ConversationEdge(target, intentId, dialogValidator);
                }
            }

            return new ConversationEdge(target, intentId);

        };

        ConversationNodeRepository<DialogTreeNode> index = JsonGraphBuilder.readJsonGraph(reader, new DialogTreeNodeBuilder(), edgeBuilder);

        Map intentMap = new LinkedHashMap();
        intentMap.put("YES", RegexIntentDetector.YES);
        intentMap.put("NO", RegexIntentDetector.NO);

        intentMap.put("ANY", "\\w+");

        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);

        HashMap initialConversationState = new HashMap();
        initialConversationState.put("nodeId", 1);

        HashMap initialCustomState = new HashMap();

        Map initialState = new HashMap();
        initialState.put(DirectedConversationEngine.CONVERSATION_STATE_KEY, initialConversationState);
        initialState.put("dialog", initialCustomState);

        Reducer dialogReducer = (Action action, Map currentState) -> {
            switch (action.getType()) {
                case "SET_MOOD":
                    Map nextState = new HashMap();
                    nextState.putAll(currentState);
                    nextState.put("mood", ((PayloadAction<String>) action).getPayload().get());
                    return nextState;
                default:
                    return currentState;
            }
        };

        Map<String, Reducer> reducers = new HashMap();
        reducers.put("dialog", dialogReducer);

        DirectedConversationEngine<TestState, IConversationIntent> engine = new DirectedConversationEngine<>(
                intentDetector,
                index,
                new TestState(initialState),
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
            OutputUtil.formatInput(formatter, "great");
            MessageHandlingResult result = engine.handleIncomingMessage("great").get();

            assertEquals(true, result.ok);
            assertEquals(3, engine.getState().getCurrentNodeId().intValue());
            assertEquals("great", engine.getState().getMood());

            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                OutputUtil.formatOutput(formatter, message);
            }

        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }

        //reset the convo
        engine.dispatch(new ConversationAction(ActionType.SET_NODE_ID, 1));

        convo.append("\n");
        for (String message : currentNode.getMessages()) {
            OutputUtil.formatOutput(formatter, message);
        }

        try {
            OutputUtil.formatInput(formatter, "bad");
            MessageHandlingResult result = engine.handleIncomingMessage("bad").get();

            assertEquals(true, result.ok);
            assertEquals(4, engine.getState().getCurrentNodeId().intValue());
            assertEquals("bad", engine.getState().getMood());

            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                OutputUtil.formatOutput(formatter, message);
            }

            String snark = "Yeah, you could work for a change.";

            OutputUtil.formatInput(formatter, snark);
            result = engine.handleIncomingMessage(snark).get();

            assertEquals(true, result.ok);
            assertEquals(6, engine.getState().getCurrentNodeId().intValue());

            currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
            for (String message : currentNode.getMessages()) {
                OutputUtil.formatOutput(formatter, message);
            }
        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }

        logger.info(convo.toString());

    }

}
