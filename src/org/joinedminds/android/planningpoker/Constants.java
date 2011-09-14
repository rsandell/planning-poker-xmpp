/*
 * The MIT License
 *
 * Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
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

package org.joinedminds.android.planningpoker;

/**
 * Created: 9/13/11 8:42 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public interface Constants {
    String PREF_KEY_SERVER = "server";
    String PREF_KEY_USERNAME = "username";
    String PREF_KEY_PASSWORD = "password";
    String PREF_KEY_PORT = "port";
    String DEFAULT_SERVER = "jabber.org";
    String DEFAULT_USERNAME = "somebody@jabber.org";
    String DEFAULT_PASSWORD = "secret";
    int DEFAULT_PORT = 5222;
    String SHARED_LOGINSETTINGS = "loginsettings";

    String[] CARD_VALUES = {"?", "0", "½", "1", "2", "3", "5", "8", "13", "20", "40", "100", "∞", "@"};


}
