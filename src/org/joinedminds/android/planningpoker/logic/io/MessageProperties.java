/*
 *  The MIT License
 *
 *  Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.joinedminds.android.planningpoker.logic.io;

import org.jivesoftware.smack.XMPPException;
import org.joinedminds.android.planningpoker.logic.PlanningManager;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

/**
* Created: 9/18/11 8:14 PM
*
* @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
*/
public class MessageProperties {
    Properties properties = new Properties();

    public static MessageProperties create(String name, String value) {
        return new MessageProperties().put(name, value);
    }

    public static MessageProperties create(PlanningManager.MessageKey key, String value) {
        return create(key.name(), value);
    }

    public static MessageProperties create(PlanningManager.Type type) {
        return create(PlanningManager.MessageKey.Type, type.name());
    }

    public MessageProperties put(PlanningManager.MessageKey key, String value) {
        return put(key.name(), value);
    }

    public MessageProperties put(String name, String value) {
        properties.setProperty(name, value);
        return this;
    }

    public String toMessageString() throws IOException {
        StringWriter out = new StringWriter();
        properties.store(out, "Planning-Command");
        return out.toString();
    }

    public static MessageProperties fromMessageString(String message) throws IOException {
        MessageProperties properties = new MessageProperties();
        properties.properties.load(new StringReader(message));
        return properties;
    }

    public void send(ChatSession session) throws IOException, XMPPException {
        session.sendMessage(this);
    }

    public String getProperty(PlanningManager.MessageKey key) {
        return properties.getProperty(key.name());
    }

    public String getProperty(PlanningManager.MessageKey key, String defaultValue) {
        return properties.getProperty(key.name(), defaultValue);
    }

    public PlanningManager.Type getType() {
        String str = getProperty(PlanningManager.MessageKey.Type);
        if (str != null) {
            try {
                return PlanningManager.Type.valueOf(str);
            } catch (IllegalArgumentException e) {
                System.err.println("Bad type from sender: " + str + ": " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean getBooleanProperty(PlanningManager.MessageKey key) {
        String val = getProperty(key);
        return Boolean.TRUE.toString().equalsIgnoreCase(val);
    }
}
