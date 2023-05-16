package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiLabelledInt;

public class TrackInfoPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String PANEL_NAME = "Track Info";
	
	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;

	private UiLabelledInt mNumberNodes;
	private UiLabelledInt mNumberSegments;
	private UiLabelledInt mNumberSignals;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public TrackInfoPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, PANEL_NAME, entityGroupdUid);

		mShowActiveLayerButton = false;
		mShowShowLayerButton = false;

		mRenderPanelTitle = true;
		mPanelTitle = PANEL_NAME;

		mNumberNodes = new UiLabelledInt(parentWindow, "Number Nodes");
		mNumberSegments = new UiLabelledInt(parentWindow, "Number Segment");
		mNumberSignals = new UiLabelledInt(parentWindow, "Number Signal");

		addWidget(mNumberNodes);
		addWidget(mNumberSegments);
		addWidget(mNumberSignals);

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

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		mNumberNodes.value(mTrackEditorController.track().getNumberTrackNodes());
		mNumberSegments.value(mTrackEditorController.track().getNumberTrackEdges());
		mNumberSignals.value(mTrackEditorController.track().trackSignalSegments.numInstances());
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
