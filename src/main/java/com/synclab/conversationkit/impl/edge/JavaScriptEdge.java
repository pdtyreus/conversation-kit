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

import com.synclab.conversationkit.model.IConversationEdge;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.InvalidResponseException;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author pdtyreus
 */
public class JavaScriptEdge<S extends IConversationState> implements IConversationEdge<S> {

    private final IConversationNode<S> endNode;
    private final String isMatchForState;
    private final String onMatch;

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");

    private static final Logger logger = Logger.getLogger(JavaScriptEdge.class.getName());

    public JavaScriptEdge(String isMatchForState, String onMatch, IConversationNode<S> endNode) {
        this.endNode = endNode;
        this.isMatchForState = isMatchForState;
        this.onMatch = onMatch;
    }

    public JavaScriptEdge(String isMatchForState, IConversationNode<S> endNode) {
        this.endNode = endNode;
        this.isMatchForState = isMatchForState;
        this.onMatch = "return state;";
    }

    public IConversationNode<S> getEndNode() {
        return endNode;
    }

    public boolean isMatchForState(S state) {
        Invocable inv = (Invocable) engine;
        String template = "function isMatchForState(state) {";
        template += isMatchForState;
        template += "};";
        try {
            engine.eval(template);
            return (Boolean) inv.invokeFunction("isMatchForState", state);
        } catch (Exception e) {
            logger.severe(e.toString());
            return false;
        }
    }

    public S onMatch(S state) throws InvalidResponseException {
        Invocable inv = (Invocable) engine;
        String template = "function onMatch(state) {";
        template += onMatch;
        template += "};";
        try {
            engine.eval(template);
            return (S) inv.invokeFunction("onMatch", state);
        } catch (Exception e) {
            throw new InvalidResponseException(e.getMessage());
        }
    }

}
