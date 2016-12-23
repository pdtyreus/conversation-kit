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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches responses based on a regular expression pattern. If a stateKey is 
 * provided, the onMatch method sets the value of this key in the conversation
 * state equal to the first group found in the match. 
 * @author pdtyreus
 */
public class RegexEdge <S extends IConversationState> implements IConversationEdge<S> {

    private final IConversationNode<S> endNode;
    protected final Pattern pattern;
    protected final String stateKey;

    public RegexEdge(String matchRegex, String stateKey, IConversationNode<S> endNode) {
        this.endNode = endNode;
        this.stateKey = stateKey;
        this.pattern = Pattern.compile(matchRegex);
    }
    
    public RegexEdge(String matchRegex, IConversationNode<S> endNode) {
        this.endNode = endNode;
        this.stateKey = null;
        this.pattern = Pattern.compile(matchRegex);
    }
    
    public IConversationNode<S> getEndNode() {
        return endNode;
    }

    public boolean isMatchForState(S state) {
        Matcher matcher = pattern.matcher(state.getCurrentResponse());
        return matcher.find();
    }

    public S onMatch(S state) {
        Matcher matcher = pattern.matcher(state.getCurrentResponse());
        if ((stateKey != null) && matcher.find()) {
            state.set(stateKey, matcher.group());
        } 
        
        return state;
    }

    public String toString() {
        return "RegexEdge {" + pattern.pattern() + '}';
    }
    
}
