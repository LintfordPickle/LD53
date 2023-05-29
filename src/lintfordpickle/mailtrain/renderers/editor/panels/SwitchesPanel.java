package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.EditorTrackController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiHorizontalEntryGroup;
import net.lintford.library.renderers.windows.components.UiLabelledInt;

public class SwitchesPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Switches";

	private static final int BUTTON_TOGGLE_MAINLINE = 10;
	private static final int BUTTON_TOGGLE_ACTIVE_AUX = 15;
	private static final int BUTTON_MOVE_BOX = 20;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private EditorTrackController mTrackEditorController;
	private EditorTrackRenderer mEditorTrackRenderer;

	private UiLabelledInt mMainSegmentUidLabel;
	private UiButton mToggleMainLine;
	private UiLabelledInt mActiveAuxSegmentUidLabel;
	private UiButton mToggleActiveAuxiliaryLine;

	private UiHorizontalEntryGroup mPlacementGroup;
	private UiButton mPlaceBox;

	// Data
	private RailTrackNode mSelectedNodeA;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SwitchesPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track;

		mMainSegmentUidLabel = new UiLabelledInt(parentWindow);
		mMainSegmentUidLabel.labelText("MainSegment Uid");
		mMainSegmentUidLabel.value(66);

		mToggleMainLine = new UiButton(parentWindow);
		mToggleMainLine.buttonLabel("Toggle Mainline");
		mToggleMainLine.setClickListener(this, BUTTON_TOGGLE_MAINLINE);

		mActiveAuxSegmentUidLabel = new UiLabelledInt(parentWindow);
		mActiveAuxSegmentUidLabel.labelText("Active Aux Uid:");
		mActiveAuxSegmentUidLabel.value(44);

		mToggleActiveAuxiliaryLine = new UiButton(parentWindow);
		mToggleActiveAuxiliaryLine.buttonLabel("Toggle Active Aux:");
		mToggleActiveAuxiliaryLine.setClickListener(this, BUTTON_TOGGLE_ACTIVE_AUX);

		mPlacementGroup = new UiHorizontalEntryGroup(parentWindow);

		mPlaceBox = new UiButton(parentWindow);
		mPlaceBox.buttonLabel("Move Box");
		mPlaceBox.setClickListener(this, BUTTON_MOVE_BOX);

		mPlacementGroup.widgets().add(mPlaceBox);

		addWidget(mMainSegmentUidLabel);
		addWidget(mToggleMainLine);
		addWidget(mActiveAuxSegmentUidLabel);
		addWidget(mToggleActiveAuxiliaryLine);
		addWidget(mPlacementGroup);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();
		mTrackEditorController = (EditorTrackController) lControllerManager.getControllerByNameRequired(EditorTrackController.CONTROLLER_NAME, mEntityGroupUid);

		final var lRendererManager = mParentWindow.rendererManager();
		mEditorTrackRenderer = (EditorTrackRenderer) lRendererManager.getRenderer(EditorTrackRenderer.RENDERER_NAME);
		mEditorTrackRenderer.drawEditorJunctions(isLayerVisible());

	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		var lNodeSelectionChanged = false;

		if (mTrackEditorController.selectedNodeA() != mSelectedNodeA) {
			mSelectedNodeA = mTrackEditorController.selectedNodeA();
			lNodeSelectionChanged = true;
		}

		if (lNodeSelectionChanged) {
			if (mSelectedNodeA != null) {

				mMainSegmentUidLabel.value(mSelectedNodeA.trackSwitch.mainSegmentUid());
				mActiveAuxSegmentUidLabel.value(mSelectedNodeA.trackSwitch.activeAuxiliarySegmentUid());

				mToggleMainLine.isEnabled(true);
				mToggleActiveAuxiliaryLine.isEnabled(true);
			} else {
				mMainSegmentUidLabel.value(-1);
				mActiveAuxSegmentUidLabel.value(-1);

				mToggleMainLine.isEnabled(false);
				mToggleActiveAuxiliaryLine.isEnabled(false);
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
		if (lIsLayerActive == false)
			return;

		final var lTrackHashCode = mEditorTrackRenderer.hashCode();

		switch (entryUid) {
		case BUTTON_TOGGLE_MAINLINE:
			mTrackEditorController.toggleSelectedSwitchMainLine();
			mSelectedNodeA = null;
			break;

		case BUTTON_TOGGLE_ACTIVE_AUX:
			mTrackEditorController.toggleSelectedSwitchAuxiliaryLine();
			mSelectedNodeA = null;
			break;

		case BUTTON_MOVE_BOX:
			if (mEditorBrushController.brush().isActionSet() == false)
				mEditorBrushController.setAction(EditorTrackController.CONTROLLER_EDITOR_ACTION_MOVE_JUNCTION_BOX, "Moving Junction Box", lTrackHashCode);

			break;
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
