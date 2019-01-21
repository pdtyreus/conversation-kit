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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tyreus
 */
public class Redux {

    public static <A extends Action> Store<A> createStore(Reducer<A> reducer, Map<String,Object> state, Middleware<A>... middlewares) {
        return new Store(reducer, state, middlewares);
    }

    public static <A extends Action> Reducer<A> combineReducers(Map<String, Reducer<A>> reducers) {
        return new Reducer<A>() {

            @Override
            public Map<String,Object> reduce(A action, Map<String,Object> currentState) {
                boolean isDirty = false;
                Map<String,Object> nextState = new HashMap();
                for (String key : currentState.keySet()) {
                    if (reducers.containsKey(key)) {
                        Map<String,Object> nestedState = (Map<String,Object>)currentState.get(key);
                        Map<String,Object> nextNestedState = reducers.get(key).reduce(action, currentState);
                        if (!nextNestedState.equals(nestedState)) {
                            isDirty = true;
                        }
                        nextState.put(key, nextNestedState);
                    }
                }
                if (isDirty) {
                    return nextState;
                } else {
                    return currentState;
                }
            }

        };
    }
}
