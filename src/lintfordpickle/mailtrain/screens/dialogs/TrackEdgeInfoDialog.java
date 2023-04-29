package lintfordpickle.mailtrain.screens.dialogs;

import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.dialogs.ConfirmationDialog;
import net.lintford.library.screenmanager.entries.MenuInputEntry;
import net.lintford.library.screenmanager.layouts.BaseLayout;

public class TrackEdgeInfoDialog extends ConfirmationDialog {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String DIALOG_TITLE = "Save Track";
	private static final String DIALOG_MESSAGE = "Enter the filename of the new track";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuInputEntry mSegmentNameInputEntry;
	private MenuInputEntry mSegmentSpecialNameInputEntry;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public String segmentName() {
		return mSegmentNameInputEntry.inputString();
	}

	public String segmentSpecialName() {
		return mSegmentSpecialNameInputEntry.inputString();
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public TrackEdgeInfoDialog(ScreenManager pScreenManager, MenuScreen pParentScreen) {
		super(pScreenManager, pParentScreen, DIALOG_TITLE, DIALOG_MESSAGE, true);
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void createMenuEntries(BaseLayout pLayout) {
		super.createMenuEntries(pLayout);

		mSegmentNameInputEntry = new MenuInputEntry(screenManager(), mParentScreen);
		mSegmentNameInputEntry.label("Name");

		mSegmentSpecialNameInputEntry = new MenuInputEntry(screenManager(), mParentScreen);
		mSegmentSpecialNameInputEntry.label("Special");

		pLayout.addMenuEntry(mSegmentNameInputEntry);
		pLayout.addMenuEntry(mSegmentSpecialNameInputEntry);
	}

	@Override
	protected void handleOnClick() {
		super.handleOnClick();
	}

}
