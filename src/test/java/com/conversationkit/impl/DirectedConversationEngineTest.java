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

import com.conversationkit.impl.edge.ConversationEdge;
import com.conversationkit.impl.node.ConversationNode;
import com.conversationkit.impl.node.ResponseSuggestingNode;
import com.conversationkit.nlp.RegexIntentDetector;
import com.conversationkit.redux.Action;
import com.conversationkit.redux.Dispatcher;
import com.conversationkit.redux.Redux;
import com.conversationkit.redux.Store;
import com.conversationkit.redux.impl.CompletableFutureMiddleware;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import junit.framework.TestCase;

/**
 *
 * @author tyreus
 */
public class DirectedConversationEngineTest extends TestCase {
    
    public DirectedConversationEngineTest(String testName) {
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

    /**
     * Test of handleIncomingMessage method, of class DirectedConversationEngine.
     */
    public void testHandleIncomingMessage() {
        
        ConversationNode top = new ResponseSuggestingNode(1,"top");
        ConversationNode left = new ResponseSuggestingNode(2,"left");
        ConversationNode right = new ResponseSuggestingNode(3,"right");
        ConversationEdge leftEdge = new ConversationEdge(left,"leftIntent");
        ConversationEdge rightEdge = new ConversationEdge(right,"rightIntent");
        top.addEdge(leftEdge);
        top.addEdge(rightEdge);
        
        MapBackedNodeIndex index = new MapBackedNodeIndex();
        index.addNodeToIndex(1, top);
        index.addNodeToIndex(2, left);
        index.addNodeToIndex(3, right);
        
        Map<String,String> intentMap = new HashMap();
        intentMap.put("leftIntent", "left");
        intentMap.put("rightIntent", "right");
        RegexIntentDetector intentDetector = new RegexIntentDetector(intentMap);
        
        DirectedConversationEngine engine = new DirectedConversationEngine(intentDetector,index);
        
        HashMap initialState = new HashMap();
        initialState.put("nodeId", 1);
        
        Store store = Redux.createStore(new ConversationReducer(), initialState, new CompletableFutureMiddleware());
        
        assertEquals(1, ConversationReducer.selectCurrentNodeId(store.getState()).intValue());
        
        engine.handleIncomingMessage(store, store.getState(), "left");
        
        assertEquals(2, ConversationReducer.selectCurrentNodeId(store.getState()).intValue());
        
    }
    
}
