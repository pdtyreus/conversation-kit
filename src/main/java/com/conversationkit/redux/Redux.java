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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java implementation of the Redux top level exports.
 * 
 * @see <a href="https://redux.js.org/api/api-reference">https://redux.js.org/api/api-reference/</a>
 * @author pdtyreus
 */
public class Redux {

    private static final Logger logger = Logger.getLogger(Store.class.getName());
    /**
     * Creates a Redux {@link Store} that holds the complete state tree of your application.
     * <p>
     * This follows the basic API specified by the original Javascript version except that it
     * also takes a constructor function as required by {@link Store}. The state builder
     * is a {@link Function} that takes a {@link Map} as a parameter and returns
     * an instance of the state as the generic type S.
     * <p>
     * There should only be a single store in your application.
     * @see <a href="https://redux.js.org/api/createstore">https://redux.js.org/api/createstore</a>
     * @param <S> generic type of the store created
     * @param reducer final Reducer function
     * @param state initial state for the application
     * @param stateBuilder function to build a typed state from a map
     * @param middlewares optional middleware
     * @return an instance of a Redux Store.
     */
    public static <S> Store<S> createStore(Reducer reducer, Map state, Function<Map,S> stateBuilder, Middleware<S>... middlewares) {
        return new Store<>(reducer, state, stateBuilder, middlewares);
    }

    /**
     * Combines multiple reducers into a single reducer.
     * <p>
     * As your application grows more complex, you'll want to split your reducing 
     * function into separate functions, each managing independent parts of the state. 
     * The combineReducers helper function turns a map of reducers into a single 
     * reducing function you can pass to createStore. The key of the reducers map
     * dictates the slice of state the reducer will manage.
     * @see <a href="https://redux.js.org/api/combinereducers">https://redux.js.org/api/combinereducers</a>
     * @param reducers
     * @return a combined Reducer
     */
    public static Reducer combineReducers(Map<String, Reducer> reducers) {
        return (Action action, Map currentState) -> {
            boolean isDirty = false;
            Map nextState = new HashMap();
            for (Object key : currentState.keySet()) {
                Map<String,Object> nestedState = (Map<String,Object>)currentState.get(key);
                if (reducers.containsKey((String)key)) {
                    logger.log(Level.FINE, "[REDUX] delegating to reducer {0}", key);
                    Map<String,Object> nextNestedState = reducers.get(key).reduce(action, nestedState);
                    if (!nextNestedState.equals(nestedState)) {
                        isDirty = true;
                    }
                    nextState.put(key, nextNestedState);
                } else {
                    nextState.put(key, nestedState);
                }
            }
            if (isDirty) {
                return nextState;
            } else {
                return currentState;
            }
        };
    }
}
