package lintfordpickle.mailtrain.screens.dialogs;

import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.dialogs.ConfirmationDialog;
import net.lintford.library.screenmanager.entries.MenuInputEntry;
import net.lintford.library.screenmanager.layouts.BaseLayout;

public class SaveTrackDialog extends ConfirmationDialog {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String DIALOG_TITLE = "Save Track";
	private static final String DIALOG_MESSAGE = "Enter the filename of the new track";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuInputEntry mFilenameInputEntry;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public void trackFilename(String pFilename) {
		mFilenameInputEntry.inputString(pFilename);
	}

	public String trackFilename() {
		return mFilenameInputEntry.inputString();
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SaveTrackDialog(ScreenManager pScreenManager, Screen pParentScreen) {
		super(pScreenManager, pParentScreen, DIALOG_TITLE, DIALOG_MESSAGE, true);
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void createMenuEntries(BaseLayout pLayout) {
		super.createMenuEntries(pLayout);

		// TODO: need to pass the parent screen and not null
		mFilenameInputEntry = new MenuInputEntry(screenManager(), null);
		mFilenameInputEntry.label("Filename");
		pLayout.addMenuEntry(mFilenameInputEntry);
	}

	@Override
	protected void handleOnClick() {
		super.handleOnClick();
	}

}
