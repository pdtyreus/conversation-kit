
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

import com.amazonaws.services.lexruntime.model.PostTextResult;
import com.conversationkit.model.IConversationIntent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pdtyreus
 */
public class LexIntent implements IConversationIntent {

    private final PostTextResult queryResult;

    public PostTextResult getPostTextResult() {
        return queryResult;
    }

    public LexIntent(PostTextResult result) {
        this.queryResult = result;
    }

    @Override
    public String getIntentId() {
        return this.queryResult.getIntentName();
    }

    @Override
    public Map<String, Object> getSlots() {
        Map<String,String> params = this.queryResult.getSlots();
        Map<String, Object> map = new HashMap();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

}
