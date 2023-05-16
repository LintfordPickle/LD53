package lintfordpickle.mailtrain.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.input.keyboard.IBufferedTextInputCallback;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.screenmanager.ScreenManager;

public class TrackEditorController extends BaseController implements IInputProcessor, IBufferedTextInputCallback {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static List<RailTrackSegment> mTempEdgeList = new ArrayList<>();

	public static final String CONTROLLER_NAME = "Track Editor Controller";

	private ScreenManager mScreenManager;
	private GameSceneInstance mGameScene;

	private RailTrackInstance mTrack;

	// Selected Items from Editor
	public RailTrackNode mSelectedNodeA;
	public RailTrackNode mSelectedNodeB;

	public int activeEdgeLocalIndex = -1; // index is local to the selected node
	public int auxiliaryEdgeLocalIndex = -1; // index is local to the selected node

	private boolean mLeftMouseDownTimed;
	private boolean mLeftMouseDown;
	private float mMouseWorldPositionX;
	private float mMouseWorldPositionY;
	private float mMouseGridPositionX;
	private float mMouseGridPositionY;

	private int mLogicalUpdateCounter;

	// TODO: refactor out (or just delete)
	private boolean mIsCapturedTextInput;
	private boolean mIsCapturingName;
	public RailTrackSegment mSelectedEdge;
	private final StringBuilder mInputField = new StringBuilder();
	//

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

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

	private float mLeftMouseClickCooldown;

	public boolean isCoolDownElapsed() {
		return mLeftMouseClickCooldown < 0.f;
	}

	public void resetCoolDownTimer() {
		mLeftMouseClickCooldown = 250.f;
	}

	@Override
	public boolean isInitialized() {
		return mTrack != null;
	}

	public RailTrackInstance track() {
		return mTrack;
	}

	public float worldToGrid(final float pWorldCoord) {
		return RailTrackInstance.worldToGrid(pWorldCoord, TrackController.GRID_SIZE_DEPRECATED);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackEditorController(ControllerManager pControllerManager, ScreenManager pScreenManager, GameSceneInstance gameScene, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mScreenManager = pScreenManager;
		mGameScene = gameScene;
		mTrack = mGameScene.trackManager().track();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().mouse().isMouseOverThisComponent(hashCode()) == false) {
			return false;
		}

		// Scenery controller
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT)) {
			return false;
		}

		mLeftMouseDown = pCore.input().mouse().isMouseLeftButtonDown();
		mLeftMouseDownTimed = pCore.input().mouse().isMouseLeftButtonDownTimed(this);

		mMouseWorldPositionX = pCore.gameCamera().getMouseWorldSpaceX();
		mMouseWorldPositionY = pCore.gameCamera().getMouseWorldSpaceY();
		mMouseGridPositionX = worldToGrid(mMouseWorldPositionX);
		mMouseGridPositionY = worldToGrid(mMouseWorldPositionY);

		if (handleEdgeTypeManipulation(pCore))
			return true;

		if (handleMoveTrackNode(pCore))
			return true;

		if (handleSignalBoxes(pCore))
			return true;

		if (handleClearEditor(pCore))
			return true;

		if (handleNodeSelection(pCore))
			return true;

		if (handleEdgeSpecialCases(pCore))
			return true;

		if (handleSignalBlockControls(pCore))
			return true;

		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mLeftMouseClickCooldown -= pCore.appTime().elapsedTimeMilli();
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

		mScreenManager.toastManager().addMessage("", "Track segment created", 1500);
	}

	// ---------------------------------------------
	// Track Manipulation Methods
	// ---------------------------------------------

	public void togglePrimaryEdgeType() {
		if (mSelectedNodeA == null)
			return;

		if (activeEdgeLocalIndex < 0)
			return;

		final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

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

		if (activeEdgeLocalIndex < 0)
			return RailTrackSegment.EDGE_TYPE_NONE;

		final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

		if (lManipulateEdge == null)
			return RailTrackSegment.EDGE_TYPE_NONE;

		return lManipulateEdge.edgeType;
	}

	private boolean handleEdgeTypeManipulation(LintfordCore pCore) {
		if (mSelectedNodeA == null)
			return false;

		if (activeEdgeLocalIndex != -1) {
			final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

			if (lManipulateEdge == null)
				return false;

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_1) && mLeftMouseDown) {
				lManipulateEdge.control0X = mMouseGridPositionX;
				lManipulateEdge.control0Y = mMouseGridPositionY;

				lManipulateEdge.edgeLengthInMeters = mTrack.getEdgeLength(lManipulateEdge);
				updateUpdateCounter();

				return true;
			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_2) && mLeftMouseDown) {
				lManipulateEdge.control1X = mMouseGridPositionX;
				lManipulateEdge.control1Y = mMouseGridPositionY;

				lManipulateEdge.edgeLengthInMeters = mTrack.getEdgeLength(lManipulateEdge);
				updateUpdateCounter();

				return true;
			}
		}
		return false;
	}

	public boolean handleNodeCreation(float worldPositionX, float worldPositionY) {
		final var lMouseGridPositionX = worldToGrid(worldPositionX);
		final var lMouseGridPositionY = worldToGrid(worldPositionY);

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

	private boolean handleNodeSelection(LintfordCore pCore) {
		if (mLeftMouseDownTimed) {
			if (mSelectedNodeA != null && mSelectedNodeB != null) {
				mSelectedNodeA = null;
				mSelectedNodeB = null;
				return true;

			}
			final RailTrackNode lSelectedNode = getNodeAtGridLocation(mMouseWorldPositionX, mMouseWorldPositionY);
			if (lSelectedNode == null) {
				mSelectedNodeA = null;
				mSelectedNodeB = null;
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
					activeEdgeLocalIndex = 0;
				else
					activeEdgeLocalIndex = -1;

				if (mSelectedNodeA.numberConnectedEdges() > 1)
					auxiliaryEdgeLocalIndex = 1;
				else
					auxiliaryEdgeLocalIndex = -1;

				return true;

			} else if (mSelectedNodeB == null) {
				mSelectedNodeB = lSelectedNode;
				return true;

			}
		}
		return false;
	}

	private RailTrackNode getNodeAtGridLocation(final float pWorldPositionX, float pWorldPositionY) {
		final int lNodeCount = mTrack.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrack.nodes().get(i);
			if (Vector2f.dst(pWorldPositionX, pWorldPositionY, lNode.x, lNode.y) < 8.f) {
				return lNode;
			}
		}
		return null;
	}

	private boolean handleSignalBoxes(LintfordCore pCore) {
		if (mSelectedNodeA != null && activeEdgeLocalIndex != -1) {
			final var lEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_U, this)) {
				if (mSelectedNodeA.numberConnectedEdges() == 3) {
					final int lEdgeUid0 = mSelectedNodeA.getOtherEdgeConnectionUids(lEdge.uid);
					final int lEdgeUid1 = mSelectedNodeA.getOtherEdgeConnectionUids2(lEdge.uid);
					lEdge.trackJunction.init(mSelectedNodeA.uid, lEdgeUid0, lEdgeUid1);
					System.out.println("Signal initialized");
				} else {
					System.out.println("resetting signal on node");
					lEdge.trackJunction.reset();
				}
			} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_Z, this)) { // toggle left / right edges on signal
				if (lEdge != null && lEdge.trackJunction != null && lEdge.trackJunction.isSignalActive) {
					int lLeftEdgeUid = lEdge.trackJunction.leftEdgeUid;
					lEdge.trackJunction.leftEdgeUid = lEdge.trackJunction.rightEdgeUid;
					lEdge.trackJunction.rightEdgeUid = lLeftEdgeUid;

				} else {
					lEdge.trackJunction.reset();
				}
			}

			// Change offset position of the lamp and box
			if (lEdge != null && lEdge.trackJunction != null && lEdge.trackJunction.isSignalActive) {
				if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_I)) {
					if (pCore.input().mouse().isMouseLeftButtonDown()) {
						lEdge.trackJunction.signalLampOffsetX = mMouseWorldPositionX - mSelectedNodeA.x;
						lEdge.trackJunction.signalLampOffsetY = mMouseWorldPositionY - mSelectedNodeA.y;

						updateUpdateCounter();

						return true;

					}
				}

				if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_B)) {
					if (pCore.input().mouse().isMouseLeftButtonDown()) {
						lEdge.trackJunction.signalBoxOffsetX = mMouseWorldPositionX - mSelectedNodeA.x;
						lEdge.trackJunction.signalBoxOffsetY = mMouseWorldPositionY - mSelectedNodeA.y;

						updateUpdateCounter();

						return true;

					}
				}
			}
		}
		return false;
	}

	private boolean handleMoveTrackNode(LintfordCore pCore) {
		boolean isLeftMouseDown = pCore.input().mouse().isMouseLeftButtonDown();

		final float lMouseWorldSpaceX = pCore.gameCamera().getMouseWorldSpaceX();
		final float lMouseWorldSpaceY = pCore.gameCamera().getMouseWorldSpaceY();

		final float lGridPositionX = worldToGrid(lMouseWorldSpaceX);
		final float lGridPositionY = worldToGrid(lMouseWorldSpaceY);
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_M) && isLeftMouseDown) {
			if (mSelectedNodeA != null) {
				mSelectedNodeA.x = lGridPositionX;
				mSelectedNodeA.y = lGridPositionY;

			}
			if (mSelectedNodeA != null) {
				// Re-calculte edge lengths
				final int lNumEdges = mSelectedNodeA.numberConnectedEdges();
				for (int i = 0; i < lNumEdges; i++) {
					final var lEdge = mSelectedNodeA.getEdgeByIndex(i);
					lEdge.edgeLengthInMeters = mTrack.getEdgeLength(lEdge);

				}
			}
			updateUpdateCounter();

			return true;
		}
		return false;
	}

	private boolean handleClearEditor(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DELETE) && pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			clearTrackEditor(pCore);

			mScreenManager.toastManager().addMessage("Editor", "Cleared all track nodes", 1500);

			resetUpdateCounter();

			return true;
		}
		return false;
	}

	private boolean handleEdgeSpecialCases(LintfordCore pCore) {
		if (mSelectedNodeA == null || activeEdgeLocalIndex == -1)
			return false;

		final var lActiveEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

		//		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_2, this)) {
		//			System.out.println("Setting edge name");
		//			mScreenManager.toastManager().addMessage("Editor", "Setting edge name", 1500);
		//			mSelectedEdge = lActiveEdge;
		//			mIsCapturedTextInput = true;
		//			mIsCapturingName = true;
		//			mInputField.setLength(0);
		//			pCore.input().keyboard().startBufferedTextCapture(this);
		//
		//			return true;
		//		}

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_3, this)) {
			System.out.println("Setting edge special name");
			mScreenManager.toastManager().addMessage("Editor", "Setting edge special name", 1500);
			mSelectedEdge = lActiveEdge;
			mInputField.setLength(0);
			mIsCapturedTextInput = true;
			mIsCapturingName = false;
			pCore.input().keyboard().startBufferedTextCapture(this);

			return true;
		}

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_4, this)) {
			System.out.println("Setting player spawn edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_SPAWN);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_5, this)) {
			System.out.println("Setting player exit edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_EXIT);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_6, this)) {
			System.out.println("Setting map edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_EDGE);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_7, this)) {
			System.out.println("Setting station");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_STATION);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_8, this)) {
			System.out.println("Setting enemy spawn edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_ENEMY_SPAWN);
			return true;
		}

		// Reset the edge's special flags
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_0, this)) {
			System.out.println("Resetting edge's special flags");
			lActiveEdge.setEdgeBitFlag(0);
			return true;
		}
		return false;
	}

	private boolean handleSignalBlockControls(LintfordCore pCore) {
		// Need to get the active edge
		if (activeEdgeLocalIndex == -1)
			return false;

		// Need to get the active node (A)
		if (mSelectedNodeA == null)
			return false;

		final var lActiveEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

		// Check for signal creation
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_Q, this)) {
			lActiveEdge.addTrackSignal(mTrack, /* RandomNumbers.random(0.f, 1.f) */ .5f, lActiveEdge.getOtherNodeUid(mSelectedNodeA.uid));

			mTrack.areSignalsDirty = true;

			mScreenManager.toastManager().addMessage("", "Added signal to track segment " + lActiveEdge.uid, 1500);

		}

		// TODO: Editor: slide signal blocks through [0..1]
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_R)) {

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_T)) {

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) {

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_E)) {

		}
		return false;
	}

	// ---------------------------------------------
	// Editor API
	// ---------------------------------------------

	public void prevLocalPrimaryEdge() {
		if (mSelectedNodeA != null) {
			activeEdgeLocalIndex--;
			if (activeEdgeLocalIndex < 0)
				activeEdgeLocalIndex = mSelectedNodeA.numberConnectedEdges() - 1;
		}
	}

	public void nextLocalPrimaryEdge() {
		if (mSelectedNodeA != null) {
			activeEdgeLocalIndex++;
			if (activeEdgeLocalIndex >= mSelectedNodeA.numberConnectedEdges())
				activeEdgeLocalIndex = 0;
		}
	}

	public void prevLocalSecondaryEdge() {
		if (mSelectedNodeA != null) {
			auxiliaryEdgeLocalIndex--;
			if (auxiliaryEdgeLocalIndex < 0)
				auxiliaryEdgeLocalIndex = mSelectedNodeA.numberConnectedEdges() - 1;
		}
	}

	public void nextLocalSecondaryEdge() {
		if (mSelectedNodeA != null) {
			auxiliaryEdgeLocalIndex++;
			if (auxiliaryEdgeLocalIndex >= mSelectedNodeA.numberConnectedEdges())
				auxiliaryEdgeLocalIndex = 0;
		}
	}

	public void toggleSelectedEdgesTravelledAllowed() {
		// Toggle allowed / not allowed (ref. edgeSelected)
		if (activeEdgeLocalIndex != -1 && auxiliaryEdgeLocalIndex != -1) {
			final var lSelectedEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);
			final var lConstrainedEdge = mSelectedNodeA.getEdgeByIndex(auxiliaryEdgeLocalIndex);
			if (lSelectedEdge.allowedEdgeConections.contains(lConstrainedEdge.uid)) {
				lSelectedEdge.allowedEdgeConections.remove((Integer) lConstrainedEdge.uid);
				Debug.debugManager().logger().i(getClass().getSimpleName(), "Added travel constraint to edge uid " + lConstrainedEdge.uid + " from edge uid " + lSelectedEdge.uid);
			} else {
				lSelectedEdge.allowedEdgeConections.add(lConstrainedEdge.uid);
				Debug.debugManager().logger().i(getClass().getSimpleName(), "Added travelled allowed to edge uid " + lConstrainedEdge.uid + " from edge uid " + lSelectedEdge.uid);
			}
		}
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

				mSelectedNodeA = null;
				mSelectedNodeB = null;

			}
		} else if (mSelectedNodeA != null) {
			deleteNode(mSelectedNodeA);
			mSelectedNodeA = null;

		} else if (mSelectedNodeB != null) {
			deleteNode(mSelectedNodeB);
			mSelectedNodeB = null;

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

	// ---------------------------------------------
	// Input Callbacks
	// ---------------------------------------------

	private void clearTrackEditor(LintfordCore pCore) {
		mTrack.nodes().clear();
		mTrack.edges().clear();
		mTrack.reset();

		mTrack.trackSignalSegments.clearInstances();

		mTempEdgeList.clear();

		mSelectedNodeA = null;
		mSelectedNodeB = null;
	}

	@Override
	public boolean allowKeyboardInput() {
		return true;
	}

	@Override
	public boolean allowGamepadInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		return true;
	}

	// ---------------------------------------------
	// Buffered Text Input
	// ---------------------------------------------

	public void onDeselection() {

	}

	@Override
	public void onKeyPressed(int codePoint) {
		if (mIsCapturingName)
			mSelectedEdge.segmentName = mInputField.toString();
		else
			mSelectedEdge.specialName = mInputField.toString();
	}

	@Override
	public boolean onEscapePressed() {
		if (mIsCapturingName)
			mSelectedEdge.segmentName = mInputField.toString();
		else
			mSelectedEdge.specialName = mInputField.toString();

		return false;
	}

	@Override
	public boolean onEnterPressed() {
		if (mIsCapturingName)
			mSelectedEdge.segmentName = mInputField.toString();
		else
			mSelectedEdge.specialName = mInputField.toString();

		return false;
	}

	@Override
	public StringBuilder getStringBuilder() {
		return mInputField;
	}

	@Override
	public boolean getEnterFinishesInput() {
		return true;
	}

	@Override
	public boolean getEscapeFinishesInput() {
		return true;
	}

	@Override
	public void captureStopped() {
		IBufferedTextInputCallback.super.captureStopped();
		mIsCapturedTextInput = false;

	}

}
