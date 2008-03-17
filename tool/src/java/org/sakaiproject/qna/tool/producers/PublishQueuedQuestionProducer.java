package org.sakaiproject.qna.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.qna.logic.CategoryLogic;
import org.sakaiproject.qna.logic.ExternalLogic;
import org.sakaiproject.qna.logic.QuestionLogic;
import org.sakaiproject.qna.model.QnaCategory;
import org.sakaiproject.qna.model.QnaQuestion;
import org.sakaiproject.qna.tool.params.QuestionParams;
import org.sakaiproject.qna.tool.producers.renderers.NavBarRenderer;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class PublishQueuedQuestionProducer implements ViewComponentProducer,NavigationCaseReporter,ViewParamsReporter {

	public static final String VIEW_ID = "publish_queued_question";
	public String getViewID() {
		return VIEW_ID;
	}
	
	
	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
        this.richTextEvolver = richTextEvolver;
    }

	private NavBarRenderer navBarRenderer;
	public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
	
	private QuestionLogic questionLogic;
	public void setQuestionLogic(QuestionLogic questionLogic) {
		this.questionLogic = questionLogic;
	}
	
	private CategoryLogic categoryLogic;
	public void setCategoryLogic(CategoryLogic categoryLogic) {
		this.categoryLogic = categoryLogic;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		QuestionParams questionParams = (QuestionParams) viewparams;
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
		QnaQuestion question = questionLogic.getQuestionById(questionParams.questionid);
		
		
		// Generate the page title
		UIMessage.make(tofill, "page-title", "qna.publish-queued-question.title");
		
		// Put in the form
		UIForm form = UIForm.make(tofill,"publish-queued-question-form");
		
		// Generate the question title
		UIMessage.make(form, "question-title", "qna.publish-queued-question.question-title");
				
		
		UIVerbatim.make(form, "unpublished-question", question.getQuestionText());
		
		// TODO: Make edit work
		UIInternalLink.make(form,"question-link",UIMessage.make("qna.publish-queued-question.question-link"),new SimpleViewParameters(EditPublishedQuestionProducer.VIEW_ID));
				
		// Generate the category title
		UIMessage.make(form, "category-title", "qna.publish-queued-question.category-title");
		
		// Generate the category note
		UIMessage.make(form, "category-note", "qna.publish-queued-question.category-note");
		
	       List<QnaCategory> categories = categoryLogic
				.getCategoriesForLocation(externalLogic.getCurrentLocationId());

		String[] categoriesIds = new String[categories.size()];
		String[] categoriesText = new String[categories.size()];

		for (int i = 0; i < categories.size(); i++) {
			QnaCategory category = categories.get(i);
			categoriesIds[i] = category.getId();
			categoriesText[i] = category.getCategoryText();
		}

        
        UISelect.make(form, "category-select", categoriesIds, categoriesText, null);
		
     // if (user permission to create categories)
        UIMessage.make(form,"or","qna.general.or");
        UIMessage.make(form,"new-category-label","qna.publish-queued-question.category-label");
        UIInput.make(form, "new-category-name", null);
        
     // Generate the answer title
		UIMessage.make(form, "answer-title", "qna.publish-queued-question.answer-title");
		
		// Generate the answer note
		UIMessage.make(form, "answer-note", "qna.publish-queued-question.answer-note");
        
//		Generate the answer input box
		UIInput answertext = UIInput.make(form, "reply-input:",null); // last parameter is value binding
        richTextEvolver.evolveTextInput(answertext);
        
		// Generate the different buttons
		UICommand.make(form, "published-button", UIMessage.make("qna.general.publish")).setReturn("publish");
		UICommand.make(form, "cancel-button",UIMessage.make("qna.general.cancel") ).setReturn("cancel");

	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase("publish",new SimpleViewParameters(QuestionsListProducer.VIEW_ID)));
		list.add(new NavigationCase("cancel",new SimpleViewParameters(QuestionsListProducer.VIEW_ID)));
		return list;
	}
	
	public ViewParameters getViewParameters() {
		return new QuestionParams();
	}

}