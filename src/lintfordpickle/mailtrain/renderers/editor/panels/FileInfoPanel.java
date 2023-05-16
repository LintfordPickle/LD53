package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.editor.gui.EditorFileController;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiHorizontalEntryGroup;
import net.lintford.library.renderers.windows.components.UiInputText;

public class FileInfoPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final int BUTTON_NEW = 10;
	private static final int BUTTON_SAVE = 11;
	private static final int BUTTON_LOAD = 12;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private EditorFileController mEditorFileController;

	private UiInputText mFileName;
	private UiInputText mTrackPath;

	private UiHorizontalEntryGroup mHorizontalGroup;

	private UiButton mNewTrackButton;
	private UiButton mSaveTrackButton;
	private UiButton mLoadTrackButton;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public FileInfoPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, "File", entityGroupdUid);

		mShowActiveLayerButton = false;
		mShowShowLayerButton = false;

		mRenderPanelTitle = true;
		mPanelTitle = "File Info";

		mFileName = new UiInputText(parentWindow);
		mFileName.maxnumInputCharacters(20);
		mFileName.emptyString("<filename>");

		mTrackPath = new UiInputText(parentWindow);
		mTrackPath.maxnumInputCharacters(200);
		mTrackPath.emptyString("<path>");
		mTrackPath.inputString(ConstantsGame.WORLD_DIRECTORY);
		mTrackPath.isReadonly(true);

		mNewTrackButton = new UiButton(parentWindow);
		mNewTrackButton.buttonLabel("New");
		mNewTrackButton.setClickListener(this, BUTTON_NEW);

		mSaveTrackButton = new UiButton(parentWindow);
		mSaveTrackButton.buttonLabel("Save");
		mSaveTrackButton.setClickListener(this, BUTTON_SAVE);

		mLoadTrackButton = new UiButton(parentWindow);
		mLoadTrackButton.buttonLabel("Load");
		mLoadTrackButton.setClickListener(this, BUTTON_LOAD);

		mHorizontalGroup = new UiHorizontalEntryGroup(parentWindow);
		mHorizontalGroup.widgets().add(mNewTrackButton);
		mHorizontalGroup.widgets().add(mSaveTrackButton);

		addWidget(mFileName);
		addWidget(mHorizontalGroup);
		addWidget(mTrackPath);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();

		mEditorFileController = (EditorFileController) lControllerManager.getControllerByNameRequired(EditorFileController.CONTROLLER_NAME, mEntityGroupUid);

		mFileName.inputString("filename");
		mTrackPath.inputString("track");
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	public void widgetOnDataChanged(InputManager inputManager, int entryUid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetOnClick(InputManager inputManager, int entryUid) {
		switch (entryUid) {

		}
	}

	@Override
	public int layerOwnerHashCode() {
		return hashCode();
	}

	// --------------------------------------
	// Inherited Methods (IInputProcessor)
	// --------------------------------------

	@Override
	public boolean allowKeyboardInput() {
		return false;
	}

	@Override
	public boolean allowGamepadInput() {
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		return false;
	}
}
