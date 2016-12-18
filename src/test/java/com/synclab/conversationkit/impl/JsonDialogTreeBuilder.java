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
package com.synclab.conversationkit.impl;

import com.synclab.conversationkit.model.ConversationNode;
import com.synclab.conversationkit.model.SnippetType;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import com.synclab.conversationkit.model.IConversationEdge;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationState;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author tyreus
 */
public class JsonDialogTreeBuilder {

    private static final Logger logger = Logger.getLogger(JsonDialogTreeBuilder.class.getName());

    public DirectedConversation<UserDialogTreeState> fromJson(Reader reader) throws IOException {
        return fromJsonWithTemplates(reader);
    }

    public DirectedConversation<UserDialogTreeState> fromJsonWithTemplates(Reader reader) throws IOException {

        JsonValue value = Json.parse(reader);

        JsonObject keyTree = value.asObject();

        Map<Integer, TemplatedDialogTreeNode<UserDialogTreeState>> nodeMap = new HashMap();

        //run through once to create nodes
        for (Member member : keyTree) {
            String name = member.getName();
            Integer id = Integer.parseInt(name);
            JsonObject node = member.getValue().asObject();

            //make the node into something
            String type = node.get("type").asString();
            String content = node.get("content").asString();
            SnippetType snippetType = SnippetType.valueOf(type);
            TemplatedDialogTreeNode dtNode = new TemplatedDialogTreeNode(id, snippetType, content);
            if (node.get("stateKey") != null) {
                dtNode.setStateKey(node.get("stateKey").asString());
            }
            nodeMap.put(id, dtNode);
        }

        logger.info(MessageFormat.format("Created {0} named nodes", nodeMap.keySet().size()));

        //connect the nodes
        for (Member member : keyTree) {
            String name = member.getName();
            JsonObject node = member.getValue().asObject();
            Integer id = Integer.parseInt(name);

            //make the node into something
            String type = node.get("type").asString();
            String content = node.get("content").asString();

            SnippetType snippetType = SnippetType.valueOf(type);
            if ((node.get("next") != null) || (node.get("answers") != null)) {
                final TemplatedDialogTreeNode prevNode = nodeMap.get(id);
                switch (snippetType) {
                    case STATEMENT:
                        for (JsonValue idVal : node.get("next").asArray()) {
                            final ConversationNode nextNode = nodeMap.get(idVal.asInt());
                            IConversationEdge edge = new IConversationEdge() {

                                public IConversationNode getEndNode() {
                                    return nextNode;
                                }

                                public boolean isMatchForState(IConversationState state) {
                                    return true;
                                }

                                public Object transformResponse(IConversationState state) {
                                    throw new UnsupportedOperationException("Not supported yet.");
                                }

                                public String toString() {
                                    return "IConversationEdge {true}";
                                }

                            };

                            prevNode.addEdge(edge);
                            prevNode.getSuggestedResponses().add(content);
                        }
                        break;
                    case QUESTION:
                        for (final JsonValue answerVal : node.get("answers").asArray()) {
                            for (JsonValue idVal : answerVal.asObject().get("next").asArray()) {
                                final ConversationNode nextNode = nodeMap.get(idVal.asInt());
                                final String answerContent = answerVal.asObject().get("content").asString();
                                IConversationEdge edge = new IConversationEdge() {
                                    
                                    public IConversationNode getEndNode() {
                                        return nextNode;
                                    }

                                    public boolean isMatchForState(IConversationState state) {
                                        return answerContent.equals(state.getCurrentResponse());
                                    }

                                    public Object transformResponse(IConversationState state) {
                                        return answerContent;
                                    }

                                    public String toString() {
                                        return "IConversationEdge {" + answerVal.asObject().get("content") + '}';
                                    }
                                };

                                prevNode.addEdge(edge);
                                prevNode.getSuggestedResponses().add(answerContent);
                            }
                        }
                        break;
                }

            }

        }

        MapBackedNodeIndex<UserDialogTreeState> index = new MapBackedNodeIndex();

        index.buildIndexFromStartNode(nodeMap.get(1));

        return new DirectedConversation(index);

    }

}
