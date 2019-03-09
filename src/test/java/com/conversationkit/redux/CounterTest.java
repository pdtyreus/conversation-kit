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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
/**
 *
 * @author tyreus
 */
public class CounterTest {

    static final String INCREMENT = "INCREMENT";
    static final String DECREMENT = "DECREMENT";

    final Reducer<StringAction> reducer = (StringAction action, Map<String,Object> currentState) -> {
        Integer counter = (Integer)currentState.get("counter");
        Map<String,Object> nextState = new HashMap();
        switch (action.getType()) {
            case INCREMENT:
                ++counter;
                nextState.put("counter", counter);
                return nextState;
            case DECREMENT:
                --counter;
                nextState.put("counter", counter);
                return nextState;
            default:
                return currentState;
        }
    };

    @Test
    public void testCounter() {

        HashMap<String,Object> state = new HashMap();
        state.put("counter", 0);

        final Middleware<StringAction> systemOutMiddleware = (store, action, next) -> {
            System.out.println("System.out middleware got " + action + " with state " + store.getState());
            next.dispatch(store, action, next);
        };

        Store<StringAction> store = Redux.createStore(reducer, state, systemOutMiddleware);

        store.dispatch(new StringAction(INCREMENT));
        assertEquals(1, store.getState().get("counter"));

        store.dispatch(new StringAction(INCREMENT));
        assertEquals(2, store.getState().get("counter"));

        store.dispatch(new StringAction(DECREMENT));
        assertEquals(1, store.getState().get("counter"));
    }

    @Test
    public void testAsyncCounter() {

        
        HashMap<String,Object> state = new HashMap();
        state.put("counter", 0);

        final Middleware<StringAction> asyncMiddleware = (store, action, next) -> {
            if (action instanceof Future) {
                Future f = (Future) action;
                try {
                    Object a = f.get();
                    System.out.println("async middleware waited and got " + a + " with state " + store.getState());
                    next.dispatch(store, a, next);
                } catch (Exception ex) {
                    fail(ex.getMessage());
                }
            } else {
                next.dispatch(store, action, next);
            }
        };

        Store<StringAction> store = Redux.createStore(reducer, state, asyncMiddleware);

        store.dispatch(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
            return new StringAction(INCREMENT);
        }));
        assertEquals(1, store.getState().get("counter"));

        store.dispatch(new StringAction(INCREMENT));
        assertEquals(2, store.getState().get("counter"));

        store.dispatch(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
            return new StringAction(DECREMENT);
        }));
        assertEquals(1, store.getState().get("counter"));
    }

    
    
}
