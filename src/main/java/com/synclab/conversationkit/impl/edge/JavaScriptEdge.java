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
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * An IConversationEdge implementation that delegates matching logic to external
 * JavaScript code. Similar to a <code>RegexEdge</code>, this type of edge 
 * allows users to store the logic for determining if an edge matches in a 
 * location outside the source code. For instance, the string representation
 * of the JavaScript logic could be stored in a database or file representation
 * of the conversation graph.
 * <p> 
 * The supplied JavaScript code modifies the behavior of the 
 * {@link #isMatchForState(IConversationState)}
 * and {@link #onMatch(IConversationState)} 
 * methods. The string representation of the JavaScript code is wrapped as
 * follows:
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
 * </pre>
 * to determine if the edge matches the current state.
 * @author pdtyreus
 */
public class JavaScriptEdge<S extends IConversationState> extends ConversationEdge<S> {

    private final String isMatchForState;
    private final String onMatch;

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");

    private static final Logger logger = Logger.getLogger(JavaScriptEdge.class.getName());

    /**
     * Creates an edge with JavaScript code for matching and for updating the
     * state after a match. 
     * @param isMatchForState String of JavaScript code
     * @param onMatch String of JavaScript code
     * @param endNode destination node
     */
    public JavaScriptEdge(String isMatchForState, String onMatch, IConversationNode<S> endNode) {
        super(endNode);
        this.isMatchForState = isMatchForState;
        this.onMatch = onMatch;
    }

     /**
     * Creates an edge with JavaScript code for matching. This edge will not
     * modify the state after a match since the default <code>onMatch</code>
     * code is just <code>return state;</code>.
     * @param isMatchForState String of JavaScript code
     * @param endNode destination node
     */
    public JavaScriptEdge(String isMatchForState, IConversationNode<S> endNode) {
        super(endNode);
        this.isMatchForState = isMatchForState;
        this.onMatch = "return state;";
    }

    public boolean isMatchForState(S state) {
        Invocable inv = (Invocable) engine;
        String template = "function isMatchForState(state) {";
        template += isMatchForState;
        template += "};";
        try {
            engine.eval(template);
            return (Boolean) inv.invokeFunction("isMatchForState", state);
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

    public void onMatch(S state){
        Invocable inv = (Invocable) engine;
        String template = "function onMatch(state) {";
        template += onMatch;
        template += "};";
        try {
            engine.eval(template);
            inv.invokeFunction("onMatch", state);
        } catch (ScriptException e) {
            logger.warning(e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.severe(e.toString());
        }
    }

}
