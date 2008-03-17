package org.sakaiproject.qna.tool.producers.renderers;

import java.util.Collections;
import java.util.List;

import org.sakaiproject.qna.logic.CategoryLogic;
import org.sakaiproject.qna.logic.ExternalLogic;
import org.sakaiproject.qna.logic.PermissionLogic;
import org.sakaiproject.qna.logic.QuestionLogic;
import org.sakaiproject.qna.model.QnaCategory;
import org.sakaiproject.qna.model.QnaQuestion;
import org.sakaiproject.qna.tool.comparators.CategoryTextComparator;
import org.sakaiproject.qna.tool.params.QuestionParams;
import org.sakaiproject.qna.tool.params.ViewTypeParams;
import org.sakaiproject.qna.tool.producers.QueuedQuestionProducer;
import org.sakaiproject.qna.tool.producers.ViewPrivateReplyProducer;
import org.sakaiproject.qna.tool.producers.ViewQuestionProducer;
import org.sakaiproject.qna.tool.utils.DateUtil;
import org.sakaiproject.qna.tool.utils.TextUtil;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;

public class CategoryQuestionListRenderer implements QuestionListRenderer {
	
	ExternalLogic externalLogic;
	PermissionLogic permissionLogic;
	CategoryLogic categoryLogic;
	QuestionLogic questionLogic;
	
    public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void setPermissionLogic(PermissionLogic permissionLogic) {
		this.permissionLogic = permissionLogic;
	}
	
	public void setCategoryLogic(CategoryLogic categoryLogic) {
		this.categoryLogic = categoryLogic;
	}

	public void setQuestionLogic(QuestionLogic questionLogic) {
		this.questionLogic = questionLogic;
	}
	
	public void makeQuestionList(UIContainer tofill, String divID, ViewTypeParams params) {
    	// Front-end customization regarding permissions/options will come here
    	UIJointContainer listTable = new UIJointContainer(tofill,divID,"question-list-table:");
		UIMessage.make(listTable, "categories-title", "qna.view-questions.categories");
		UIMessage.make(listTable, "answers-title", "qna.view-questions.answers");
		UIMessage.make(listTable,"views-title","qna.view-questions.views");
		
		// Creates remove heading for users with update rights
		if (permissionLogic.canUpdate(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId())) {
			UIMessage.make(listTable,"modified-title","qna.view-questions.modified");
			UIMessage.make(listTable, "remove-title", "qna.view-questions.remove");
		} else { // To remove irritating scrollbar rendered
			UIMessage.make(listTable,"modified-title-longer","qna.view-questions.modified");
		}
		
		List<QnaCategory> categories = categoryLogic.getCategoriesForLocation(externalLogic.getCurrentLocationId());
		Collections.sort(categories,new CategoryTextComparator());

		// List of published questions by category
		for (QnaCategory qnaCategory : categories) {
			if (qnaCategory.getPublishedQuestions().size() > 0) {
				UIBranchContainer entry = UIBranchContainer.make(listTable, "table-entry:");		
				UIBranchContainer category = UIBranchContainer.make(entry,"category-entry:");
			
				initViewToggle(entry, category);
				UIOutput.make(category,"category-name",qnaCategory.getCategoryText());
				UIOutput.make(category,"modified-date",DateUtil.getSimpleDate(qnaCategory.getDateLastModified()));
				
				if (permissionLogic.canUpdate(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId())) {
					UIOutput.make(category,"remove-category-cell");
					UIBoundBoolean.make(category, "remove-checkbox",false);
				}
				
				List<QnaQuestion> publishedQuestions = qnaCategory.getPublishedQuestions();
				renderQuestions(entry,publishedQuestions,ViewQuestionProducer.VIEW_ID);
			}
			
		}
		
		// Only users with update permissions can view new questions + private replies
		if (permissionLogic.canUpdate(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId())) {
			
			// All new questions (questions not published)
			List<QnaQuestion> newQuestions = questionLogic.getNewQuestions(externalLogic.getCurrentLocationId());
			
			if (newQuestions.size() > 0) {
				UIBranchContainer entry = UIBranchContainer.make(listTable, "table-entry:");		
				UIBranchContainer category = UIBranchContainer.make(entry,"category-entry:");
				
				if (showFlagIcon(newQuestions)) {
					UILink.make(category,"new-question-icon","/library/image/silk/flag_yellow.png");
				}
		
				initViewToggle(entry, category);
				UIMessage.make(category,"category-name","qna.view-questions.new-questions");
				UIOutput.make(category,"modified-date","");
				UIOutput.make(category,"remove-category-cell","");
				renderQuestions(entry,newQuestions,QueuedQuestionProducer.VIEW_ID);
			}
			
			// All questions with Private Replies
			List<QnaQuestion> questionsWithPrivateReplies = questionLogic.getQuestionsWithPrivateReplies(externalLogic.getCurrentLocationId());
			if (questionsWithPrivateReplies.size() > 0) {
				UIBranchContainer entry = UIBranchContainer.make(listTable, "table-entry:");		
				UIBranchContainer category = UIBranchContainer.make(entry,"category-entry:");
				
				initViewToggle(entry, category);
				UIMessage.make(category,"category-name","qna.view-questions.questions-with-private-replies");
				UIOutput.make(category,"modified-date","");
				UIOutput.make(category,"remove-category-cell","");
				renderQuestions(entry,questionsWithPrivateReplies,ViewPrivateReplyProducer.VIEW_ID);
			}
		}
	}

	/**
	 *	Initiate javascript to show/hide by category
	 */
	private void initViewToggle(UIBranchContainer entry, UIBranchContainer category) {
		String expandIconSrc = "/library/image/sakai/expand.gif";
		String collapseIconSrc = "/library/image/sakai/collapse.gif";
		
		UILink expandIcon = UILink.make(category, "expand-icon", expandIconSrc);
		UILink collapseIcon = UILink.make(category, "collapse-icon", collapseIconSrc);
		
		UIInitBlock.make(category,"onclick-init","init_questions_toggle", new Object[]{expandIcon,collapseIcon,entry});
	}
	
	/**
	 * Renders list of questions
	 */
	private void renderQuestions(UIBranchContainer entry, List<QnaQuestion> questions, String viewIdForLink) {
		
		for (QnaQuestion qnaQuestion : questions) {
			UIBranchContainer question = UIBranchContainer.make(entry, "question-entry:");
			UIInternalLink.make(question,"question-link",TextUtil.stripTags(qnaQuestion.getQuestionText()),new QuestionParams(viewIdForLink,qnaQuestion.getId()));
			UIOutput.make(question,"answers-nr",qnaQuestion.getAnswers().size() +"");
			UIOutput.make(question,"views-nr",qnaQuestion.getViews().toString());
			UIOutput.make(question,"question-modified-date",DateUtil.getSimpleDate(qnaQuestion.getDateLastModified()));
				
			if (permissionLogic.canUpdate(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId())) {
				UIOutput.make(question,"remove-question-cell");
				UIBoundBoolean.make(question, "remove-checkbox",false);
			}
		}
	}
	
	/**
	 * Check if flag icon must be shown (if there are new questions which have not been viewed)
	 */
	private boolean showFlagIcon(List<QnaQuestion> newQuestions) {
		for (QnaQuestion qnaQuestion : newQuestions) {
			if (qnaQuestion.getViews() == 0) {
				return true;
			}
		}
		return false;
	}

}