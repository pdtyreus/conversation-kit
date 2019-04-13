/*
 * The MIT License
 *
 * Copyright 2017 Synclab Consulting LLC.
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
package com.conversationkit.impl.button.facebook;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pdtyreus
 */
public class WebUrlButton  {

    private final String text;
    private final String value;
    private final Map<String, Object> attributes = new HashMap();

    public WebUrlButton(String url, String title, HeightRatio webviewHeightRatio) {
        this.text = title;
        this.value = url;

    }

    public WebUrlButton(String url, String title, HeightRatio webviewHeightRatio, boolean messengerExtensions, boolean hideShareButton) {
        this.text = title;
        this.value = url;
        this.attributes.put("webview_height_ratio", webviewHeightRatio.toString());
        if (messengerExtensions) {
            this.attributes.put("messenger_extensions", true);
        }
        if (hideShareButton) {
            this.attributes.put("webview_share_button", "hide");
        }

    }

    public String getType() {
        return "web_url";
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public enum HeightRatio {
        compact, tall, full
    }
}
