package lintfordpickle.mailtrain.controllers;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;

public class EditorTrackController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static List<RailTrackSegment> mTempSegmentList = new ArrayList<>();

	public static final String CONTROLLER_NAME = "Track Editor Controller";

	public static final int CONTROLLER_EDITOR_ACTION_MOVE_NODE = 100;

	public static final int CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_1 = 101;
	public static final int CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_2 = 102;
	public static final int CONTROLLER_EDITOR_ACTION_MOVE_JUNCTION_BOX = 103;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameSceneInstance mGameScene;

	private RailTrackInstance mTrackInstace;

	private RailTrackNode mSelectedNodeA;
	private RailTrackNode mSelectedNodeB;

	private int mLogicalUpdateCounter;

	// These are used for functionality within the controller, like marking segments for allowed lists etc.
	private int mEditorPrimarySegmentLocalIndex = -1; // index is local to the selected node
	private int mEditorSecondarySegmentLocalIndex = -1; // index is local to the selected node

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int editorPrimarySegmentLocalIndex() {
		return mEditorPrimarySegmentLocalIndex;
	}

	public int editorSecondarySegmentLocalIndex() {
		return mEditorSecondarySegmentLocalIndex;
	}

	public RailTrackNode selectedNodeA() {
		return mSelectedNodeA;
	}

	public void selectedNodeA(RailTrackNode selectedNode) {
		mSelectedNodeA = selectedNode;
	}

	public RailTrackNode selectedNodeB() {
		return mSelectedNodeB;
	}

	public void selectedNodeB(RailTrackNode selectedNode) {
		mSelectedNodeB = selectedNode;
	}

	public RailTrackSegment getSelectedSegment() {
		if (mEditorPrimarySegmentLocalIndex == -1)
			return null;

		if (mSelectedNodeA == null)
			return null;

		return mSelectedNodeA.trackSwitch.getConnectedSegmentByIndex(mEditorPrimarySegmentLocalIndex);
	}

	private void updateUpdateCounter() {
		mLogicalUpdateCounter++;
		System.out.println("Track structure changed (" + mLogicalUpdateCounter + ")");
	}

	private void resetUpdateCounter() {
		mLogicalUpdateCounter = 0;
	}

	/** Everytime there is a change to thge structure of the network, this counter will be incremented */
	public int logicalUpdateCounter() {
		return mLogicalUpdateCounter;
	}

	@Override
	public boolean isInitialized() {
		return mTrackInstace != null;
	}

	public RailTrackInstance track() {
		return mTrackInstace;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorTrackController(ControllerManager pControllerManager, GameSceneInstance gameScene, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mGameScene = gameScene;
		mTrackInstace = mGameScene.trackManager().track();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void deleteNode(RailTrackNode node) {
		if (node == null) {
			return;
		}
		mTempSegmentList.clear();

		final var lSegmentCount = node.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lSegmentCount; i++) {
			mTempSegmentList.add(node.trackSwitch.getConnectedSegmentByIndex(i));
		}
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = mTempSegmentList.get(i);
			deleteSegment(lSegment);
		}

		mTrackInstace.nodes().remove(node);
	}

	private void deleteSegment(RailTrackSegment segment) {
		if (segment == null)
			return;
		final int lNodeCount = mTrackInstace.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrackInstace.nodes().get(i);
			if (lNode.trackSwitch.getConnectedSegmentByUid(segment.uid) != null) {
				lNode.removeSegmentByUid(segment.uid);
			}
		}
		RailTrackSegment lSegmentToDelete = null;
		final int lSegmentCount = mTrackInstace.segments().size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = mTrackInstace.segments().get(i);
			if (lSegment.nodeAUid == segment.nodeAUid || lSegment.nodeAUid == segment.nodeBUid) {
				if (lSegment.nodeBUid == segment.nodeAUid || lSegment.nodeBUid == segment.nodeBUid) {
					lSegmentToDelete = lSegment;
				}
			}
		}
		mTrackInstace.segments().remove(lSegmentToDelete);
		segment = null;
	}

	private RailTrackSegment getCommonSegment(final int nodeAUid, final int nodeBUid) {
		final var lNodeA = mTrackInstace.getNodeByUid(nodeAUid);
		final var lNodeB = mTrackInstace.getNodeByUid(nodeBUid);

		if (lNodeA == null || lNodeB == null)
			return null;

		final int lSegmentCountNodeA = lNodeA.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lSegmentCountNodeA; i++) {
			final var lOtherNodeUid = lNodeA.trackSwitch.getConnectedSegmentByIndex(i).getOtherNodeUid(nodeAUid);
			if (nodeBUid == lOtherNodeUid) {
				return lNodeA.trackSwitch.getConnectedSegmentByIndex(i);
			}
		}
		return null;
	}

	private void createSegmentBetween(int nodeAUid, int nodeBUid) {
		if (nodeAUid == nodeBUid)
			return; // Nope

		boolean lSegmentExists = mTrackInstace.doesSegmentExistsBetween(nodeAUid, nodeBUid);
		if (lSegmentExists)
			return; // nope

		final var lNodeA = mTrackInstace.getNodeByUid(nodeAUid);
		final var lNodeB = mTrackInstace.getNodeByUid(nodeBUid);

		if (lNodeA == null || lNodeB == null)
			return; // nope

		final var lNewSegmentAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lNodeA.y, lNodeB.x - lNodeA.x));
		final var lNewSegment = new RailTrackSegment(mTrackInstace, mTrackInstace.getNewSegmentUid(), nodeAUid, nodeBUid, lNewSegmentAngle);
		mTrackInstace.segments().add(lNewSegment);

		lNewSegment.control0X = lNodeA.x;
		lNewSegment.control0Y = lNodeA.y;

		lNewSegment.control1X = lNodeB.x;
		lNewSegment.control1Y = lNodeB.y;

		lNewSegment.segmentLengthInMeters = mTrackInstace.getSegmentLength(lNewSegment);

		lNodeA.addSegmentToNode(lNewSegment);
		lNodeB.addSegmentToNode(lNewSegment);

		// TODO: Update allowed segment conections
	}

	// ---------------------------------------------
	// Track Manipulation Methods
	// ---------------------------------------------

	public void togglePrimarySegmentType() {
		if (mSelectedNodeA == null)
			return;

		if (mEditorPrimarySegmentLocalIndex < 0)
			return;

		final var lManipulateSegment = mSelectedNodeA.trackSwitch.getConnectedSegmentByIndex(mEditorPrimarySegmentLocalIndex);

		if (lManipulateSegment == null)
			return;

		if (lManipulateSegment.segmentType == RailTrackSegment.SEGMENT_TYPE_STRAIGHT)
			lManipulateSegment.segmentType = RailTrackSegment.SEGMENT_TYPE_CURVE;
		else
			lManipulateSegment.segmentType = RailTrackSegment.SEGMENT_TYPE_STRAIGHT;

		updateUpdateCounter();
	}

	public int getPrimarySegmentType() {
		if (mSelectedNodeA == null)
			return RailTrackSegment.SEGMENT_TYPE_NONE;

		if (mEditorPrimarySegmentLocalIndex < 0)
			return RailTrackSegment.SEGMENT_TYPE_NONE;

		final var lManipulateSegment = mSelectedNodeA.trackSwitch.getConnectedSegmentByIndex(mEditorPrimarySegmentLocalIndex);

		if (lManipulateSegment == null)
			return RailTrackSegment.SEGMENT_TYPE_NONE;

		return lManipulateSegment.segmentType;
	}

	// ---------------------------------------------
	// Editor API
	// ---------------------------------------------

	public void prevLocalPrimarySegment() {
		if (mSelectedNodeA != null) {
			mEditorPrimarySegmentLocalIndex--;
			if (mEditorPrimarySegmentLocalIndex < 0)
				mEditorPrimarySegmentLocalIndex = mSelectedNodeA.trackSwitch.numberConnectedSegments() - 1;
		}
	}

	public void nextLocalPrimarySegment() {
		if (mSelectedNodeA != null) {
			mEditorPrimarySegmentLocalIndex++;
			if (mEditorPrimarySegmentLocalIndex >= mSelectedNodeA.trackSwitch.numberConnectedSegments())
				mEditorPrimarySegmentLocalIndex = 0;
		}
	}

	public void prevLocalSecondarySegment() {
		if (mSelectedNodeA != null) {
			mEditorSecondarySegmentLocalIndex--;
			if (mEditorSecondarySegmentLocalIndex < 0)
				mEditorSecondarySegmentLocalIndex = mSelectedNodeA.trackSwitch.numberConnectedSegments() - 1;
		}
	}

	public void nextLocalSecondarySegment() {
		if (mSelectedNodeA != null) {
			mEditorSecondarySegmentLocalIndex++;
			if (mEditorSecondarySegmentLocalIndex >= mSelectedNodeA.trackSwitch.numberConnectedSegments())
				mEditorSecondarySegmentLocalIndex = 0;
		}
	}

	public void toggleSelectedSegmentTravelledAllowed() {
		// TODO: Unimplemented method
	}

	public RailTrackNode getNodeAtGridLocation(float worldPositionX, float worldPositionY) {
		final int lNodeCount = mTrackInstace.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrackInstace.nodes().get(i);
			if (Vector2f.dst(worldPositionX, worldPositionY, lNode.x, lNode.y) < 8.f) {
				return lNode;
			}
		}
		return null;
	}

	public boolean createNodeAt(float mouseGridPositionX, float mouseGridPositionY) {
		// If there is already a node here
		final var lNodeUnderMouse = getNodeAtGridLocation(mouseGridPositionX, mouseGridPositionY);
		if (lNodeUnderMouse != null) {
			if (mSelectedNodeB != null && lNodeUnderMouse != mSelectedNodeB) {
				// There maybe a chance to connect nodes A and the just selected one together
				createSegmentBetween(mSelectedNodeB.uid, lNodeUnderMouse.uid);

				updateUpdateCounter();

				return true;
			}
			return false;
		}

		final var lSelectedNode = mSelectedNodeB != null ? mSelectedNodeB : mSelectedNodeA;
		if (lSelectedNode != null) {
			final var lNewNode = new RailTrackNode(mTrackInstace.getNewNodeUid());
			lNewNode.init(mouseGridPositionX, mouseGridPositionY);

			mTrackInstace.nodes().add(lNewNode);

			createSegmentBetween(lNewNode.uid, lSelectedNode.uid);
			mSelectedNodeB = lNewNode;

			updateUpdateCounter();

			return true;
		} else {
			final var lNewNode = new RailTrackNode(mTrackInstace.getNewNodeUid());
			lNewNode.init(mouseGridPositionX, mouseGridPositionY);

			updateUpdateCounter();

			mTrackInstace.nodes().add(lNewNode);
			mSelectedNodeB = lNewNode;
		}

		return true;
	}

	public void clearNodeSelection() {
		mSelectedNodeA = null;
		mSelectedNodeB = null;
	}

	public boolean handleNodeSelection(float worldX, float worldY) {
		if (mSelectedNodeA != null && mSelectedNodeB != null) {
			selectedNodeA(null);
			selectedNodeB(null);
			return true;
		}

		final RailTrackNode lSelectedNode = getNodeAtGridLocation(worldX, worldY);
		if (lSelectedNode == null) {
			selectedNodeA(null);
			selectedNodeB(null);
			return true;
		}

		if (mSelectedNodeA == null) {
			mSelectedNodeA = lSelectedNode;

			if (mSelectedNodeA.trackSwitch.numberConnectedSegments() > 0)
				mEditorPrimarySegmentLocalIndex = 0;
			else
				mEditorPrimarySegmentLocalIndex = -1;

			if (mSelectedNodeA.trackSwitch.numberConnectedSegments() > 1)
				mEditorSecondarySegmentLocalIndex = 1;
			else
				mEditorSecondarySegmentLocalIndex = -1;

			return true;

		} else if (mSelectedNodeB == null) {
			mSelectedNodeB = lSelectedNode;
			return true;
		}

		return false;
	}

	public boolean deleteSelectedNodes() {
		if (mSelectedNodeA != null && mSelectedNodeB != null) {
			// remove segments from between these nodes
			final var lCommonSegment = getCommonSegment(mSelectedNodeA.uid, mSelectedNodeB.uid);
			if (lCommonSegment != null) {
				deleteSegment(lCommonSegment);

			} else {
				// delete both nodes
				deleteNode(mSelectedNodeA);
				deleteNode(mSelectedNodeB);

				selectedNodeA(null);
				selectedNodeB(null);
			}
		} else if (mSelectedNodeA != null) {
			deleteNode(mSelectedNodeA);
			selectedNodeA(null);
		} else if (mSelectedNodeB != null) {
			deleteNode(mSelectedNodeB);
			selectedNodeB(null);
		}
		updateUpdateCounter();

		return true;
	}

	public boolean handleTrackSegmentCreation() {
		if (mSelectedNodeA != null && mSelectedNodeB != null) {
			createSegmentBetween(mSelectedNodeA.uid, mSelectedNodeB.uid);

		}
		updateUpdateCounter();

		return true;
	}

	public void moveSelectedANode(float worldX, float worldY) {
		if (mSelectedNodeA != null) {
			mSelectedNodeA.x = worldX;
			mSelectedNodeA.y = worldY;

			final int lNumConnectedSegments = mSelectedNodeA.trackSwitch.numberConnectedSegments();
			for (int i = 0; i < lNumConnectedSegments; i++) {
				final var lSegment = mSelectedNodeA.trackSwitch.connectedSegments().get(i);
				lSegment.segmentLengthInMeters = mTrackInstace.getSegmentLength(lSegment);
			}

		}

		updateUpdateCounter();
	}

	public void moveSelectedSegmentJunctionBoxTo(float worldX, float worldY) {
		final var lSelectedNode = mSelectedNodeA != null ? mSelectedNodeA : mSelectedNodeB;
		if (lSelectedNode == null)
			return;

		lSelectedNode.trackSwitch.signalBoxWorldX = worldX;
		lSelectedNode.trackSwitch.signalBoxWorldY = worldY;
	}

	public void moveSelectedSegmentControlNode1To(float worldX, float worldY) {
		final var lSelectedSegment = getSelectedSegment();
		if (lSelectedSegment == null)
			return;

		lSelectedSegment.control0X = worldX;
		lSelectedSegment.control0Y = worldY;

		lSelectedSegment.segmentLengthInMeters = mTrackInstace.getSegmentLength(lSelectedSegment);
		updateUpdateCounter();
	}

	public void moveSelectedSegmentControlNode2To(float worldX, float worldY) {
		final var lSelectedSegment = getSelectedSegment();
		if (lSelectedSegment == null)
			return;

		lSelectedSegment.control1X = worldX;
		lSelectedSegment.control1Y = worldY;

		lSelectedSegment.segmentLengthInMeters = mTrackInstace.getSegmentLength(lSelectedSegment);
		updateUpdateCounter();
	}

	// --- Switches

	public void toggleSelectedSwitchMainLine() {
		final var lSelectedNode = mSelectedNodeA != null ? mSelectedNodeA : mSelectedNodeB;
		if (lSelectedNode == null)
			return;

		lSelectedNode.trackSwitch.cycleSwitchMainSegmentForward();
	}

	public void toggleSelectedSwitchAuxiliaryLine() {
		final var lSelectedNode = mSelectedNodeA != null ? mSelectedNodeA : mSelectedNodeB;
		if (lSelectedNode == null)
			return;

		lSelectedNode.trackSwitch.cycleSwitchAuxSegmentsForward();
	}

	// ---------------------------------------------
	// Input Callbacks
	// ---------------------------------------------

	public void clearTrackEditor(LintfordCore pCore) {
		mTrackInstace.nodes().clear();
		mTrackInstace.segments().clear();
		mTrackInstace.reset();

		mTrackInstace.trackSignalSegments.clearInstances();

		mTempSegmentList.clear();

		mSelectedNodeA = null;
		mSelectedNodeB = null;

		resetUpdateCounter();
	}

}
