package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.EditorTrackController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiLabelledInt;
import net.lintford.library.renderers.windows.components.UifSlider;

public class SignalsPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Signals";

	private static final int BUTTON_CREATE_SIGNAL_A = 50;
	private static final int BUTTON_DELETE_SIGNAL_A = 51;
	private static final int BUTTON_CREATE_SIGNAL_B = 55;
	private static final int BUTTON_DELETE_SIGNAL_B = 56;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private RailTrackInstance mTrack;
	private EditorTrackController mTrackEditorController;
	private EditorTrackRenderer mEditorTrackRenderer;

	private UiLabelledInt mSelectedSegmentLabel;
	private UiLabelledInt mSignalsADestinationNodeLabel;
	private UiLabelledInt mSignalsBDestinationNodeLabel;

	private UifSlider mDistanceA;
	private UifSlider mDistanceB;

	private UiButton mCreateSignalA;
	private UiButton mCreateSignalB;
	private UiButton mDeleteSignalA;
	private UiButton mDeleteSignalB;

	// Data
	private RailTrackNode mSelectedNodeA;
	private RailTrackNode mSelectedNodeB;
	private RailTrackSegment mSelectedRailTrackSegment;

	private int mActiveSegmentLocalIndex;

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

		mSelectedSegmentLabel = new UiLabelledInt(parentWindow);
		mSelectedSegmentLabel.labelText("Segment: ");
		mSelectedSegmentLabel.value(-1);

		mSignalsADestinationNodeLabel = new UiLabelledInt(parentWindow);
		mSignalsADestinationNodeLabel.labelText("A Destination: ");
		mSignalsADestinationNodeLabel.value(2);

		mSignalsBDestinationNodeLabel = new UiLabelledInt(parentWindow);
		mSignalsBDestinationNodeLabel.labelText("B Destination: ");
		mSignalsBDestinationNodeLabel.value(3);

		mDistanceA = new UifSlider(parentWindow);
		mDistanceA.sliderLabel("Dist A:");
		mDistanceA.setMinMax(0.f, 1.f);

		mDistanceB = new UifSlider(parentWindow);
		mDistanceB.sliderLabel("Dist B:");
		mDistanceB.setMinMax(0.f, 1.f);

		mCreateSignalA = new UiButton(parentWindow, "Create A");
		mCreateSignalA.setClickListener(this, BUTTON_CREATE_SIGNAL_A);
		mCreateSignalB = new UiButton(parentWindow, "Create B");
		mCreateSignalB.setClickListener(this, BUTTON_CREATE_SIGNAL_B);

		mDeleteSignalA = new UiButton(parentWindow, "Delete A");
		mDeleteSignalA.setClickListener(this, BUTTON_DELETE_SIGNAL_A);
		mDeleteSignalB = new UiButton(parentWindow, "Delete B");
		mDeleteSignalB.setClickListener(this, BUTTON_DELETE_SIGNAL_B);

		addWidget(mSelectedSegmentLabel);
		addWidget(mSignalsADestinationNodeLabel);
		addWidget(mCreateSignalA);
		addWidget(mDistanceA);
		addWidget(mDeleteSignalA);
		addWidget(mSignalsBDestinationNodeLabel);
		addWidget(mCreateSignalB);
		addWidget(mDistanceB);
		addWidget(mDeleteSignalB);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();
		mTrackEditorController = (EditorTrackController) lControllerManager.getControllerByNameRequired(EditorTrackController.CONTROLLER_NAME, mEntityGroupUid);
		mTrack = mTrackEditorController.track();

		final var lRendererManager = mParentWindow.rendererManager();
		mEditorTrackRenderer = (EditorTrackRenderer) lRendererManager.getRenderer(EditorTrackRenderer.RENDERER_NAME);
		mEditorTrackRenderer.drawEditorSignals(isLayerVisible());

		mCreateSignalA.isEnabled(false);
		mDeleteSignalA.isEnabled(false);
		mCreateSignalB.isEnabled(false);
		mDeleteSignalB.isEnabled(false);

	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		var lPrimarySegmentDeselected = false;

		if (mTrackEditorController.selectedNodeA() != mSelectedNodeA) {
			mSelectedNodeA = mTrackEditorController.selectedNodeA();

			if (mSelectedNodeA == null)
				lPrimarySegmentDeselected = true;
			else
				mActiveSegmentLocalIndex = -1; // force re-select of segment on node change
		}

		if (mTrackEditorController.selectedNodeB() != mSelectedNodeB) {
			mSelectedNodeB = mTrackEditorController.selectedNodeB();

			if (mSelectedNodeB == null)
				lPrimarySegmentDeselected = true;
			else
				mActiveSegmentLocalIndex = -1; // force re-select of segment on node change

		}

		if (mSelectedNodeA != null || mSelectedNodeB != null) {
			if (mActiveSegmentLocalIndex != mTrackEditorController.editorPrimarySegmentLocalIndex()) {
				mActiveSegmentLocalIndex = mTrackEditorController.editorPrimarySegmentLocalIndex();

				final var lSelectedNode = mSelectedNodeA != null ? mSelectedNodeA : mSelectedNodeB;
				final var lSelectedSegmentUid = lSelectedNode.trackSwitch.connectedSegmentUidFromIndex(mActiveSegmentLocalIndex);

				mSelectedSegmentLabel.value(lSelectedSegmentUid);

				mSelectedRailTrackSegment = mTrack.getSegmentByUid(lSelectedSegmentUid);
				if (mSelectedRailTrackSegment == null) {
					mCreateSignalA.isEnabled(false);
					mDeleteSignalA.isEnabled(false);

					mCreateSignalB.isEnabled(false);
					mDeleteSignalB.isEnabled(false);

					mSignalsADestinationNodeLabel.value(-1);
					mSignalsBDestinationNodeLabel.value(-1);

				} else {
					final var lSignalsA = mSelectedRailTrackSegment.signalsA;
					mSignalsADestinationNodeLabel.value(lSignalsA.destinationNodeUid());
					if (lSignalsA.isAuxiliarySignalSegmentActive()) {
						mCreateSignalA.isEnabled(false);
						mDeleteSignalA.isEnabled(true);

						final var lPrimarySignalSegment = lSignalsA.primarySignalSegment();
						final float roundedValueA = round2(lPrimarySignalSegment.length(), 2);
						mDistanceA.currentValue(roundedValueA);

					} else {
						mCreateSignalA.isEnabled(true);
						mDeleteSignalA.isEnabled(false);
						mDistanceA.currentValue(1.f);
					}

					final var lSignalsB = mSelectedRailTrackSegment.signalsB;
					mSignalsBDestinationNodeLabel.value(lSignalsB.destinationNodeUid());
					if (lSignalsB.isAuxiliarySignalSegmentActive()) {
						mCreateSignalB.isEnabled(false);
						mDeleteSignalB.isEnabled(true);

						final var lPrimarySignalSegment = lSignalsB.primarySignalSegment();
						final float roundedValueA = round2(lPrimarySignalSegment.length(), 2);
						mDistanceB.currentValue(roundedValueA);
					} else {
						mCreateSignalB.isEnabled(true);
						mDeleteSignalB.isEnabled(false);

						mDistanceB.currentValue(1.f);
					}
				}
			}
		}

		if (mSelectedRailTrackSegment != null) {
			final var lSignalsA = mSelectedRailTrackSegment.signalsA;
			final var lPrimarySignalSegmentA = lSignalsA.primarySignalSegment();

			final float roundedValueA = round2(mDistanceA.currentValue(), 2);

			if (lPrimarySignalSegmentA.length() != roundedValueA) {
				lPrimarySignalSegmentA.updateSignalSegmentLength(roundedValueA);
				lSignalsA.updateSignalLengths();
			}

			final var lSignalsB = mSelectedRailTrackSegment.signalsB;
			final var lPrimarySignalSegmentB = lSignalsB.primarySignalSegment();

			final float roundedValueB = round2(mDistanceB.currentValue(), 2);

			if (lPrimarySignalSegmentB.length() != roundedValueB) {
				lPrimarySignalSegmentB.updateSignalSegmentLength(roundedValueB);
				lSignalsB.updateSignalLengths();
			}
		}

		if (lPrimarySegmentDeselected) {
			mActiveSegmentLocalIndex = RailTrackSegment.NO_SEGMENT;
			mCreateSignalA.isEnabled(false);
			mDeleteSignalA.isEnabled(false);
			mCreateSignalB.isEnabled(false);
			mDeleteSignalB.isEnabled(false);

			mSelectedSegmentLabel.value(-1);
			mSelectedRailTrackSegment = null;

			mSignalsADestinationNodeLabel.value(-1);
			mSignalsBDestinationNodeLabel.value(-1);

			mActiveSegmentLocalIndex = -1;
		}
	}

	public static float round2(float number, int scale) {
		int pow = 10;
		for (int i = 1; i < scale; i++)
			pow *= 10;
		float tmp = number * pow;
		return ((float) ((int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp))) / pow;
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
		case BUTTON_CREATE_SIGNAL_A: {
			if (lIsLayerActive == false)
				return;

			if (mSelectedRailTrackSegment == null)
				return;

			final var lSignalsDestinationNodeUid = mSelectedRailTrackSegment.signalsA.destinationNodeUid();
			mSelectedRailTrackSegment.setSegmentSignalAtDistance(0.5f, lSignalsDestinationNodeUid);
		}
			return;

		case BUTTON_CREATE_SIGNAL_B: {
			if (lIsLayerActive == false)
				return;

			if (mSelectedRailTrackSegment == null)
				return;

			final var lSignalsDestinationNodeUid = mSelectedRailTrackSegment.signalsB.destinationNodeUid();
			mSelectedRailTrackSegment.setSegmentSignalAtDistance(0.5f, lSignalsDestinationNodeUid);
			return;
		}

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
