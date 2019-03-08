/*
 * The MIT License
 *
 * Copyright 2019 Synclab Consulting LLC.
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

import com.conversationkit.impl.action.IntentFulfillmentSucceededAction;
import com.conversationkit.redux.Reducer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author pdtyreus
 */
public class ConversationReducer implements Reducer<ConversationAction> {

    @Override
    public Map<String, Object> reduce(ConversationAction action, Map<String, Object> currentState) {
        Map<String, Object> nextState = new HashMap(currentState);
        switch (action.getActionType()) {
            case MESSAGE_RECEIVED:
                nextState.remove("intentId");
                return nextState;
            case INTENT_UNDERSTANDING_SUCCEEDED:
                nextState.remove("misunderstoodCount");
                nextState.put("intentId", ((ConversationAction<Optional<String>>)action).getPayload().get());
                return nextState;
            case INTENT_UNDERSTANDING_FAILED:
                nextState.remove("intentId");
                Integer misunderstoodCount = (Integer)nextState.get("misunderstoodCount");
                if (misunderstoodCount == null) {
                    misunderstoodCount = 0;
                }
                misunderstoodCount++;
                nextState.put("misunderstoodCount", misunderstoodCount);
                return nextState;
            case INTENT_FULFILLMENT_SUCCEEDED:
                nextState.put("nodeId", ((IntentFulfillmentSucceededAction)action).getPayload().get().getId());
                return nextState;
            default:
                return currentState;
        }
        
    }

    public static Integer selectCurrentNodeId(Map<String, Object> currentState) {
        return (Integer)currentState.get("nodeId");
    }
}
