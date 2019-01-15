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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public final class Store<A, S> {

    private static final Logger logger = Logger.getLogger(Store.class.getName());
    
    private S currentState;

    private final Reducer<A, S> reducer;
    private ActionDispatcher<A> middleware;
    private final Map<UUID, Consumer<S>> consumers = new HashMap<>();

    @FunctionalInterface
    private interface ActionDispatcher<A> {

        public void dispatch(A action);
    }

    protected Store(Reducer<A, S> reducer, S state, Middleware<A, S>... middlewares) {
        this.reducer = reducer;
        this.currentState = state;

        List<Middleware<A, S>> allMiddlewares = new ArrayList();
        //native middleware, last middleware in chain

        allMiddlewares.add((store, action, next) -> {
            synchronized (Store.this) {
                currentState = store.reducer.reduce(action, currentState);
            }
            consumers.values().parallelStream().forEach(e -> e.accept(currentState));
        });
        for (Middleware<A, S> mw : middlewares) {
            allMiddlewares.add(mw);
        }
        
        logger.info(String.format("initializing redux store with %d middlewares.",(allMiddlewares.size()-1)));

        for (int i = allMiddlewares.size() - 1; i >= 0; i--) {
            final Middleware<A, S> mw = allMiddlewares.get(i);
            //this will be null for the native middleware only, which is last
            final Middleware<A, S> next = (i == allMiddlewares.size() - 1 ? null : allMiddlewares.get(i+1));

            this.middleware = (action) -> {
                mw.dispatch(Store.this, action, next);
            };

        }
    }

    public S dispatch(A action) {
        logger.fine(String.format("[REDUX] dispatching action: %s",action.toString()));

        this.middleware.dispatch(action);
        logger.finer(String.format("[REDUX] reduced state: %s",this.getState().toString()));
        return this.getState();
    }

    public S getState() {
        return this.currentState;
    }

    public UUID subscribe(Consumer<S> subscriber) {
        UUID uuid = UUID.randomUUID();
        this.consumers.put(uuid, subscriber);

        return uuid;
    }

    public void unsubscribe(UUID uuid) {

        this.consumers.remove(uuid);
    }
}
