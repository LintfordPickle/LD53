package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;

public class SignalsPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Signals";

	private static final int BUTTON_CREATE_SIGNAL_A = 10;
	private static final int BUTTON_CREATE_SIGNAL_B = 11;

	private static final int BUTTON_DELETE_SIGNAL = 15;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;

	private UiButton mCreateSignalAButton;
	private UiButton mCreateSignalBButton;

	private UiButton mDeleteSegmentNode;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SignalsPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track;

		mCreateSignalAButton = new UiButton(parentWindow);
		mCreateSignalAButton.buttonLabel("New");
		mCreateSignalAButton.setClickListener(this, BUTTON_CREATE_SIGNAL_A);

		mCreateSignalBButton = new UiButton(parentWindow);
		mCreateSignalBButton.buttonLabel("New");
		mCreateSignalBButton.setClickListener(this, BUTTON_CREATE_SIGNAL_B);

		mDeleteSegmentNode = new UiButton(parentWindow);
		mDeleteSegmentNode.buttonLabel("Delete");
		mDeleteSegmentNode.setClickListener(this, BUTTON_DELETE_SIGNAL);

		addWidget(mCreateSignalAButton);
		addWidget(mCreateSignalBButton);
		addWidget(mDeleteSegmentNode);

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

		mCreateSignalAButton.setPosition(lCurPositionX, lCurPositionY);
		mCreateSignalAButton.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mCreateSignalAButton.height(lWidgetHeight * 1.f);

		lCurPositionY = increaseYPosition(lCurPositionY, mCreateSignalAButton, mCreateSignalBButton) + lVSpacing;

		mCreateSignalBButton.setPosition(lCurPositionX, lCurPositionY);
		mCreateSignalBButton.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mCreateSignalBButton.height(lWidgetHeight * 1.f);

		lCurPositionY = increaseYPosition(lCurPositionY, mCreateSignalBButton, mDeleteSegmentNode) + lVSpacing;

		mDeleteSegmentNode.setPosition(lCurPositionX, lCurPositionY);
		mDeleteSegmentNode.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mDeleteSegmentNode.height(25.f * 1.f);

		lCurPositionY = increaseYPosition(lCurPositionY, mDeleteSegmentNode, null) + lVSpacing;

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
