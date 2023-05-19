package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;

public class JunctionsPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Junctions";

	private static final int BUTTON_CREATE_JUNCTION = 10;
	private static final int BUTTON_DELETE_JUNCTION = 15;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;

	private UiButton mCreateJunctionButton;
	private UiButton mDeleteJunctionNode;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public JunctionsPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track;

		mCreateJunctionButton = new UiButton(parentWindow);
		mCreateJunctionButton.buttonLabel("New");
		mCreateJunctionButton.setClickListener(this, BUTTON_CREATE_JUNCTION);

		mDeleteJunctionNode = new UiButton(parentWindow);
		mDeleteJunctionNode.buttonLabel("Delete");
		mDeleteJunctionNode.setClickListener(this, BUTTON_DELETE_JUNCTION);

		addWidget(mCreateJunctionButton);
		addWidget(mDeleteJunctionNode);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();

		mTrackEditorController = (TrackEditorController) lControllerManager.getControllerByNameRequired(TrackEditorController.CONTROLLER_NAME, mEntityGroupUid);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void arrangeWidgets(LintfordCore core) {
		float lCurPositionX = mPanelArea.x() + mPaddingLeft;
		float lCurPositionY = mPanelArea.y() + mPaddingTop;

		float lWidgetHeight = 25.f;
		float lVSpacing = mVerticalSpacing;

		if (mRenderPanelTitle || mIsExpandable) {
			lCurPositionY += getTitleBarHeight();
		}

		mCreateJunctionButton.setPosition(lCurPositionX, lCurPositionY);
		mCreateJunctionButton.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mCreateJunctionButton.height(lWidgetHeight * 1.f);

		lCurPositionY = increaseYPosition(lCurPositionY, mCreateJunctionButton, mDeleteJunctionNode) + lVSpacing;

		mDeleteJunctionNode.setPosition(lCurPositionX, lCurPositionY);
		mDeleteJunctionNode.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mDeleteJunctionNode.height(25.f * 1.f);

		lCurPositionY = increaseYPosition(lCurPositionY, mDeleteJunctionNode, null) + lVSpacing;

	}

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
