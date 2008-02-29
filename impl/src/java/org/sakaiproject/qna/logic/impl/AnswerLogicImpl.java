package org.sakaiproject.qna.logic.impl;

import static org.sakaiproject.qna.logic.test.TestDataPreload.USER_UPDATE;

import java.util.Date;

import org.sakaiproject.qna.dao.QnaDao;
import org.sakaiproject.qna.logic.AnswerLogic;
import org.sakaiproject.qna.logic.CategoryLogic;
import org.sakaiproject.qna.logic.ExternalLogic;
import org.sakaiproject.qna.logic.GeneralLogic;
import org.sakaiproject.qna.logic.OptionsLogic;
import org.sakaiproject.qna.logic.QuestionLogic;
import org.sakaiproject.qna.logic.exceptions.QnaConfigurationException;
import org.sakaiproject.qna.model.QnaAnswer;
import org.sakaiproject.qna.model.QnaCategory;
import org.sakaiproject.qna.model.QnaOptions;
import org.sakaiproject.qna.model.QnaQuestion;

public class AnswerLogicImpl implements AnswerLogic {

	private GeneralLogic generalLogic;

	public void setGeneralLogic(GeneralLogic generalLogic) {
		this.generalLogic = generalLogic;
	}

	private OptionsLogic optionsLogic;

	public void setOptionsLogic(OptionsLogic optionsLogic) {
		this.optionsLogic = optionsLogic;
	}

	private QuestionLogic questionLogic;

	public void setQuestionLogic(QuestionLogic questionLogic) {
		this.questionLogic = questionLogic;
	}

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private QnaDao dao;

	public void setDao(QnaDao dao) {
		this.dao = dao;
	}

	public void addAnswerToQuestion(String questionId, String answerText,
			boolean anonymous, boolean privateReply, String locationId) {
		if (answerText == null) {
			throw new IllegalArgumentException(
					"Answer of a question may not be null");
		}

		String userId = externalLogic.getCurrentUserId();
		if (generalLogic.canAddNewAnswer(locationId, userId)) {
			QnaQuestion question = questionLogic.getQuestionById(questionId);
			if (question == null) {
				throw new QnaConfigurationException("Question with id "+questionId+" does not exist");
			}
			
			if (question.getLocation().equals(locationId)) {
				QnaOptions options = optionsLogic.getOptions(locationId);

				if (anonymous) {
					if (!options.getAnonymousAllowed()) {
						throw new QnaConfigurationException("The location "
								+ locationId
								+ " does not allow anonymous replies");
					}
				}
				Date now = new Date();

				QnaAnswer answer = new QnaAnswer();
				answer.setAnswerText(answerText);
				answer.setAnonymous(anonymous);
				answer.setPrivateReply(privateReply);
				answer.setDateCreated(now);
				answer.setDateLastModified(now);
				answer.setOwnerId(userId);

				if (options.getModerationOn()) {
					answer.setApproved(false);
				} else {
					answer.setApproved(true);
				}
				
				question.addAnswer(answer);
				dao.save(answer);
			} else {
				throw new QnaConfigurationException(
						"The location of the question ("
								+ question.getLocation()
								+ ") and location supplied (" + locationId
								+ ") does not match");
			}
		} else {
			throw new SecurityException("Current user cannot add question for "
					+ locationId + " because they do not have permission");
		}
	}


	public void approveAnswer(String answerId, String locationId) {
		String userId = externalLogic.getCurrentUserId();
		if (generalLogic.canUpdate(locationId, userId)) {
			QnaAnswer answer = getAnswerById(answerId);
			answer.setApproved(true);
			answer.setDateLastModified(new Date());
			answer.setOwnerId(userId);
			dao.save(answer);
		} else {
			throw new SecurityException("Current user cannot approve answers for " + locationId + " because they do not have permission");
		}
	}

	public QnaAnswer getAnswerById(String answerId) {
		return (QnaAnswer) dao.findById(QnaAnswer.class, answerId);
	}

	public void removeAnswerFromQuestion(String answerId, String locationId) {
		String userId = externalLogic.getCurrentUserId();
		if (generalLogic.canUpdate(locationId, userId)) {
			QnaAnswer answer = getAnswerById(answerId);
			dao.delete(answer);
			
		} else {
			throw new SecurityException("Current user cannot delete answers for " + locationId + " because they do not have permission");
		}
	}

	public void withdrawApprovalAnswer(String answerId, String locationId) {
		String userId = externalLogic.getCurrentUserId();
		if (generalLogic.canUpdate(locationId, userId)) {
			QnaAnswer answer = getAnswerById(answerId);
			answer.setApproved(false);
			answer.setDateLastModified(new Date());
			answer.setOwnerId(userId);
			dao.save(answer);
		} else {
			throw new SecurityException("Current user cannot withdraw approval of answers for " + locationId + " because they do not have permission");
		}

	}

}
