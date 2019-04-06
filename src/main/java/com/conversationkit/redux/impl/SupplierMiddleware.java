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

import com.conversationkit.redux.Middleware;
import com.conversationkit.redux.Store;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class SupplierMiddleware implements Middleware {

    private static final Logger logger = Logger.getLogger(SupplierMiddleware.class.getName());
    
    private final ExecutorService executorService;

    public SupplierMiddleware(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void dispatch(Store store, Object action, Middleware next) {
        if (action instanceof Supplier) {
            logger.log(Level.FINE, "middleware handling future action {0}", action.toString());
            Supplier s = (Supplier) action;
            try {
                Object a = CompletableFuture.supplyAsync(s, executorService).get();
                next.dispatch(store, a, next);
            } catch (Exception ex) {
                throw new RuntimeException("Middleware received an unhandled exception. Catch exceptions in your lambda expression.", ex);
            }
        } else {
            logger.log(Level.FINE, "middleware ignoring action {0}", action.toString());
            next.dispatch(store, action, next);
        }
    }
}
