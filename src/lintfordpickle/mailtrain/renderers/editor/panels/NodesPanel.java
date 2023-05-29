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

public class NodesPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Nodes";

	private static final int BUTTON_NEW_NODE = 10;
	private static final int BUTTON_MOVE_NODE = 11;
	private static final int BUTTON_DELETE_NODE = 12;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;

	private UiLabelledInt mSelectedNodeALabel;
	private UiLabelledInt mSelectedNodeBLabel;

	private UiButton mNewNodeButton;
	private UiButton mMoveNodeButton;
	private UiButton mDeleteNodeNode;

	private RailTrackNode mSelectedNodeA;
	private RailTrackNode mSelectedNodeB;

	// Renderers
	private EditorTrackRenderer mEditorTrackRenderer;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public NodesPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track;

		mNewNodeButton = new UiButton(parentWindow);
		mNewNodeButton.buttonLabel("New");
		mNewNodeButton.setClickListener(this, BUTTON_NEW_NODE);

		mMoveNodeButton = new UiButton(parentWindow);
		mMoveNodeButton.buttonLabel("Move");
		mMoveNodeButton.setClickListener(this, BUTTON_MOVE_NODE);
		mMoveNodeButton.isEnabled(false);

		mSelectedNodeALabel = new UiLabelledInt(parentWindow, "Selected Node A:");
		mSelectedNodeBLabel = new UiLabelledInt(parentWindow, "Selected Node B:");

		mDeleteNodeNode = new UiButton(parentWindow);
		mDeleteNodeNode.buttonLabel("Delete");
		mDeleteNodeNode.setClickListener(this, BUTTON_DELETE_NODE);

		addWidget(mNewNodeButton);
		addWidget(mMoveNodeButton);
		addWidget(mSelectedNodeALabel);
		addWidget(mSelectedNodeBLabel);
		addWidget(mDeleteNodeNode);

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

		// lsiten for changes to Node A selection
		if (mSelectedNodeA != mTrackEditorController.selectedNodeA()) {
			mSelectedNodeA = mTrackEditorController.selectedNodeA();

			if (mSelectedNodeA != null) {
				mSelectedNodeALabel.value(mSelectedNodeA.uid);
				mDeleteNodeNode.isEnabled(true);
				mMoveNodeButton.isEnabled(true);
			} else {
				mSelectedNodeALabel.value(0);
				mDeleteNodeNode.isEnabled(false);
				mMoveNodeButton.isEnabled(false);
			}
		}

		if (mSelectedNodeB != mTrackEditorController.selectedNodeB()) {
			mSelectedNodeB = mTrackEditorController.selectedNodeB();

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
		final var lIsLayerActive = mEditorBrushController.isLayerActive(mEditorLayer);

		switch (entryUid) {
		case BUTTON_NEW_NODE:
			if (lIsLayerActive == false)
				return;

			final var lCursorPositionX = mEditorBrushController.cursorWorldX();
			final var lCursorPositionY = mEditorBrushController.cursorWorldY();

			mEditorTrackRenderer.handleNodeCreation(lCursorPositionX, lCursorPositionY);

			break;

		case BUTTON_MOVE_NODE:
			if (lIsLayerActive == false)
				return;

			if (mEditorBrushController.setAction(TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_NODE, "Moving Node", layerOwnerHashCode())) {
				mEditorTrackRenderer.setMoveSelectedNode();
			}

			break;

		case BUTTON_DELETE_NODE:
			if (lIsLayerActive == false)
				return;

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
		return mEditorTrackRenderer.hashCode();
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
