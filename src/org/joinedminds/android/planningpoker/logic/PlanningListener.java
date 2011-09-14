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

package org.joinedminds.android.planningpoker.logic;

/**
 * Created: 9/14/11 7:56 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public interface PlanningListener {

    /**
     * Called when a communication error is raised in the lower levels.
     * That is not part of a direct public method call.
     * @param ex the exception that was raised.
     */
    void communicationError(Exception ex);

    /**
     * Called when an invite arrives from someone, i.e. the scrum master.
     * @param fromUser the scrum master.
     * @param participants the other users who has been invited.
     */
    void invite(String fromUser, String[] participants);

    /**
     * Called when the scrum master requests a new round of poker.
     * @param fromUser the scrum master.
     */
    void newRound(String fromUser);

    /**
     * Called when a user has selected a card.
     * @param fromUser the user who selected a card.
     * @param card the card selected.
     */
    void cardSelect(String fromUser, String card);

    /**
     * Called when a user has responded to an invite.
     * @param fromUser the user who responded.
     * @param accepted if the user accepted or not.
     */
    void inviteResponse(String fromUser, boolean accepted);
}
