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
package com.conversationkit.impl.edge;

import com.conversationkit.impl.edge.RegexEdge;
import com.conversationkit.impl.MapBackedState;
import com.conversationkit.model.IConversationState;
import java.util.Optional;
import junit.framework.TestCase;

/**
 *
 * @author pdtyreus
 */
public class RegexEdgeTest extends TestCase {

    public RegexEdgeTest(String testName) {
        super(testName);
    }

    public void testIsMatchForState() {
        MapBackedState state = new MapBackedState();
        RegexEdge<String, MapBackedState> instance = new RegexEdge("\\d+", null);

        Optional<String> response = Optional.of("word");

        assertEquals(false, instance.isMatchForState(response, state));
        response = Optional.of("5");
        assertEquals(true, instance.isMatchForState(response, state));

        instance = new RegexEdge("\\w+", null);
        response = Optional.of("word");
        assertEquals(true, instance.isMatchForState(response, state));
    }

}
