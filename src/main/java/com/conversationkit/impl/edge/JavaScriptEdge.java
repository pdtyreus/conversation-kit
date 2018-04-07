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

import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationState;
import java.util.Optional;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * An IConversationEdge implementation that delegates matching logic to external
 * JavaScript code. Similar to a <code>RegexEdge</code>, this type of edge
 * allows users to store the logic for determining if an edge matches in a
 * location outside the source code. For instance, the string representation of
 * the JavaScript logic could be stored in a database or file representation of
 * the conversation graph.
 * <p>
 * The supplied JavaScript code modifies the behavior of the
 * {@link #isMatchForState(IConversationState)} and
 * {@link #onMatch(IConversationState)} methods. The string representation of
 * the JavaScript code is wrapped as follows:
 * <pre>
 * {@code
 * function isMatchForState(state) {
 *   ...value of isMatchForState...
 * }
 *
 * function onMatch(state) {
 *   ...value of onMatch...
 * }
 * }
 * </pre>
 * <p>
 * So, for example, if
 * <code>isMatchForState = "return (state.mostRecentResponse === 'graph');"</code>
 * then the IConversation implementation would evaluate the result of
 * <pre>
 * {@code
 * function isMatchForState(state) {
 *   return (state.mostRecentResponse === 'graph');
 * }
 * }
 * </pre> to determine if the edge matches the current state.
 *
 * @author pdtyreus
 */
public class JavaScriptEdge<R, S extends IConversationState> extends ConversationEdge<R, S> {

    private final String isMatchForState;

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");

    private static final Logger logger = Logger.getLogger(JavaScriptEdge.class.getName());

    /**
     * Creates an edge with JavaScript code for matching and for updating the
     * state after a match.
     *
     * @param isMatchForState String of JavaScript code
     * @param onMatch String of JavaScript code
     * @param endNode destination node
     */
    public JavaScriptEdge(String isMatchForState, IConversationNode<R, S> endNode) {
        super(endNode);
        this.isMatchForState = isMatchForState;
    }

    @Override
    public boolean isMatchForState(Optional<R> response, S immutableState) {
        Invocable inv = (Invocable) engine;
        R nullableResponse = null;
        if (response.isPresent()) {
            nullableResponse = response.get();
        }
        String template = "function isMatchForState(response, state) {";
        template += isMatchForState;
        template += "};";
        try {
            engine.eval(template);
            return (Boolean) inv.invokeFunction("isMatchForState", nullableResponse, immutableState);
        } catch (ScriptException e) {
            logger.warning(e.getMessage());
            logger.warning(isMatchForState);
            return false;
        } catch (NoSuchMethodException e) {
            logger.severe(e.toString());
            logger.severe(isMatchForState);
            return false;
        } catch (NullPointerException e) {
            logger.warning(e.getMessage());
            logger.warning(isMatchForState);
            return false;
        }
    }

    @Override
    public String toString() {
        return "JavascriptEdge {" + isMatchForState + '}';
    }
}
