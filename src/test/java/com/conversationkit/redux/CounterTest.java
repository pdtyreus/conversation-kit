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
package com.conversationkit.redux;

import junit.framework.TestCase;
import static junit.framework.Assert.assertEquals;
/**
 *
 * @author tyreus
 */
public class CounterTest extends TestCase {

    static final String INCREMENT = "INCREMENT";
    static final String DECREMENT = "DECREMENT";

    public void testCounter() {
        Reducer<String, Integer> reducer = (String action, Integer currentState) -> {
            switch (action) {
                case INCREMENT:
                    return new Integer(++currentState);
                case DECREMENT:
                    return new Integer(--currentState);
                default:
                    return currentState;
            }
        };
        Integer state = 0;

        final Middleware<String, Integer> systemOutMiddleware = (store, action, next) -> {
            System.out.println("System.out middleware got " + action + " with state " + store.getState());
            next.dispatch(store, action, next);
        };

        Store<String, Integer> store = Redux.createStore(reducer, state, systemOutMiddleware);
        
        store.dispatch(INCREMENT);
        assertEquals(1,store.getState().intValue());
        
        store.dispatch(INCREMENT);
        assertEquals(2,store.getState().intValue());
        
        store.dispatch(DECREMENT);
        assertEquals(1,store.getState().intValue());
    }

}
