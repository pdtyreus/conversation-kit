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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches responses based on a regular expression pattern. If a <code>stateKey</code> is
 * provided, the <code>onMatch</code> method sets the value of this key in the
 * conversation state equal to the first group found in the match or to the
 * <code>stateValue</code> constructor argument if specified.
 *
 * @author pdtyreus
 * @param <S> an implementation of IConversationState
 */
public class RegexEdge<R,S extends IConversationState> extends ConversationEdge<R,S> {

    protected final Pattern pattern;

    /**
     * Creates a RegEx edge that matches the pattern specified. The value of
     * flags is passed to Pattern.compile() to generate Pattern. 
     * 
     * @param matchRegex RegEx string pattern 
     * @param flags value passed to Pattern.compile()
     * @param endNode next node after a match
     */
    public RegexEdge(String matchRegex, int flags, IConversationNode<R,S> endNode) {
        super(endNode);
        this.pattern = Pattern.compile(matchRegex, flags);
    }
    
     /**
     * Creates a RegEx edge that matches the pattern specified. The RegEx is case
     * insensitive by default. 
     * 
     * @param matchRegex RegEx string pattern 
     * @param endNode next node after a match
     */
    public RegexEdge(String matchRegex, IConversationNode<R,S> endNode) {
        super(endNode);
        this.pattern = Pattern.compile(matchRegex, Pattern.CASE_INSENSITIVE);
    }


    @Override
    public boolean isMatchForState(Optional<R> response, S immutableState) {
        
        if (response.isPresent()) {
             Matcher matcher = pattern.matcher(response.get().toString());
            return matcher.find();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "RegexEdge {" + pattern.pattern() + '}';
    }

}
