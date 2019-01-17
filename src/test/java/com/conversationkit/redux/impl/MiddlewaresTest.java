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
package com.conversationkit.redux.impl;

import com.conversationkit.redux.Reducer;
import com.conversationkit.redux.Redux;
import com.conversationkit.redux.Store;
import com.conversationkit.redux.StringAction;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author pdtyreus
 */
public class MiddlewaresTest extends TestCase {

    static final String INCREMENT = "INCREMENT";
    static final String DECREMENT = "DECREMENT";

    final Reducer<StringAction, Integer> reducer = (StringAction action, Integer currentState) -> {
        switch (action.getType()) {
            case INCREMENT:
                return new Integer(++currentState);
            case DECREMENT:
                return new Integer(--currentState);
            default:
                return currentState;
        }
    };

    public void testCompletableFutureCounter() {
        Integer state = 0;
        Store<StringAction, Integer> store = Redux.createStore(reducer, state, new CompletableFutureMiddleware());

        store.dispatch(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
            return new StringAction(INCREMENT);
        }));
        assertEquals(1, store.getState().intValue());

        store.dispatch(new StringAction(INCREMENT));
        assertEquals(2, store.getState().intValue());

        store.dispatch(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
            return new StringAction(DECREMENT);
        }));
        assertEquals(1, store.getState().intValue());
    }

    public void testThunkCounter() {
        Integer state = 0;
        Store<StringAction, Integer> store = Redux.createStore(reducer, state, new ThunkMiddleware());

        Consumer<Store> c = s -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
            s.dispatch(new StringAction(INCREMENT));
        };
        store.dispatch(c);
        assertEquals(1, store.getState().intValue());

        store.dispatch(new StringAction(INCREMENT));
        assertEquals(2, store.getState().intValue());

        store.dispatch((Consumer<Store>)s -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
            s.dispatch(new StringAction(DECREMENT));
        });
        assertEquals(1, store.getState().intValue());
    }

}
