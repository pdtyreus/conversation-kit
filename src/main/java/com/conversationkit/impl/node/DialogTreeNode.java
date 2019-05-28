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
import com.conversationkit.model.IConversationNode;
import com.eclipsesource.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>DialogTreeNode</code> is an implementation of 
 * <code>IConversationNode</code> that
 * holds an array of messages to return as the response to the user.
 * <p>
 * A dialog tree is a type of branching conversation often seen in adventure
 * video games. The user is given a choice of what to say and makes subsequent
 * choices until the conversation ends. The responses to the user are scripted
 * based on the choices made. Each DialogTreeNode represents either a question
 * or statement presented to the user by the bot.
 *
 * @author pdtyreus
 */
public class DialogTreeNode implements IConversationNode<DialogTreeEdge> {

    protected final List<String> messages;
    protected final List<DialogTreeEdge> edges;
    private final int id;
    private final JsonObject metadata;

    /**
     * Creates a node with the specified text.
     *
     * @param id the unique ID of the node from the underlying data store
     * @param messages the text responses displayed to the user
     */
    public DialogTreeNode(int id, List<String> messages) {
        this.id = id;
        this.edges = new ArrayList();
        this.metadata = new JsonObject();
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
    
    public List<String> getSuggestedResponses() {
        List<String> suggestions = new ArrayList();
        for (DialogTreeEdge edge : edges) {
            suggestions.add(edge.getPrompt());
        }
        return suggestions;
    }

    @Override
    public Iterable<DialogTreeEdge> getEdges() {
        return edges;
    }

    @Override
    public void addEdge(DialogTreeEdge edge) {
        edges.add(edge);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public JsonObject getMetadata() {
        return metadata;
    }
    
    

}
