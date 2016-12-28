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
package com.synclab.conversationkit.impl.edge;

import com.synclab.conversationkit.impl.MapBackedState;
import com.synclab.conversationkit.model.IConversationState;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author pdtyreus
 */
public class JavaScriptEdgeTest extends TestCase {

    public JavaScriptEdgeTest(String testName) {
        super(testName);
    }

    public void testIsMatchForState() {
        IConversationState state = new MapBackedState();
        JavaScriptEdge instance = new JavaScriptEdge("return true;", null);
        state.setCurrentResponse("word");
        assertEquals(true, instance.isMatchForState(state));
        
        instance = new JavaScriptEdge("return (state.currentResponse == 'word');", null);

        assertEquals(true, instance.isMatchForState(state));
        
        state.setCurrentResponse("number");
        
        assertEquals(false, instance.isMatchForState(state));
        
        instance = new JavaScriptEdge("return invalidFunc();", null);
        
        assertEquals(false, instance.isMatchForState(state));
    }

    public void testOnMatch() throws Exception {
        IConversationState state = new MapBackedState();
        JavaScriptEdge instance = new JavaScriptEdge("return true;", "return state;", null);
        state.setCurrentResponse("word");
        state.set("js",false);
        assertEquals(true, instance.isMatchForState(state));
        assertEquals(null, state.get("java"));
        state = instance.onMatch(state);
        assertEquals(false, state.get("js"));
        
        instance = new JavaScriptEdge("return true;", "state.js = true; return state;", null);
        state = instance.onMatch(state);
        assertEquals(true, state.get("js"));
    }

}
