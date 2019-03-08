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
package com.conversationkit.impl.node;

import com.conversationkit.impl.edge.DialogTreeEdge;
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.IConversationSnippetButton;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>DialogTreeNode</code> is a restricted implementation of 
 * <code>IConversationNode</code> that
 * holds a text string to represent the displayed conversation snippet and 
 * retrieves a list of allowed responses from the outbound edges. 
 * By default the snippet and allowed
 * responses will be rendered unchanged, but this class can easily be extended
 * to use a template engine for string substitution.
 * <p>
 * A dialog tree is a type of branching conversation often seen in adventure
 * video games. The user is given a choice of what to say and makes subsequent
 * choices until the conversation ends. The responses to the user are scripted
 * based on the choices made. Each DialogTreeNode represents either a question
 * or statement presented to the user by the bot.
 * <p>
 * A <code>DialogTreeNode</code> can either be a statement or question. 
 * A statement will not
 * stop the conversation, continuing along the outbound edge to the next node if
 * available. A question will stop the conversation and wait for a a response
 * from the user. The suggested responses for a question are taken from the
 * content of the outbound edges.
 *
 * @author pdtyreus
 * @param <S> an implementation of IConversationState to store the state of the
 * conversation for the current user
 */
public class DialogTreeNode extends ConversationNode {

    protected final String content;

    /**
     * Creates a node with the specified text.
     *
     * @param id the unique ID of the node from the underlying data store
     * @param type whether the node should be a QUESTION or STATEMENT
     * @param content the text prompt displayed to the user
     */
    public DialogTreeNode(int id, String content) {
        super(id);
        this.content = content;
    }

    public String getValue() {
        return content;
    }

    public Iterable<String> getSuggestedResponses() {
        List<String> allowed = new ArrayList();
        for (IConversationEdge edge : edges) {
            if (edge instanceof DialogTreeEdge) {
                DialogTreeEdge dtEdge = (DialogTreeEdge) edge;
                allowed.add(dtEdge.getIntentId().toString());
            }
        }
        return allowed;
    }

    public SnippetContentType getContentType() {
        return SnippetContentType.TEXT;
    }
    
    public Iterable<IConversationSnippetButton> getButtons() {
        return null;
    }

}
