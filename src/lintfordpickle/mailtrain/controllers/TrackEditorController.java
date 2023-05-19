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
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;

public class TrackEditorController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static List<RailTrackSegment> mTempEdgeList = new ArrayList<>();

	public static final String CONTROLLER_NAME = "Track Editor Controller";

	public static final int CONTROLLER_EDITOR_ACTION_MOVE_NODE = 100;

	public static final int CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_1 = 101;
	public static final int CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_2 = 102;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameSceneInstance mGameScene;

	private RailTrackInstance mTrack;

	private RailTrackNode mSelectedNodeA;
	private RailTrackNode mSelectedNodeB;

	private int mLogicalUpdateCounter;

	private int mActiveEdgeLocalIndex = -1; // index is local to the selected node
	private int mAuxiliaryEdgeLocalIndex = -1; // index is local to the selected node

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int activeEdgeLocalIndex() {
		return mActiveEdgeLocalIndex;
	}

	public int auxiliaryEdgeLocalIndex() {
		return mAuxiliaryEdgeLocalIndex;
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

	public RailTrackSegment getSelectedEdge() {
		if (mActiveEdgeLocalIndex == -1)
			return null;

		if (mSelectedNodeA == null)
			return null;

		return mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);
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
		return mTrack != null;
	}

	public RailTrackInstance track() {
		return mTrack;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackEditorController(ControllerManager pControllerManager, GameSceneInstance gameScene, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mGameScene = gameScene;
		mTrack = mGameScene.trackManager().track();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void deleteNode(RailTrackNode pNode) {
		if (pNode == null) {
			return;
		}
		mTempEdgeList.clear();

		final var lEdgeCount = pNode.numberConnectedEdges();
		for (int i = 0; i < lEdgeCount; i++) {
			mTempEdgeList.add(pNode.getEdgeByIndex(i));
		}
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = mTempEdgeList.get(i);
			deleteEdge(lEdge);
		}
		mTrack.nodes().remove(pNode);
	}

	private void deleteEdge(RailTrackSegment pEdge) {
		if (pEdge == null)
			return;
		final int lNodeCount = mTrack.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrack.nodes().get(i);
			if (lNode.getEdgeByUid(pEdge.uid) != null) {
				lNode.removeEdgeByUid(pEdge.uid);
			}
		}
		RailTrackSegment lEdgeToDelete = null;
		final int lEdgeCount = mTrack.edges().size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = mTrack.edges().get(i);
			if (lEdge.nodeAUid == pEdge.nodeAUid || lEdge.nodeAUid == pEdge.nodeBUid) {
				if (lEdge.nodeBUid == pEdge.nodeAUid || lEdge.nodeBUid == pEdge.nodeBUid) {
					lEdgeToDelete = lEdge;
				}
			}
		}
		mTrack.edges().remove(lEdgeToDelete);
		pEdge = null;
	}

	private RailTrackSegment getCommonEdge(final int pUidA, final int pUidB) {
		final var lNodeA = mTrack.getNodeByUid(pUidA);
		final var lNodeB = mTrack.getNodeByUid(pUidB);

		if (lNodeA == null || lNodeB == null)
			return null;

		final int lEdgeCountNodeA = lNodeA.numberConnectedEdges();
		for (int i = 0; i < lEdgeCountNodeA; i++) {
			final var lOtherNodeUid = lNodeA.getEdgeByIndex(i).getOtherNodeUid(pUidA);
			if (pUidB == lOtherNodeUid) {
				return lNodeA.getEdgeByIndex(i);
			}
		}
		return null;
	}

	private void createEdgeBetween(int pNodeAUid, int pNodeBUid) {
		if (pNodeAUid == pNodeBUid)
			return; // Nope

		boolean lEdgeExists = mTrack.edgeExistsBetween(pNodeAUid, pNodeBUid);
		if (lEdgeExists)
			return; // nope

		final var lNodeA = mTrack.getNodeByUid(pNodeAUid);
		final var lNodeB = mTrack.getNodeByUid(pNodeBUid);

		if (lNodeA == null || lNodeB == null)
			return; // nope

		final var lNewEdgeAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lNodeA.y, lNodeB.x - lNodeA.x));

		final var lNewEdge = new RailTrackSegment(mTrack, mTrack.getNewEdgeUid(), pNodeAUid, pNodeBUid, lNewEdgeAngle);
		mTrack.edges().add(lNewEdge);

		final int lNodeAEdgeCount = lNodeA.numberConnectedEdges();
		for (int i = 0; i < lNodeAEdgeCount; i++) {
			final var lOldEdge = lNodeA.getEdgeByIndex(i);
			if (lOldEdge == null)
				return;
			if (!lOldEdge.allowedEdgeConections.contains((Integer) lNewEdge.uid)) {
				lOldEdge.allowedEdgeConections.add((Integer) lNewEdge.uid);
			}
			if (!lNewEdge.allowedEdgeConections.contains((Integer) lOldEdge.uid)) {
				lNewEdge.allowedEdgeConections.add((Integer) lOldEdge.uid);
			}
		}
		final int lNodeBEdgeCount = lNodeB.numberConnectedEdges();
		for (int i = 0; i < lNodeBEdgeCount; i++) {
			final var lOldEdge = lNodeB.getEdgeByIndex(i);
			if (lOldEdge == null)
				continue;
			if (!lOldEdge.allowedEdgeConections.contains((Integer) lNewEdge.uid)) {
				lOldEdge.allowedEdgeConections.add((Integer) lNewEdge.uid);
			}
			if (!lNewEdge.allowedEdgeConections.contains((Integer) lOldEdge.uid)) {
				lNewEdge.allowedEdgeConections.add((Integer) lOldEdge.uid);
			}
		}
		lNewEdge.control0X = lNodeA.x;
		lNewEdge.control0Y = lNodeA.y;

		lNewEdge.control1X = lNodeB.x;
		lNewEdge.control1Y = lNodeB.y;

		lNewEdge.edgeLengthInMeters = mTrack.getEdgeLength(lNewEdge);

		lNodeA.addEdgeToNode(lNewEdge);
		lNodeB.addEdgeToNode(lNewEdge);
	}

	// ---------------------------------------------
	// Track Manipulation Methods
	// ---------------------------------------------

	public void togglePrimaryEdgeType() {
		if (mSelectedNodeA == null)
			return;

		if (mActiveEdgeLocalIndex < 0)
			return;

		final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);

		if (lManipulateEdge == null)
			return;

		if (lManipulateEdge.edgeType == RailTrackSegment.EDGE_TYPE_STRAIGHT)
			lManipulateEdge.edgeType = RailTrackSegment.EDGE_TYPE_CURVE;
		else
			lManipulateEdge.edgeType = RailTrackSegment.EDGE_TYPE_STRAIGHT;

		updateUpdateCounter();
	}

	public int getPrimaryEdgeType() {
		if (mSelectedNodeA == null)
			return RailTrackSegment.EDGE_TYPE_NONE;

		if (mActiveEdgeLocalIndex < 0)
			return RailTrackSegment.EDGE_TYPE_NONE;

		final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);

		if (lManipulateEdge == null)
			return RailTrackSegment.EDGE_TYPE_NONE;

		return lManipulateEdge.edgeType;
	}

	// ---------------------------------------------
	// Editor API
	// ---------------------------------------------

	public void prevLocalPrimaryEdge() {
		if (mSelectedNodeA != null) {
			mActiveEdgeLocalIndex--;
			if (mActiveEdgeLocalIndex < 0)
				mActiveEdgeLocalIndex = mSelectedNodeA.numberConnectedEdges() - 1;
		}
	}

	public void nextLocalPrimaryEdge() {
		if (mSelectedNodeA != null) {
			mActiveEdgeLocalIndex++;
			if (mActiveEdgeLocalIndex >= mSelectedNodeA.numberConnectedEdges())
				mActiveEdgeLocalIndex = 0;
		}
	}

	public void prevLocalSecondaryEdge() {
		if (mSelectedNodeA != null) {
			mAuxiliaryEdgeLocalIndex--;
			if (mAuxiliaryEdgeLocalIndex < 0)
				mAuxiliaryEdgeLocalIndex = mSelectedNodeA.numberConnectedEdges() - 1;
		}
	}

	public void nextLocalSecondaryEdge() {
		if (mSelectedNodeA != null) {
			mAuxiliaryEdgeLocalIndex++;
			if (mAuxiliaryEdgeLocalIndex >= mSelectedNodeA.numberConnectedEdges())
				mAuxiliaryEdgeLocalIndex = 0;
		}
	}

	public void toggleSelectedEdgesTravelledAllowed() {
		// Toggle allowed / not allowed (ref. edgeSelected)
		if (mActiveEdgeLocalIndex != -1 && mAuxiliaryEdgeLocalIndex != -1) {
			final var lSelectedEdge = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);
			final var lConstrainedEdge = mSelectedNodeA.getEdgeByIndex(mAuxiliaryEdgeLocalIndex);
			if (lSelectedEdge.allowedEdgeConections.contains(lConstrainedEdge.uid)) {
				lSelectedEdge.allowedEdgeConections.remove((Integer) lConstrainedEdge.uid);
				Debug.debugManager().logger().i(getClass().getSimpleName(), "Added travel constraint to edge uid " + lConstrainedEdge.uid + " from edge uid " + lSelectedEdge.uid);
			} else {
				lSelectedEdge.allowedEdgeConections.add(lConstrainedEdge.uid);
				Debug.debugManager().logger().i(getClass().getSimpleName(), "Added travelled allowed to edge uid " + lConstrainedEdge.uid + " from edge uid " + lSelectedEdge.uid);
			}
		}
	}

	public RailTrackNode getNodeAtGridLocation(float worldPositionX, float worldPositionY) {
		final int lNodeCount = mTrack.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrack.nodes().get(i);
			if (Vector2f.dst(worldPositionX, worldPositionY, lNode.x, lNode.y) < 8.f) {
				return lNode;
			}
		}
		return null;
	}

	public boolean createNodeAt(float lMouseGridPositionX, float lMouseGridPositionY) {
		// If there is already a node here
		final var lNodeUnderMouse = getNodeAtGridLocation(lMouseGridPositionX, lMouseGridPositionY);
		if (lNodeUnderMouse != null) {
			if (mSelectedNodeB != null && lNodeUnderMouse != mSelectedNodeB) {
				// There maybe a chance to connect nodes A and the just selected one together
				createEdgeBetween(mSelectedNodeB.uid, lNodeUnderMouse.uid);

				updateUpdateCounter();

				return true;
			}
			return false;
		}

		// if no nodes selected, then no problem
		boolean lCreationAllowed = (mSelectedNodeA == null && mSelectedNodeB == null);
		if (mSelectedNodeB != null) {
			int localGridX = (int) mSelectedNodeB.x;
			int localGridY = (int) mSelectedNodeB.y;

			int newRelX = (int) lMouseGridPositionX - localGridX;
			int newRelY = (int) lMouseGridPositionY - localGridY;
			if (mSelectedNodeB.x == lMouseGridPositionX) { // Horizontal
				lCreationAllowed = true;
			} else if (mSelectedNodeB.y == lMouseGridPositionY) { // vertical
				lCreationAllowed = true;
			} else if (Math.abs(newRelX) == Math.abs(newRelY)) { // diagonal
				lCreationAllowed = true;
			}

			//if (!lCreationAllowed)
			//	return true;

			final var lNewNode = new RailTrackNode(mTrack.getNewNodeUid());
			lNewNode.x = lMouseGridPositionX;
			lNewNode.y = lMouseGridPositionY;

			mTrack.nodes().add(lNewNode);

			createEdgeBetween(lNewNode.uid, mSelectedNodeB.uid);
			mSelectedNodeB = lNewNode;

			updateUpdateCounter();

			return true;

		} else if (mSelectedNodeA != null) {
			int localGridX = (int) mSelectedNodeA.x;
			int localGridY = (int) mSelectedNodeA.y;

			int newRelX = (int) lMouseGridPositionX - localGridX;
			int newRelY = (int) lMouseGridPositionY - localGridY;
			if (mSelectedNodeA.x == lMouseGridPositionX) { // Horizontal
				lCreationAllowed = true;
			} else if (mSelectedNodeA.y == lMouseGridPositionY) { // vertical
				lCreationAllowed = true;
			} else if (Math.abs(newRelX) == Math.abs(newRelY)) { // diagonal
				lCreationAllowed = true;
			}
			//				if (!lCreationAllowed)
			//					return true;

			final var lNewNode = new RailTrackNode(mTrack.getNewNodeUid());
			lNewNode.x = lMouseGridPositionX;
			lNewNode.y = lMouseGridPositionY;

			mTrack.nodes().add(lNewNode);

			createEdgeBetween(lNewNode.uid, mSelectedNodeA.uid);
			mSelectedNodeB = lNewNode;

			updateUpdateCounter();

			return true;
		} else {
			final var lNewNode = new RailTrackNode(mTrack.getNewNodeUid());
			lNewNode.x = lMouseGridPositionX;
			lNewNode.y = lMouseGridPositionY;

			updateUpdateCounter();

			mTrack.nodes().add(lNewNode);
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

			final int lEdgeCount = mSelectedNodeA.numberConnectedEdges();
			for (int j = 0; j < lEdgeCount; j++) {
				final var lEdge = mSelectedNodeA.getEdgeByIndex(j);
				if (lEdge == null) {
					System.out.println(mSelectedNodeA.uid + ":: NULL EDGE");
				} else {
					System.out.println(mSelectedNodeA.uid + ":: Edge found Index : " + j + " Uid: " + lEdge.uid);
				}
			}

			if (mSelectedNodeA.numberConnectedEdges() > 0)
				mActiveEdgeLocalIndex = 0;
			else
				mActiveEdgeLocalIndex = -1;

			if (mSelectedNodeA.numberConnectedEdges() > 1)
				mAuxiliaryEdgeLocalIndex = 1;
			else
				mAuxiliaryEdgeLocalIndex = -1;

			return true;

		} else if (mSelectedNodeB == null) {
			mSelectedNodeB = lSelectedNode;
			return true;
		}

		return false;
	}

	public boolean deleteSelectedNodes() {
		if (mSelectedNodeA != null && mSelectedNodeB != null) {
			// remove edges from between these nodes
			final var lCommonEdge = getCommonEdge(mSelectedNodeA.uid, mSelectedNodeB.uid);
			if (lCommonEdge != null) {
				deleteEdge(lCommonEdge);

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

	public boolean handleTrackEdgeCreation() {
		if (mSelectedNodeA != null && mSelectedNodeB != null) {
			createEdgeBetween(mSelectedNodeA.uid, mSelectedNodeB.uid);

		}
		updateUpdateCounter();

		return true;
	}

	public void moveSelectedANode(float worldX, float worldY) {
		if (mSelectedNodeA != null) {
			mSelectedNodeA.x = worldX;
			mSelectedNodeA.y = worldY;
		}

		updateUpdateCounter();
	}

	public void moveSelectedSegmentControlNode1(float worldX, float worldY) {
		if (mSelectedNodeA == null)
			return;

		if (mActiveEdgeLocalIndex != -1) {
			final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);

			if (lManipulateEdge == null)
				return;

			lManipulateEdge.control0X = worldX;
			lManipulateEdge.control0Y = worldY;

			lManipulateEdge.edgeLengthInMeters = mTrack.getEdgeLength(lManipulateEdge);
			updateUpdateCounter();
		}
	}

	public void moveSelectedSegmentControlNode2(float worldX, float worldY) {
		if (mSelectedNodeA == null)
			return;

		if (mActiveEdgeLocalIndex != -1) {
			final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(mActiveEdgeLocalIndex);

			if (lManipulateEdge == null)
				return;

			lManipulateEdge.control1X = worldX;
			lManipulateEdge.control1Y = worldY;

			lManipulateEdge.edgeLengthInMeters = mTrack.getEdgeLength(lManipulateEdge);
			updateUpdateCounter();
		}
	}

	// --- Junctions

	public void toggleSelectedEdgeJunction() {
		final var lSelectedNodeA = selectedNodeA();
		final var lSelectedEdge = getSelectedEdge();

		if (lSelectedNodeA.numberConnectedEdges() == 3) {
			final int lEdgeUid0 = mSelectedNodeA.getOtherEdgeConnectionUids(lSelectedEdge.uid);
			final int lEdgeUid1 = mSelectedNodeA.getOtherEdgeConnectionUids2(lSelectedEdge.uid);
			lSelectedEdge.trackJunction.init(mSelectedNodeA.uid, lEdgeUid0, lEdgeUid1);

		} else {
			lSelectedEdge.trackJunction.reset();
		}
	}

	public void toggleSelectedJunctionLeftRightEdges() {
		final var lSelectedEdge = getSelectedEdge();

		if (lSelectedEdge == null)
			return;

		if (lSelectedEdge.trackJunction != null && lSelectedEdge.trackJunction.isSignalActive) {
			int lLeftEdgeUid = lSelectedEdge.trackJunction.leftEdgeUid;
			lSelectedEdge.trackJunction.leftEdgeUid = lSelectedEdge.trackJunction.rightEdgeUid;
			lSelectedEdge.trackJunction.rightEdgeUid = lLeftEdgeUid;

		} else {
			lSelectedEdge.trackJunction.reset();

		}
	}

	public void setSelectedJunctionLamp(float worldX, float worldY) {
		final var lSelectedEdge = getSelectedEdge();

		if (lSelectedEdge == null)
			return;

		if (lSelectedEdge.trackJunction == null || lSelectedEdge.trackJunction.isSignalActive == false)
			return;

		lSelectedEdge.trackJunction.signalLampOffsetX = worldX - mSelectedNodeA.x;
		lSelectedEdge.trackJunction.signalLampOffsetY = worldY - mSelectedNodeA.y;

		updateUpdateCounter();
	}

	public void setSelectedJunctionBox(float worldX, float worldY) {
		final var lSelectedEdge = getSelectedEdge();

		if (lSelectedEdge == null)
			return;

		if (lSelectedEdge.trackJunction == null || lSelectedEdge.trackJunction.isSignalActive == false)
			return;

		lSelectedEdge.trackJunction.signalBoxOffsetX = worldX - mSelectedNodeA.x;
		lSelectedEdge.trackJunction.signalBoxOffsetY = worldY - mSelectedNodeA.y;

		updateUpdateCounter();
	}

	// ---------------------------------------------
	// Input Callbacks
	// ---------------------------------------------

	public void clearTrackEditor(LintfordCore pCore) {
		mTrack.nodes().clear();
		mTrack.edges().clear();
		mTrack.reset();

		mTrack.trackSignalSegments.clearInstances();

		mTempEdgeList.clear();

		mSelectedNodeA = null;
		mSelectedNodeB = null;

		resetUpdateCounter();
	}

}
