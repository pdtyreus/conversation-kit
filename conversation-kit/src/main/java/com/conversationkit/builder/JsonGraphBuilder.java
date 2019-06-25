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
import com.conversationkit.impl.MapBackedNodeRepository;
import com.conversationkit.impl.edge.ConversationEdge;
import com.conversationkit.impl.node.DialogTreeNode;
import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.ConversationNodeRepository;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * I/O class for loading a node index from a JSON file.
 *
 * @author pdtyreus
 * @see <a href="http://jsongraphformat.info/">JSON Graph Format</a>
 */
public class JsonGraphBuilder {

    private static final Logger logger = Logger.getLogger(JsonGraphBuilder.class.getName());

    /**
     * A default implementation of JsonNodeBuilder.
     */
    public static final JsonNodeBuilder DEFAULT_NODE_BUILDER = (Integer id, String type, JsonObject metadata) -> {

        List<String> messages = new ArrayList();
        if (metadata.get("message") != null) {
            if (metadata.get("message").isArray()) {
                for (JsonValue node : metadata.get("message").asArray()) {
                    messages.add(node.asString());
                }
            } else {
                messages.add(metadata.get("message").asString());
            }
        } else {
            throw new IOException("No \"message\" metadata for node " + id);
        }

        //make the node into something
        IConversationNode conversationNode = new DialogTreeNode(id, messages);

        return conversationNode;
    };

    /**
     * A default implementation of JsonEdgeBuilder.
     */
    public static final JsonEdgeBuilder DEFAULT_EDGE_BUILDER = (String edgeId, String label, JsonObject metadata, Integer targetId) -> {
        IConversationEdge edge = new ConversationEdge(targetId, edgeId);

        return edge;
    };

    protected List<String> createSuggestionsFromMetadata(JsonObject metadata) {

        List<String> s = new ArrayList();
        if (metadata.get("suggestedResponses") != null) {
            JsonArray suggestions = metadata.get("suggestedResponses").asArray();
            for (JsonValue suggestion : suggestions) {
                s.add(suggestion.asString());
            }
        }
        return s;
    }

    /**
     * Reads JSON an creates a node repository using default node and edge builder implementations.
     * @param reader A reader of the JSON source file
     * @return A ConversationNodeRepository
     * @throws IOException when the JSON is not able to be parsed.
     */
    public static ConversationNodeRepository readJsonGraph(Reader reader) throws IOException {

        return readJsonGraph(reader, DEFAULT_NODE_BUILDER, DEFAULT_EDGE_BUILDER);
    }

    /**
     * 
     * @param <N> IConversationNode class
     * @param <E> IConversationEdge class
     * @param reader A reader of the JSON source file
     * @param nodeBuilder Custom node builder implementation
     * @param edgeBuilder Custom edge builder implementation
     * @return A typed ConversationNodeRepository
     * @throws IOException when the JSON is not able to be parsed.
     */
    public static <N extends IConversationNode<E>, E extends IConversationEdge> ConversationNodeRepository<N> readJsonGraph(Reader reader, JsonNodeBuilder<N> nodeBuilder, JsonEdgeBuilder<E> edgeBuilder) throws IOException {

        JsonValue value = Json.parse(reader);

        JsonObject keyTree = value.asObject().get("graph").asObject();

        MapBackedNodeRepository<N> index = new MapBackedNodeRepository();

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

            N conversationNode = nodeBuilder.nodeFromJson(id, type, metadata);
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

            if (edge.get("relation") == null) {
                throw new IOException("Missing \"relation\" for edge: " + edge.toString());
            }
            if (edge.get("source") == null) {
                throw new IOException("Missing \"source\" for edge: " + edge.toString());
            }
            if (edge.get("target") == null) {
                throw new IOException("Missing \"target\" for edge: " + edge.toString());
            }

            Integer sourceId = Integer.parseInt(edge.get("source").asString());
            Integer targetId = Integer.parseInt(edge.get("target").asString());

            N source = index.getNodeById(sourceId);
            N target = index.getNodeById(targetId);

            if (source == null) {
                throw new IOException("Source node missing for edge " + edge);
            }

            if (target == null) {
                throw new IOException("Target node missing for edge " + edge);
            }

            String relation = edge.get("relation").asString();
            String label = edge.getString("label", "");
            JsonValue metadataValue = edge.get("metadata");
            JsonObject metadata = null;
            if (metadataValue != null) {
                metadata = metadataValue.asObject();
            }

            E conversationEdge = edgeBuilder.edgeFromJson(relation, label, metadata, targetId);
            if (conversationEdge == null) {
                throw new IOException("Unhandled edge " + edge);
            }
            source.addEdge(conversationEdge);
            i++;
        }

        logger.info(MessageFormat.format("Created {0} edges", i));
        return index;

    }
}
