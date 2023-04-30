package lintfordpickle.mailtrain.screens.dialogs;

import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.dialogs.ConfirmationDialog;
import net.lintford.library.screenmanager.entries.MenuInputEntry;
import net.lintford.library.screenmanager.layouts.BaseLayout;

public class CreateWorldDialog extends ConfirmationDialog {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final int CREATE_WORLD_BUTTON_CONFIRM_YES = 200;
	public static final int CREATE_WORLD_BUTTON_CONFIRM_NO = 101;

	private static final String DIALOG_TITLE = "Create New World";
	private static final String DIALOG_MESSAGE = "Enter the world name";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuInputEntry mWorldNameInputEntry;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public void worldName(String pFilename) {
		mWorldNameInputEntry.inputString(pFilename);
	}

	public String worldName() {
		return mWorldNameInputEntry.inputString();
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public CreateWorldDialog(ScreenManager pScreenManager, MenuScreen pParentScreen) {
		super(pScreenManager, pParentScreen, DIALOG_TITLE, DIALOG_MESSAGE, true);
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void createMenuEntries(BaseLayout pLayout) {
		super.createMenuEntries(pLayout);

		mWorldNameInputEntry = new MenuInputEntry(screenManager(), mParentScreen);
		mWorldNameInputEntry.label("World Name:");
		mWorldNameInputEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);

		pLayout.addMenuEntry(mWorldNameInputEntry);
	}

	@Override
	protected void handleOnClick() {
		super.handleOnClick();
	}

}
