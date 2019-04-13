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
package com.conversationkit.impl;

import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.ConversationNodeRepository;
import com.conversationkit.model.IConversationState;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class MapBackedNodeRepository<N extends IConversationNode> implements ConversationNodeRepository<N> {

    private static final Logger logger = Logger.getLogger(MapBackedNodeRepository.class.getName());
    private final Map<Integer, N> nodeIndex = new HashMap();

    @Override
    public N getNodeById(int id) {
        return nodeIndex.get(id);
    }
    
    public void addNodeToIndex(int id, N node) {
        nodeIndex.put(id, node);
    }

}
