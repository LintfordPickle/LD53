package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiHorizontalEntryGroup;

public class JunctionsPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Junctions";

	private static final int BUTTON_TOGGLE_JUNCTION = 10;
	private static final int BUTTON_TOGGLE_JUNCTION_ROUTE = 15;
	private static final int BUTTON_MOVE_BOX = 20;
	private static final int BUTTON_MOVE_LAMP = 21;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;
	private EditorTrackRenderer mEditorTrackRenderer;

	private UiButton mToggleJunctionButton;
	private UiButton mToggleMainRoute;

	private UiHorizontalEntryGroup mPlacementGroup;
	private UiButton mPlaceBox;
	private UiButton mPlacePost;

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

		mToggleJunctionButton = new UiButton(parentWindow);
		mToggleJunctionButton.buttonLabel("Toggle Junction");
		mToggleJunctionButton.setClickListener(this, BUTTON_TOGGLE_JUNCTION);

		mToggleMainRoute = new UiButton(parentWindow);
		mToggleMainRoute.buttonLabel("Toggle Main Path");
		mToggleMainRoute.setClickListener(this, BUTTON_TOGGLE_JUNCTION_ROUTE);

		mPlacementGroup = new UiHorizontalEntryGroup(parentWindow);

		mPlaceBox = new UiButton(parentWindow);
		mPlaceBox.buttonLabel("Move Box");
		mPlaceBox.setClickListener(this, BUTTON_MOVE_BOX);

		mPlacePost = new UiButton(parentWindow);
		mPlacePost.buttonLabel("Move Lamp");
		mPlacePost.setClickListener(this, BUTTON_MOVE_LAMP);

		mPlacementGroup.widgets().add(mPlaceBox);
		mPlacementGroup.widgets().add(mPlacePost);

		addWidget(mToggleJunctionButton);
		addWidget(mToggleMainRoute);
		addWidget(mPlacementGroup);

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
		mEditorTrackRenderer.drawEditorJunctions(isLayerVisible());

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
		case BUTTON_TOGGLE_JUNCTION:
			mTrackEditorController.toggleSelectedEdgeJunction();
			break;

		case BUTTON_TOGGLE_JUNCTION_ROUTE:
			mTrackEditorController.toggleSelectedJunctionLeftRightEdges();
			break;

		case BUTTON_MOVE_BOX:
			if (mEditorBrushController.brush().isActionSet() == false)
				mEditorBrushController.setAction(TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_JUNCTION_BOX, "Moving Junction Box", lTrackHashCode);

			break;

		case BUTTON_MOVE_LAMP:
			if (mEditorBrushController.brush().isActionSet() == false)
				mEditorBrushController.setAction(TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_JUNCTION_POST, "Moving Junction Post", lTrackHashCode);
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
