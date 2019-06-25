
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
package com.conversationkit.nlp;

import com.conversationkit.model.IConversationIntent;
import com.google.cloud.dialogflow.v2beta1.QueryResult;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pdtyreus
 */
public class DialogFlowIntent implements IConversationIntent {

    private final QueryResult queryResult;

    public QueryResult getQueryResult() {
        return queryResult;
    }

    public DialogFlowIntent(QueryResult result) {
        this.queryResult = result;
    }

    @Override
    public String getIntentId() {
        //TODO - should this be Action name?
        return this.queryResult.getIntent().getDisplayName();
    }

    @Override
    public Map<String, Object> getSlots() {
        Struct params = this.queryResult.getParameters();
        Map<String, Value> paramMap = params.getFieldsMap();
        Map<String, Object> map = new HashMap();
        for (Map.Entry<String, Value> entry : paramMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

}
