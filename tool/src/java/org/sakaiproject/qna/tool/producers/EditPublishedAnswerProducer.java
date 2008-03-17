package org.sakaiproject.qna.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.qna.tool.producers.renderers.NavBarRenderer;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class EditPublishedAnswerProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "edit_published_answer";
	public String getViewID() {
		// TODO Auto-generated method stub
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

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
		
		// Generate the warning if the answers were already viewed
		UIMessage.make(tofill, "error-message", "qna.warning.answer-already-viewed");
		
		// Generate the page title
		UIMessage.make(tofill, "page-title", "qna.edit-published-answer.title");
		
		// Generate the answer title
		UIMessage.make(tofill, "answer-title", "qna.edit-published-answer.answer-title");
		
		// Put in the form
		UIForm form = UIForm.make(tofill,"edit-published-answer-form");		
		
        
//		Generate the answer input box
		UIInput answertext = UIInput.make(form, "answer-input:",null); // last parameter is value binding
        richTextEvolver.evolveTextInput(answertext);
        
		// Generate the different buttons
		UICommand.make(form, "update-button", UIMessage.make("qna.general.update")).setReturn("update");
		UICommand.make(form, "cancel-button",UIMessage.make("qna.general.cancel") ).setReturn("cancel");

	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase("update",new SimpleViewParameters(QuestionsListProducer.VIEW_ID)));
		list.add(new NavigationCase("cancel",new SimpleViewParameters(QuestionsListProducer.VIEW_ID)));
		return list;
	}

}