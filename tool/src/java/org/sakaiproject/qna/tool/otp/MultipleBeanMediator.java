/***********************************************************************************
 * MultipleBeanMediator.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.qna.tool.otp;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.qna.logic.AttachmentLogic;
import org.sakaiproject.qna.logic.ExternalLogic;
import org.sakaiproject.qna.logic.QuestionLogic;
import org.sakaiproject.qna.logic.exceptions.AttachmentException;
import org.sakaiproject.qna.logic.exceptions.QnaConfigurationException;
import org.sakaiproject.qna.model.QnaAnswer;
import org.sakaiproject.qna.model.QnaCategory;
import org.sakaiproject.qna.model.QnaQuestion;
import org.sakaiproject.qna.tool.utils.TextUtil;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * Mediator for working with multiple beans
 *
 */
public class MultipleBeanMediator {

    public static final String NEW_PREFIX = "new ";
    public static String NEW_1 = NEW_PREFIX + "1";

    private QuestionLocator questionLocator;
	private CategoryLocator categoryLocator;
	private AnswerLocator answerLocator;

    private QuestionLogic questionLogic;
    private ExternalLogic externalLogic;
    private AttachmentLogic attachmentLogic;

    // Used for uploaded files
    public Map<String,CommonsMultipartFile> multipartMap;

	private TargettedMessageList messages;

	private static Log log = LogFactory.getLog(MultipleBeanMediator.class);
	
	public String moveQuestionSave() {
		QnaCategory categoryToLink = null;
		Set<String> keys = questionLocator.getDeliveredBeans().keySet();
		String questionId = keys.iterator().next();

		QnaQuestion question = (QnaQuestion)questionLocator.getDeliveredBeans().get(questionId);

		if (TextUtil.isEmptyWithoutTags(((QnaCategory)categoryLocator.locateBean(NEW_1)).getCategoryText())) {
			if (question.getCategoryId() != null) {
				categoryToLink = (QnaCategory)categoryLocator.locateBean(question.getCategoryId());
			}
		} else {
			categoryLocator.save();
			categoryToLink = (QnaCategory)categoryLocator.locateBean(NEW_1);
		}

		String oldCategory = TextUtil.stripTags(question.getCategory().getCategoryText());

		question.setCategory(categoryToLink);
		question.setSortOrder(new Integer(categoryToLink.getPublishedQuestions().size()));
		questionLogic.saveQuestion(question, externalLogic.getCurrentLocationId());

		messages.addMessage(
			new TargettedMessage("qna.move-question.moved-successfully",
			new Object[] { oldCategory, TextUtil.stripTags(categoryToLink.getCategoryText()) },
			TargettedMessage.SEVERITY_INFO)
		);

		return "saved";
	}


	// Used for saving new question
    // TODO: When time permits: combine the two calls + try to remove categoryId string field from model
    public String saveNew() {
    	QnaCategory categoryToLink=null;

		QnaQuestion newQuestion = (QnaQuestion)questionLocator.locateBean(NEW_1);
		if (TextUtil.isEmptyWithoutTags(newQuestion.getQuestionText())) {
			messages.addMessage(new TargettedMessage("qna.ask-question.save-failure-empty", null, TargettedMessage.SEVERITY_ERROR));
			return "error";
		}


		if (TextUtil.isEmptyWithoutTags(((QnaCategory)categoryLocator.locateBean(NEW_1)).getCategoryText())) {
			if (newQuestion.getCategoryId() != null) {
				categoryToLink = (QnaCategory)categoryLocator.locateBean(newQuestion.getCategoryId());
			}
		} else {
			categoryLocator.save();
			categoryToLink = (QnaCategory)categoryLocator.locateBean(NEW_1);
		}

		newQuestion.setCategory(categoryToLink);
		questionLocator.saveAll();

		if (multipartMap != null) {
			try {
				attachmentLogic.uploadAll(newQuestion.getId(), multipartMap);
			} catch (AttachmentException e) {
				messages.addMessage(new  TargettedMessage("qna.ask-question.error-uploading-files", new Object[]{e.getMessage()}, TargettedMessage.SEVERITY_ERROR));
			}
		}

    	return "saved";
    }

    public String saveAll() {
    	// If a new category was created. Check that category text is not empty.
		if (!TextUtil.isEmptyWithoutTags(((QnaCategory) categoryLocator.locateBean(NEW_1)).getCategoryText())) {
			categoryLocator.save();
			QnaCategory categoryToLink = (QnaCategory) categoryLocator.locateBean(NEW_1);

			for (QnaQuestion question : questionLocator.getDeliveredBeans().values()) {
				question.setCategory(categoryToLink);
			}
			questionLocator.saveAll();
		} else {
			if (questionLocator.getDeliveredBeans().size() == 1) { // Should only be 1
				for (QnaQuestion question : questionLocator.getDeliveredBeans().values()) {
					if (question.getCategoryId() != null) {
						question.setCategory((QnaCategory)categoryLocator.locateBean(question.getCategoryId()));
					}
				}
			}

			questionLocator.saveAll();
		}

		// If answer was added
		if (answerLocator.getDeliveredBeans().values().size() > 0) {

			if (!answerLocator.getDeliveredBeans().containsKey(NEW_1)) {
				answerLocator.saveAll();
			} else if (!TextUtil.isEmptyWithoutTags(((QnaAnswer)answerLocator.locateBean(NEW_1)).getAnswerText())) {
				answerLocator.saveAll();
			}
		}
		return "saved";
    }

	// Used when publishing questions
	public String publish() {
		saveAll();
		for (QnaQuestion question : questionLocator.getDeliveredBeans().values()) {
			try
			{
				questionLogic.publishQuestion(question.getId(), externalLogic.getCurrentLocationId());
				messages.addMessage(new TargettedMessage("qna.publish-queued-question.publish-success",
			                new Object[] { TextUtil.stripTags(question.getQuestionText()) },
			                TargettedMessage.SEVERITY_INFO));
			} catch (QnaConfigurationException qne) {
				log.info("Error received when publishing question: " + qne.getMessage());
				messages.addMessage(new TargettedMessage("qna.publish-queued-question.publish-failure",
		                new Object[] {},
		                TargettedMessage.SEVERITY_ERROR));
			}
		}
		return "saved-published";
	}

	public String deleteQuestions() {

		return "deletedQuestions";
	}

	public void setQuestionLocator(QuestionLocator questionLocator) {
		this.questionLocator = questionLocator;
	}

	public void setCategoryLocator(CategoryLocator categoryLocator) {
		this.categoryLocator = categoryLocator;
	}

	public void setAnswerLocator(AnswerLocator answerLocator) {
		this.answerLocator = answerLocator;
	}

	public void setQuestionLogic(QuestionLogic questionLogic) {
		this.questionLogic = questionLogic;
	}

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public void setAttachmentLogic(AttachmentLogic attachmentLogic) {
		this.attachmentLogic = attachmentLogic;
	}
}
