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
package com.synclab.conversationkit.impl;

import com.synclab.conversationkit.model.IConversationSnippet;
import com.synclab.conversationkit.model.IConversationSnippetRenderer;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.SnippetType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author tyreus
 */
public class DialogTreeTest extends TestCase {

    private static final Logger logger = Logger.getLogger(DialogTreeTest.class.getName());

    public DialogTreeTest(String testName) {
        super(testName);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testTemplatedDialogTree() throws IOException {

        logger.info("** Initializing Templated DialogTree for testing");
        
        //In practice you would use a real template engine here, but we are making a simple one to minimize dependencies
        
        JsonDialogTreeBuilder builder = new JsonDialogTreeBuilder();
        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/templated_dialog_tree.json"));
        DialogTree<UserDialogTreeState> tree = builder.fromJson(reader, new IConversationSnippetRenderer<UserDialogTreeState>() {

            public String renderContentUsingState(String content, UserDialogTreeState state) {
                if (state == null) {
                    return content;
                }
                
                for (String key : state.keySet()) {
                    content = content.replace("{{"+key+"}}", state.get(key).toString());
                }
                
                return content;
            }
        
        },null);

        logger.info("** Testing conversation");
        
        UserDialogTreeState state = new UserDialogTreeState();
        state.setCurrentNodeId(1);
        state.setName("Daniel");
        state.setNumber(3);

        List<IConversationSnippet> nodes = tree.startConversationFromState(state);
        StringBuilder convo = new StringBuilder();
        Formatter formatter = new Formatter(convo);

        convo.append("\n");
        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        String response = "4";
        formatResponse(formatter, response);
        nodes = tree.processResponse(response, state);
        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        assertEquals(4, state.getCurrentNodeId());

        logger.info(convo.toString());
    }

    public void testBasicDialogTree() throws IOException {

        logger.info("** Initializing Basic DialogTree for testing");

        JsonDialogTreeBuilder builder = new JsonDialogTreeBuilder();
        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/dialog_tree.json"));
        DialogTree<UserDialogTreeState> tree = builder.fromJson(reader);

        logger.info("** Testing conversation");

        UserDialogTreeState state = new UserDialogTreeState();
        state.setCurrentNodeId(1);

        List<IConversationSnippet> nodes = tree.startConversationFromState(state);
        StringBuilder convo = new StringBuilder();
        Formatter formatter = new Formatter(convo);

        convo.append("\n");
        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        assertEquals(2, state.getCurrentNodeId());

        String response = "great";
        formatResponse(formatter, response);
        nodes = tree.processResponse(response, state);
        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        assertEquals(3, state.getCurrentNodeId());

        logger.info(convo.toString());

        //restart the convo
        state.setCurrentNodeId(1);
        nodes = tree.startConversationFromState(state);

        convo = new StringBuilder();
        formatter = new Formatter(convo);

        convo.append("\n");
        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        assertEquals(2, state.getCurrentNodeId());

        response = "bad";
        formatResponse(formatter, response);

        nodes = tree.processResponse(response, state);

        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        assertEquals(5, state.getCurrentNodeId());

        response = "No, I actually feel fine.";
        formatResponse(formatter, response);
        nodes = tree.processResponse(response, state);

        for (IConversationSnippet node : nodes) {
            formatSnippet(formatter, node, state);
        }

        assertEquals(3, state.getCurrentNodeId());

        logger.info(convo.toString());
    }

    private void formatSnippet(Formatter formatter, IConversationSnippet node, IConversationState state) {
        formatter.format("  > %-100s <\n", node.renderContent(state));
        if ((node.getType() == SnippetType.QUESTION) && (node.getSuggestedResponses() != null) && !node.getSuggestedResponses().isEmpty()) {
            formatter.format("  >   %-98s <\n", "[ " + String.join(" | ", node.getSuggestedResponses()) + " ]");
        }
    }

    private void formatResponse(Formatter formatter, String response) {
        formatter.format("  > %100s <\n", response);
    }

}
