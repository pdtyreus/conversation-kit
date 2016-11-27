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

import com.synclab.conversationkit.model.IConversationSnippetRenderer;
import com.synclab.conversationkit.model.IConversationState;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author tyreus
 */
public class DialogTreeTest extends TestCase {
    
    private static final Logger logger = Logger.getLogger(DialogTreeTest.class.getName());
    
    private DialogTree tree;
    
    public DialogTreeTest(String testName) {
        super(testName);
        logger.info("** Initializing DialogTree for testing");
        JsonDialogTreeBuilder builder = new JsonDialogTreeBuilder();
        Reader reader = new InputStreamReader(DialogTreeTest.class.getResourceAsStream("/dialog_tree.json"));
        try {
            tree = builder.fromJson(reader);
        } catch (IOException ex) {
            logger.log(Level.SEVERE,"Unable to load tree json",ex);
        }
    }

    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConversation() {
        logger.info("** Testing conversation");
        
        DialogTreeState state = new DialogTreeState();
        state.setCurrentNodeId(1);
        
        List<DialogTreeNode> nodes = tree.startConversationFromState(state);
        StringBuilder convo = new StringBuilder();
        Formatter formatter = new Formatter(convo);

        convo.append("\n");
        for (DialogTreeNode node : nodes) {
            formatter.format("  > %-50s <\n", node.renderContent(state));
        }
        
        assertEquals(2,state.getCurrentNodeId());
        
        String response = "great";
        formatter.format("  > %50s <\n", response);
        nodes = tree.processResponse(response, state);
        
        for (DialogTreeNode node : nodes) {
            formatter.format("  > %-50s <\n", node.renderContent(state));
        }
        
        assertEquals(3,state.getCurrentNodeId());
        
        logger.info(convo.toString());
        
        //restart the convo
        state.setCurrentNodeId(1);
        nodes = tree.startConversationFromState(state);
        
        convo = new StringBuilder();
        formatter = new Formatter(convo);
        
        convo.append("\n");
        for (DialogTreeNode node : nodes) {
            formatter.format("  > %-50s <\n", node.renderContent(state));
        }
        
        assertEquals(2,state.getCurrentNodeId());
        
        response = "bad";
        formatter.format("  > %50s <\n", response);
        nodes = tree.processResponse(response, state);
        
        for (DialogTreeNode node : nodes) {
            formatter.format("  > %-50s <\n", node.renderContent(state));
        }
        
        assertEquals(5,state.getCurrentNodeId());
        
        response = "No, I actually feel fine.";
        formatter.format("  > %50s <\n", response);
        nodes = tree.processResponse(response, state);
        
        for (DialogTreeNode node : nodes) {
            formatter.format("  > %-50s <\n", node.renderContent(state));
        }
        
        assertEquals(3,state.getCurrentNodeId());
        
        logger.info(convo.toString());
    }

}
