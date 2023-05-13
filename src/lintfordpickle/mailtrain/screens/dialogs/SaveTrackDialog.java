package lintfordpickle.mailtrain.screens.dialogs;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
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

	private MenuInputEntry mSceneNameInputEntry;
	private MenuInputEntry mFilenameInputEntry;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public void sceneName(String pFilename) {
		mSceneNameInputEntry.inputString(pFilename);
	}

	public String sceneName() {
		return mSceneNameInputEntry.inputString();
	}

	public void trackFilename(String pFilename) {
		mFilenameInputEntry.inputString(pFilename);
	}

	public String trackFilename() {
		return mFilenameInputEntry.inputString();
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SaveTrackDialog(ScreenManager pScreenManager, MenuScreen pParentScreen) {
		super(pScreenManager, pParentScreen, DIALOG_TITLE, DIALOG_MESSAGE, true);

		mScreenPaddingTop = -50;

		// Don't use the Screen esc. input handler (handle esc. ourselves)
		mESCBackEnabled = false;
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void createMenuEntries(BaseLayout pLayout) {
		super.createMenuEntries(pLayout);

		mSceneNameInputEntry = new MenuInputEntry(screenManager(), mParentScreen);
		mSceneNameInputEntry.label("Name");
		mSceneNameInputEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);

		// TODO: need to pass the parent screen and not null
		mFilenameInputEntry = new MenuInputEntry(screenManager(), mParentScreen);
		mFilenameInputEntry.label("Filename");
		mFilenameInputEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);

		pLayout.addMenuEntry(mSceneNameInputEntry);
		pLayout.addMenuEntry(mFilenameInputEntry);
	}

	@Override
	public void handleInput(LintfordCore core) {
		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE, this) || core.input().gamepads().isGamepadButtonDownTimed(GLFW.GLFW_GAMEPAD_BUTTON_B, this)) {
			if (mScreenState == ScreenState.Active) {
				exitScreen();
				mParentScreen.resetCoolDownTimer();
				return;
			}
		}

		super.handleInput(core);
	}

	@Override
	protected void handleOnClick() {
		super.handleOnClick();

	}

}
