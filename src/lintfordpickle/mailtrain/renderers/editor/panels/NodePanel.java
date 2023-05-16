package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiLabelledInt;

public class NodePanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Nodes";

	private static final int BUTTON_NEW_NODE = 10;
	private static final int BUTTON_DELETE_NODE = 11;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;

	private UiLabelledInt mSelectedNodeALabel;
	private UiLabelledInt mSelectedNodeBLabel;

	private UiButton mNewTrackButton;
	private UiButton mDeleteNode;

	private RailTrackNode mSelectedNodeA;
	private RailTrackNode mSelectedNodeB;

	// Renderers
	private EditorTrackRenderer mEditorTrackRenderer;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public NodePanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track_Node;

		mNewTrackButton = new UiButton(parentWindow);
		mNewTrackButton.buttonLabel("New");
		mNewTrackButton.setClickListener(this, BUTTON_NEW_NODE);

		mSelectedNodeALabel = new UiLabelledInt(parentWindow, "Selected Node A:");
		mSelectedNodeBLabel = new UiLabelledInt(parentWindow, "Selected Node B:");

		mDeleteNode = new UiButton(parentWindow);
		mDeleteNode.buttonLabel("Delete");
		mDeleteNode.setClickListener(this, BUTTON_DELETE_NODE);

		addWidget(mNewTrackButton);
		addWidget(mSelectedNodeALabel);
		addWidget(mSelectedNodeBLabel);
		addWidget(mDeleteNode);

		isLayerVisible(true);
		mIsPanelOpen = true;
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();

		mTrackEditorController = (TrackEditorController) lControllerManager.getControllerByNameRequired(TrackEditorController.CONTROLLER_NAME, mEntityGroupUid);

		final var lRendererManager = mParentWindow.rendererManager();
		mEditorTrackRenderer = (EditorTrackRenderer) lRendererManager.getRenderer(EditorTrackRenderer.RENDERER_NAME);
		mEditorTrackRenderer.drawEditorNodes(isLayerVisible());
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		if (mSelectedNodeA != mTrackEditorController.mSelectedNodeA) {
			mSelectedNodeA = mTrackEditorController.mSelectedNodeA;

			if (mSelectedNodeA != null) {
				mSelectedNodeALabel.value(mSelectedNodeA.uid);
				mDeleteNode.isEnabled(true);
			} else {
				mSelectedNodeALabel.value(0);
				mDeleteNode.isEnabled(false);
			}
		}

		if (mSelectedNodeB != mTrackEditorController.mSelectedNodeB) {
			mSelectedNodeB = mTrackEditorController.mSelectedNodeB;

			if (mSelectedNodeB != null) {
				mSelectedNodeBLabel.value(mSelectedNodeB.uid);
			} else {
				mSelectedNodeBLabel.value(0);
			}
		}
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
		case BUTTON_NEW_NODE:
			final var lCursorPositionX = mEditorBrushController.cursorWorldX();
			final var lCursorPositionY = mEditorBrushController.cursorWorldY();

			mTrackEditorController.handleNodeCreation(lCursorPositionX, lCursorPositionY);
			break;

		case BUTTON_DELETE_NODE:
			if (mSelectedNodeA == null)
				return;

			mTrackEditorController.deleteSelectedNodes();
			break;

		case BUTTON_SHOW_LAYER:
			mEditorTrackRenderer.drawEditorNodes(isLayerVisible());
			return;
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
