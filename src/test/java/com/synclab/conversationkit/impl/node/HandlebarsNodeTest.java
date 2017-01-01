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
package com.synclab.conversationkit.impl.node;

import com.synclab.conversationkit.impl.TestCaseUserState;
import com.synclab.conversationkit.model.SnippetContentType;
import com.synclab.conversationkit.model.SnippetType;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author pdtyreus
 */
public class HandlebarsNodeTest extends TestCase {
    
    public HandlebarsNodeTest(String testName) {
        super(testName);
    }

    public void testRenderContent() {
        TestCaseUserState state = new TestCaseUserState();
        HandlebarsNode instance = new HandlebarsNode(1,SnippetType.STATEMENT,"rendered {{name}} {{number}} {{answer}}",SnippetContentType.TEXT);
        state.setName("Daniel");
        state.setNumber(5);
        state.set("answer", "content");
        String expResult = "rendered Daniel 5 content";
        String result = instance.renderContent(state);
        assertEquals(expResult, result);
    }

    public void testGetSuggestedResponses() {
        TestCaseUserState state = new TestCaseUserState();
        HandlebarsNode instance = new HandlebarsNode(1,SnippetType.STATEMENT,"content","one|two|{{number}}",SnippetContentType.TEXT);
        state.setName("Daniel");
        state.setNumber(5);
        state.set("answer", "content");
        Iterable<String> result = instance.getSuggestedResponses(state);
        List<String> responses = new ArrayList();
        for (String r : result) {
            responses.add(r);
        }
        assertEquals(3,responses.size());
        assertEquals("5",responses.get(2));
    }
    
}
