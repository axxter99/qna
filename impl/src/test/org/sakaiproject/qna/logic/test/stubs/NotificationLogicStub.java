/**
 * Copyright (c) 2007-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.qna.logic.test.stubs;

import org.sakaiproject.qna.logic.NotificationLogic;
import org.sakaiproject.qna.model.QnaQuestion;

public class NotificationLogicStub implements NotificationLogic {

	public void sendNewAnswerNotification(String[] userids,
			QnaQuestion question, String answerText) {
		// TODO Auto-generated method stub

	}

	public void sendPrivateReplyNotification(String[] userids,
			QnaQuestion question, String privateReplyText) {
		// TODO Auto-generated method stub

	}

	public void sendNewQuestionNotification(String[] emails, QnaQuestion question) {
		// TODO Auto-generated method stub
		
	}

	public void sendNewQuestionNotification(String[] emails,
			QnaQuestion question, String fromUserId) {
		// TODO Auto-generated method stub
		
	}

	public void sendNewAnswerSmsNotification(String[] mobileNrs,
			QnaQuestion question, String answerText) {
		// TODO Auto-generated method stub
		
	}

}
