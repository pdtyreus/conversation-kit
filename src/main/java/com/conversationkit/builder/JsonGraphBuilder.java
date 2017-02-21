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
package com.conversationkit.builder;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.conversationkit.impl.DirectedConversationEngine;
import com.conversationkit.impl.MapBackedNodeIndex;
import com.conversationkit.impl.edge.AffirmativeEdge;
import com.conversationkit.impl.edge.DialogTreeEdge;
import com.conversationkit.impl.edge.JavaScriptEdge;
import com.conversationkit.impl.edge.NegativeEdge;
import com.conversationkit.impl.edge.RegexEdge;
import com.conversationkit.impl.edge.StatementEdge;
import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.impl.node.HiddenNode;
import com.conversationkit.impl.node.ResponseSuggestingNode;
import com.conversationkit.impl.node.StringReplacingNode;
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * I/O class for loading a directed conversation from a JSON file. Extend this
 * class to handle custom node and edge types.
 *
 * @author pdtyreus
 * @see <a href="http://jsongraphformat.info/">JSON Graph Format</a>
 */
public class JsonGraphBuilder<S extends IConversationState> {

    private static final Logger logger = Logger.getLogger(JsonGraphBuilder.class.getName());

    protected enum NodeType {

        StringReplacing, ResponseSuggesting, DialogTree, Hidden
    }

    protected enum EdgeType {

        DialogTree, JavaScript, Regex, Statement, Affirmative, Negative
    }

    /**
     * Creates an <code>IConversationNode</code> from JSON. Override this to
     * handle additional node types. The call to
     * <code>super.nodeFromJson()</code> will return null if the node type is
     * not currently handled.
     *
     * @param id the node id
     * @param type the node type
     * @param content the value of the content key
     * @param snippetType question or statement
     * @param contentType content type to render
     * @param metadata the additional metadata in the JSON
     * @return a node or null
     * @throws IOException exception parsing JSON
     */
    protected IConversationNode<S> nodeFromJson(Integer id, String type, String content, SnippetType snippetType, SnippetContentType contentType, JsonObject metadata) throws IOException {
     
        NodeType nodeType;
        try {
            nodeType = NodeType.valueOf(type);
        } catch (Exception e) {
            return null;
        }

        //make the node into something
        IConversationNode<S> conversationNode = null;

        switch (nodeType) {
            case Hidden:
                conversationNode = new HiddenNode(id,snippetType);
                break;
            case DialogTree:
                DialogTreeNode dtNode = new DialogTreeNode(id, snippetType, content);
                conversationNode = dtNode;
                break;
            case StringReplacing:
                StringReplacingNode srNode = new StringReplacingNode(id, snippetType, content);
                if (metadata.get("suggestedResponses") != null) {
                    JsonArray suggestions = metadata.get("suggestedResponses").asArray();
                    for (JsonValue suggestion : suggestions) {
                        srNode.addSuggestedResponse(suggestion.asString());
                    }
                }
                conversationNode = srNode;
                break;
            case ResponseSuggesting:
                ResponseSuggestingNode rsNode = new ResponseSuggestingNode(id, snippetType, content, contentType);
                if (metadata.get("suggestedResponses") != null) {
                    JsonArray suggestions = metadata.get("suggestedResponses").asArray();
                    for (JsonValue suggestion : suggestions) {
                        rsNode.addSuggestedResponse(suggestion.asString());
                    }
                }
                conversationNode = rsNode;
                break;
            default:
                return null;
        }

        return conversationNode;
    }

    /**
     * Creates an <code>IConversationEdge</code> from JSON. Override this to
     * handle additional edge types. The call to
     * <code>super.edgeFromJson()</code> will return null if the edge type is
     * not currently handled.
     *
     * @param type the edge type
     * @param metadata the additional metadata in the JSON
     * @param target the target node
     * @return an edge or null
     * @throws IOException exception parsing JSON
     */
    protected IConversationEdge<S> edgeFromJson(String type, JsonObject metadata, IConversationNode<S> target) throws IOException {

        EdgeType edgeType;
        try {
            edgeType = EdgeType.valueOf(type);
        } catch (Exception e) {
            return null;
        }
        String stateKey = null;
        if ((metadata != null) && (metadata.get("stateKey") != null)) {
            stateKey = metadata.get("stateKey").asString();
        }
        Object stateValue = null;
        if ((metadata != null) && metadata.get("stateValue") != null) {

            if (metadata.get("stateValue").isArray()) {
                stateValue = metadata.get("stateValue").asArray();
            } else if (metadata.get("stateValue").isBoolean()) {
                stateValue = metadata.get("stateValue").asBoolean();
            } else if (metadata.get("stateValue").isNumber()) {
                stateValue = metadata.get("stateValue").asInt();
            } else {
                stateValue = metadata.get("stateValue").asString();
            }
        }
        switch (edgeType) {
            case DialogTree:
                if ((metadata == null) || (metadata.get("answer") == null)) {
                    throw new IOException("DialogTreeEdge missing \"answer\" metadata key: " + metadata);
                }
                DialogTreeEdge dte = new DialogTreeEdge(metadata.get("answer").asString(), stateKey, target);
                return dte;
            case JavaScript:

                if ((metadata == null) || (metadata.get("isMatchForState") == null)) {
                    throw new IOException("JavaScriptEdge missing \"isMatchForState\" metadata key: " + metadata);
                }
                String isMatch = metadata.get("isMatchForState").asString();
                if (metadata.get("onMatch") != null) {
                    String onMatch = metadata.get("onMatch").asString();
                    return new JavaScriptEdge(isMatch, onMatch, target);
                } else {
                    return new JavaScriptEdge(isMatch, target);
                }
            case Regex:
                if ((metadata == null) || (metadata.get("pattern") == null)) {
                    throw new IOException("RegexEdge missing \"pattern\" metadata key: " + metadata);
                }
                String pattern = metadata.get("pattern").asString();
                if (stateValue != null) {
                    return new RegexEdge(pattern, stateKey, stateValue, target);
                } else {
                    return new RegexEdge(pattern, stateKey, target);
                }
            case Affirmative:
                if (stateValue != null) {
                    return new AffirmativeEdge(stateKey, stateValue, target);
                } else {
                    return new AffirmativeEdge(target);
                }
            case Negative:
                if (stateValue != null) {
                    return new NegativeEdge(stateKey, stateValue, target);
                } else {
                    return new NegativeEdge(target);
                }
            case Statement:
                StatementEdge e = new StatementEdge(target);
                return e;
            default:
                return null;
        }
    }

    public DirectedConversationEngine<S> readJsonGraph(Reader reader) throws IOException {

        JsonValue value = Json.parse(reader);

        JsonObject keyTree = value.asObject().get("graph").asObject();

        MapBackedNodeIndex<S> index = new MapBackedNodeIndex();

        int i = 0;
        //run through once to create nodes
        for (JsonValue member : keyTree.get("nodes").asArray()) {
            JsonObject node = member.asObject();
            Integer id;
            try {
                id = Integer.parseInt(node.get("id").asString());
            } catch (Exception e) {
                throw new IOException("Missing or Invalid \"id\" for node: " + node.toString());
            }
            if (node.get("label") == null) {
                throw new IOException("Missing \"label\" for node " + id);
            }
            if (node.get("type") == null) {
                throw new IOException("Missing \"type\" for node " + id);
            }

            String type = node.get("type").asString();
            String content = node.get("label").asString();
            JsonValue metadataValue = node.get("metadata");
            JsonObject metadata = null;
            if (metadataValue == null) {
                throw new IOException("Missing \"metadata\" for node " + id);
            } else {
                metadata = metadataValue.asObject();
            }
            
            SnippetType snippetType = SnippetType.valueOf(metadata.get("snippetType").asString());
        SnippetContentType contentType = SnippetContentType.TEXT;
        if (metadata.get("contentType") != null) {
            contentType = SnippetContentType.valueOf(metadata.get("contentType").asString());
        }

            IConversationNode<S> conversationNode = nodeFromJson(id, type, content, snippetType, contentType, metadata);
            if (conversationNode == null) {
                throw new IOException("Unhandled node " + node);
            }
            index.addNodeToIndex(conversationNode.getId(), conversationNode);
            i++;
        }

        logger.info(MessageFormat.format("Created {0} nodes", i));

        i = 0;

        //connect the nodes
        for (JsonValue member : keyTree.get("edges").asArray()) {
            JsonObject edge = member.asObject();

            if (edge.get("type") == null) {
                throw new IOException("Missing \"type\" for edge: " + edge.toString());
            }
            if (edge.get("source") == null) {
                throw new IOException("Missing \"source\" for edge: " + edge.toString());
            }
            if (edge.get("target") == null) {
                throw new IOException("Missing \"target\" for edge: " + edge.toString());
            }

            Integer sourceId = Integer.parseInt(edge.get("source").asString());
            Integer targetId = Integer.parseInt(edge.get("target").asString());

            IConversationNode<S> source = index.getNodeAtIndex(sourceId);
            IConversationNode<S> target = index.getNodeAtIndex(targetId);

            if (source == null) {
                throw new IOException("Source node missing for edge " + edge);
            }

            if (target == null) {
                throw new IOException("Target node missing for edge " + edge);
            }

            String type = edge.get("type").asString();
            JsonValue metadataValue = edge.get("metadata");
            JsonObject metadata = null;
            if (metadataValue != null) {
                metadata = metadataValue.asObject();
            }

            IConversationEdge<S> conversationEdge = edgeFromJson(type, metadata, target);
            if (conversationEdge == null) {
                throw new IOException("Unhandled edge " + edge);
            }
            source.addEdge(conversationEdge);
            i++;
        }

        logger.info(MessageFormat.format("Created {0} edges", i));
        return new DirectedConversationEngine(index);

    }
}
