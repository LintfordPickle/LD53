package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.core.input.keyboard.IUiInputKeyPressCallback;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiHorizontalEntryGroup;
import net.lintford.library.renderers.windows.components.UiHorizontalEntryGroup.SPACING_TYPE;
import net.lintford.library.renderers.windows.components.UiInputText;
import net.lintford.library.renderers.windows.components.UiLabelledFloat;
import net.lintford.library.renderers.windows.components.UiLabelledInt;
import net.lintford.library.renderers.windows.components.UiLabelledString;

public class SegmentPanel extends UiPanel implements IUiInputKeyPressCallback {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Segments";

	private static final int BUTTON_CREATE_SEGMENT = 10;
	private static final int BUTTON_DELETE_SEGMENT = 11;
	private static final int BUTTON_TOGGLE_ALLOWED_SEGMENT = 12;

	private static final int BUTTON_PRIMARY_PREV_LOCAL = 20;
	private static final int BUTTON_PRIMARY_NEXT_LOCAL = 21;
	private static final int BUTTON_PRIMARY_TOGGLE_TYPE = 22;

	private static final int BUTTON_PRIMARY_CONTROL_NODE_1_MOVE = 24;
	private static final int BUTTON_PRIMARY_CONTROL_NODE_2_MOVE = 25;

	private static final int BUTTON_SECONDARY_PREV_LOCAL = 30;
	private static final int BUTTON_SECONDARY_NEXT_LOCAL = 31;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private RailTrackInstance mTrack;
	private TrackEditorController mTrackEditorController;
	private EditorTrackRenderer mEditorTrackRenderer;

	private UiButton mNewSegmentButton;
	private UiButton mDeleteSegmentNode;
	private UiInputText mSegmentName;

	private UiHorizontalEntryGroup mPHorzGroup;
	private UiLabelledString mPriLabel;
	private UiButton mPPrevSegmentButton;
	private UiLabelledInt mSegmentPrimaryId;
	private UiButton mPNextSegmentButton;
	private UiLabelledFloat mSegmentDistance;
	private UiButton mPrimaryEdgeType;

	private UiHorizontalEntryGroup mControlNodeGroup;
	private UiButton mMoveControlNodeA;
	private UiButton mMoveControlNodeB;

	private UiHorizontalEntryGroup mSHorzGroup;
	private UiLabelledString mSecLabel;
	private UiButton mSPrevSegmentButton;
	private UiLabelledInt mSegmentSecondaryId;
	private UiButton mSNextSegmentButton;

	private UiHorizontalEntryGroup mTravelHorzGroup;
	private UiLabelledString mSegmentTravelAllowed;
	private UiButton mToggleTravelAllowed;

	// Data
	private RailTrackNode mSelectedNodeA;
	private RailTrackNode mSelectedNodeB;

	private int mActiveEdgeLocalIndex;
	private int mAuxiliaryEdgeLocalIndex;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SegmentPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track;

		mNewSegmentButton = new UiButton(parentWindow);
		mNewSegmentButton.buttonLabel("New");
		mNewSegmentButton.setClickListener(this, BUTTON_CREATE_SEGMENT);
		mNewSegmentButton.isEnabled(false);

		mDeleteSegmentNode = new UiButton(parentWindow);
		mDeleteSegmentNode.buttonLabel("Delete");
		mDeleteSegmentNode.setClickListener(this, BUTTON_DELETE_SEGMENT);
		mDeleteSegmentNode.isEnabled(false);

		{//  Primary Segment
			mPriLabel = new UiLabelledString(parentWindow);
			mPriLabel.labelText("Primary: ");
			mPriLabel.layoutWeight(0.6f);

			mPPrevSegmentButton = new UiButton(parentWindow);
			mPPrevSegmentButton.buttonLabel("<");
			mPPrevSegmentButton.layoutWeight(0.1f);
			mPPrevSegmentButton.setClickListener(this, BUTTON_PRIMARY_PREV_LOCAL);

			mSegmentPrimaryId = new UiLabelledInt(parentWindow, "");
			mSegmentPrimaryId.layoutWeight(0.2f);

			mPNextSegmentButton = new UiButton(parentWindow);
			mPNextSegmentButton.buttonLabel(">");
			mPNextSegmentButton.layoutWeight(0.1f);
			mPNextSegmentButton.setClickListener(this, BUTTON_PRIMARY_NEXT_LOCAL);

			mPHorzGroup = new UiHorizontalEntryGroup(parentWindow);
			mPHorzGroup.widgets().add(mPriLabel);
			mPHorzGroup.widgets().add(mPPrevSegmentButton);
			mPHorzGroup.widgets().add(mSegmentPrimaryId);
			mPHorzGroup.widgets().add(mPNextSegmentButton);
			mPHorzGroup.spacingType(SPACING_TYPE.weighted);

			mSegmentName = new UiInputText(parentWindow);
			mSegmentName.isEnabled(false);

			mPrimaryEdgeType = new UiButton(parentWindow);
			mPrimaryEdgeType.buttonLabel("-");
			mPrimaryEdgeType.setClickListener(this, BUTTON_PRIMARY_TOGGLE_TYPE);

			mControlNodeGroup = new UiHorizontalEntryGroup(parentWindow);
			mMoveControlNodeA = new UiButton(parentWindow, "Move A");
			mMoveControlNodeA.isEnabled(false);
			mMoveControlNodeA.setClickListener(this, BUTTON_PRIMARY_CONTROL_NODE_1_MOVE);
			mMoveControlNodeB = new UiButton(parentWindow, "Move B");
			mMoveControlNodeB.isEnabled(false);
			mMoveControlNodeB.setClickListener(this, BUTTON_PRIMARY_CONTROL_NODE_2_MOVE);

			mControlNodeGroup.widgets().add(mMoveControlNodeA);
			mControlNodeGroup.widgets().add(mMoveControlNodeB);

			mSegmentDistance = new UiLabelledFloat(parentWindow, "Length:");

		}

		{//  Auxiliary Segment selection
			mSecLabel = new UiLabelledString(parentWindow);
			mSecLabel.labelText("Sec: ");
			mSecLabel.layoutWeight(0.6f);

			mSPrevSegmentButton = new UiButton(parentWindow);
			mSPrevSegmentButton.buttonLabel("<");
			mSPrevSegmentButton.layoutWeight(0.1f);
			mSPrevSegmentButton.setClickListener(this, BUTTON_SECONDARY_PREV_LOCAL);

			mSegmentSecondaryId = new UiLabelledInt(parentWindow, "");
			mSegmentSecondaryId.layoutWeight(0.2f);

			mSNextSegmentButton = new UiButton(parentWindow);
			mSNextSegmentButton.buttonLabel(">");
			mSNextSegmentButton.layoutWeight(0.1f);
			mSNextSegmentButton.setClickListener(this, BUTTON_SECONDARY_NEXT_LOCAL);

			mSHorzGroup = new UiHorizontalEntryGroup(parentWindow);
			mSHorzGroup.widgets().add(mSecLabel);
			mSHorzGroup.widgets().add(mSPrevSegmentButton);
			mSHorzGroup.widgets().add(mSegmentSecondaryId);
			mSHorzGroup.widgets().add(mSNextSegmentButton);
			mSHorzGroup.spacingType(SPACING_TYPE.weighted);
		}

		{ // Travel
			mSegmentTravelAllowed = new UiLabelledString(parentWindow, "A - B");
			mToggleTravelAllowed = new UiButton(parentWindow);
			mToggleTravelAllowed.buttonLabel("Toggle Allowed");
			mToggleTravelAllowed.setClickListener(this, BUTTON_TOGGLE_ALLOWED_SEGMENT);
			mToggleTravelAllowed.isEnabled(false);

			mTravelHorzGroup = new UiHorizontalEntryGroup(parentWindow);
			mTravelHorzGroup.widgets().add(mSegmentTravelAllowed);
			mTravelHorzGroup.widgets().add(mToggleTravelAllowed);
			mTravelHorzGroup.spacingType(SPACING_TYPE.even);

		}

		addWidget(mNewSegmentButton);
		addWidget(mPHorzGroup);
		addWidget(mSegmentName);
		addWidget(mSegmentDistance);
		addWidget(mPrimaryEdgeType);
		addWidget(mControlNodeGroup);

		addWidget(mDeleteSegmentNode);
		addWidget(mSHorzGroup);
		addWidget(mTravelHorzGroup);

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
		mTrack = mTrackEditorController.track();

		final var lRendererManager = mParentWindow.rendererManager();
		mEditorTrackRenderer = (EditorTrackRenderer) lRendererManager.getRenderer(EditorTrackRenderer.RENDERER_NAME);
		mEditorTrackRenderer.drawEditorSegments(isLayerVisible());
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		var lNodeSelectionChanged = false;
		var lSegmentSelectionChanged = false;

		if (mTrackEditorController.selectedNodeA() != mSelectedNodeA) {
			mSelectedNodeA = mTrackEditorController.selectedNodeA();
			lNodeSelectionChanged = true;
		}
		if (mTrackEditorController.selectedNodeB() != mSelectedNodeB) {
			mSelectedNodeB = mTrackEditorController.selectedNodeB();
			lNodeSelectionChanged = true;
		}

		if (lNodeSelectionChanged) {
			if (mSelectedNodeA != null && mSelectedNodeB != null) {
				var edgeBetweenNodes = mTrack.getEdgeBetweenNodes(mSelectedNodeA.uid, mSelectedNodeB.uid);
				if (edgeBetweenNodes != null) {
					mNewSegmentButton.isEnabled(false);
					mDeleteSegmentNode.isEnabled(true);
				} else {
					mNewSegmentButton.isEnabled(true);
					mDeleteSegmentNode.isEnabled(false);
				}
			} else {
				mNewSegmentButton.isEnabled(false);
				mDeleteSegmentNode.isEnabled(false);
			}

			// update segment readings on node changes
			mActiveEdgeLocalIndex = mTrackEditorController.activeEdgeLocalIndex();
			mAuxiliaryEdgeLocalIndex = mTrackEditorController.auxiliaryEdgeLocalIndex();
			lSegmentSelectionChanged = true;

		} else {
			if (mActiveEdgeLocalIndex != mTrackEditorController.activeEdgeLocalIndex()) {
				mActiveEdgeLocalIndex = mTrackEditorController.activeEdgeLocalIndex();
				lSegmentSelectionChanged = true;
			}

			if (mAuxiliaryEdgeLocalIndex != mTrackEditorController.auxiliaryEdgeLocalIndex()) {
				mAuxiliaryEdgeLocalIndex = mTrackEditorController.auxiliaryEdgeLocalIndex();
				lSegmentSelectionChanged = true;
			}

		}

		if (lSegmentSelectionChanged) {
			var lActiveSegment = (RailTrackSegment) null; // mSelectedNodeA.getEdgeByUid(mActiveEdgeLocalIndex);
			var lAuxilierySegment = (RailTrackSegment) null; // mTrack.getEdgeByUid(mAuxiliaryEdgeLocalIndex);
			if (mSelectedNodeA != null) {
				if (mActiveEdgeLocalIndex >= 0)
					lActiveSegment = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);

				if (mAuxiliaryEdgeLocalIndex >= 0)
					lAuxilierySegment = mSelectedNodeA.getEdgeByIndex(mAuxiliaryEdgeLocalIndex);
			}

			if (lActiveSegment != null) {
				mSegmentName.inputString(lActiveSegment.segmentName);

				mSegmentPrimaryId.value(lActiveSegment.uid);
				mSegmentDistance.value(lActiveSegment.edgeLengthInMeters);
				mPrimaryEdgeType.buttonLabel(RailTrackSegment.getEdgeTypeName(lActiveSegment.edgeType));
			} else {
				mSegmentName.inputString("");
				mSegmentPrimaryId.value(-1);
				mSegmentDistance.value(0.0f);
				mPrimaryEdgeType.buttonLabel("-");
			}

			if (lAuxilierySegment != null) {
				mSegmentSecondaryId.value(lAuxilierySegment.uid);
			} else {
				mSegmentSecondaryId.value(-1);
			}

			if (lActiveSegment != null && lAuxilierySegment != null) {
				mSegmentTravelAllowed.labelText(lActiveSegment.uid + " -> " + lAuxilierySegment.uid);
				if (lActiveSegment == null || lAuxilierySegment == null || lActiveSegment.uid == lAuxilierySegment.uid) {
					mToggleTravelAllowed.buttonLabel("-");
					mToggleTravelAllowed.isEnabled(false);
				} else {

					var lIsTravelAllowed = lActiveSegment.allowedEdgeConections.contains(lAuxilierySegment.uid);

					if (lIsTravelAllowed) {
						mToggleTravelAllowed.buttonLabel("Allowed");
					} else {
						mToggleTravelAllowed.buttonLabel("Blocked");
					}

					mToggleTravelAllowed.isEnabled(true);
				}
			} else {
				mSegmentTravelAllowed.labelText("- -> -");
				mToggleTravelAllowed.buttonLabel("-");
				mToggleTravelAllowed.isEnabled(false);
			}
		}

		// Listen for changes to the properties on the active edge
		if (mSelectedNodeA != null) {
			final var lActiveSegment = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);
			if (lActiveSegment != null) {
				if (lActiveSegment.segmentName != null && lActiveSegment.segmentName.length() != mSegmentName.inputString().length()) {
					final var lInputString = mSegmentName.inputString().toString();
					if (lActiveSegment.segmentName.equals(lInputString) == false) {
						lActiveSegment.segmentName = lInputString;
					}
				}

				else if (lActiveSegment.segmentName == null && mSegmentName.inputString().length() > 0) {
					final var lInputString = mSegmentName.inputString().toString();
					lActiveSegment.segmentName = lInputString;
				}
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
		final var lTrackHashCode = mEditorTrackRenderer.hashCode();

		switch (entryUid) {
		case BUTTON_SHOW_LAYER:
			mEditorTrackRenderer.drawEditorSegments(isLayerVisible());
			return;

		case BUTTON_CREATE_SEGMENT:
			if (lIsLayerActive) {
				mTrackEditorController.handleTrackEdgeCreation();
			}
			return;

		case BUTTON_TOGGLE_ALLOWED_SEGMENT:
			if (lIsLayerActive) {
				mTrackEditorController.toggleSelectedEdgesTravelledAllowed();
				mActiveEdgeLocalIndex = -1; // force update of segments
			}
			break;

		case BUTTON_PRIMARY_PREV_LOCAL:
			if (lIsLayerActive) {
				mTrackEditorController.prevLocalPrimaryEdge();
			}
			break;

		case BUTTON_PRIMARY_NEXT_LOCAL:
			if (lIsLayerActive) {
				mTrackEditorController.nextLocalPrimaryEdge();
			}
			break;

		case BUTTON_SECONDARY_PREV_LOCAL:
			if (lIsLayerActive) {
				mTrackEditorController.prevLocalSecondaryEdge();
			}
			break;

		case BUTTON_SECONDARY_NEXT_LOCAL:
			if (lIsLayerActive) {
				mTrackEditorController.nextLocalSecondaryEdge();
			}
			break;

		case BUTTON_PRIMARY_TOGGLE_TYPE:
			if (lIsLayerActive) {
				mTrackEditorController.togglePrimaryEdgeType();

				final var lActiveEdge = mTrackEditorController.getSelectedEdge();
				if (lActiveEdge != null) {
					mPrimaryEdgeType.buttonLabel(RailTrackSegment.getEdgeTypeName(lActiveEdge.edgeType));

					final var lIsCurvedEdge = lActiveEdge.edgeType == RailTrackSegment.EDGE_TYPE_CURVE;
					if (lIsCurvedEdge) {
						mMoveControlNodeA.isEnabled(true);
						mMoveControlNodeB.isEnabled(true);
					} else {
						mMoveControlNodeA.isEnabled(false);
						mMoveControlNodeB.isEnabled(false);
					}

				}

			}
			break;

		case BUTTON_PRIMARY_CONTROL_NODE_1_MOVE:
			if (lIsLayerActive) {
				if (mEditorBrushController.setAction(TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_1, "Moving Control Node 1", lTrackHashCode)) {
					// TODO: Set mTrackEditorController.setMoveControl1Mode()
				}
			}
			break;

		case BUTTON_PRIMARY_CONTROL_NODE_2_MOVE:
			if (lIsLayerActive) {
				if (mEditorBrushController.setAction(TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_2, "Moving Control Node 2", lTrackHashCode)) {
					// TODO: Set mTrackEditorController.setMoveControl2Mode()
				}
			}
			break;
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

	@Override
	public void keyPressUpdate(int corePoint) {

	}
}
