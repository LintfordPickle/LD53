package lintfordpickle.mailtrain.controllers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import lintfordpickle.mailtrain.controllers.tracks.TrackIOController;
import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.track.TrackNode;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.keyboard.IBufferedTextInputCallback;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.screenmanager.ScreenManager;

public class TrackEditorController extends BaseController implements IInputProcessor, IBufferedTextInputCallback {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static List<TrackSegment> mTempEdgeList = new ArrayList<>();

	public static final String CONTROLLER_NAME = "Track Editor Controller";

	private ScreenManager mScreenManager;
	private Track mTrack;
	public TrackNode mSelectedNodeA;
	public TrackNode mSelectedNodeB;

	public int selectedSignalUid = 0;

	public int activeEdgeLocalIndex = -1; // index is local to the selected node
	public int auxilleryEdgeLocalIndex = -1; // index is local to the selected node

	private boolean mLeftMouseDownTimed;
	private boolean mLeftMouseDown;
	private float mMouseWorldPositionX;
	private float mMouseWorldPositionY;
	private float mMouseGridPositionX;
	private float mMouseGridPositionY;

	private int mLogicalUpdateCounter;

	// TODO: refactor out
	private boolean mIsCapturedTextInput;
	private boolean mIsCapturingName;
	public TrackSegment mSelectedEdge;
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

	public Track track() {
		return mTrack;
	}

	public float worldToGrid(final float pWorldCoord) {
		return Track.worldToGrid(pWorldCoord, mTrack.gridSizeInPixels);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackEditorController(ControllerManager pControllerManager, ScreenManager pScreenManager, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mScreenManager = pScreenManager;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean handleInput(LintfordCore pCore) {
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

		// TODO: Toggle node is exit
		// --->

		if (handleSignalBoxes(pCore))
			return true;

		if (handleClearEditor(pCore))
			return true;

		if (handleDeleteTrackNode(pCore))
			return true;

		if (handleTrackConstrainSelection(pCore))
			return true;

		if (handleNodeCreation(pCore))
			return true;

		if (handleNodeSelection(pCore))
			return true;

		if (handleTrackEdgeCreation(pCore))
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

	private void deleteNode(TrackNode pNode) {
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

	private void deleteEdge(TrackSegment pEdge) {
		if (pEdge == null)
			return;
		final int lNodeCount = mTrack.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrack.nodes().get(i);
			if (lNode.getEdgeByUid(pEdge.uid) != null) {
				lNode.removeEdgeByUid(pEdge.uid);
			}
		}
		TrackSegment lEdgeToDelete = null;
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

	private TrackSegment getCommonEdge(final int pUidA, final int pUidB) {
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

		final var lNewEdge = new TrackSegment(mTrack, mTrack.getNewEdgeUid(), pNodeAUid, pNodeBUid, lNewEdgeAngle);
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
		lNewEdge.lControl0X = lNodeA.x;
		lNewEdge.lControl0Y = lNodeA.y;

		lNewEdge.lControl1X = lNodeB.x;
		lNewEdge.lControl1Y = lNodeB.y;

		lNewEdge.edgeLengthInMeters = mTrack.getEdgeLength(lNewEdge);

		lNodeA.addEdgeToNode(lNewEdge);
		lNodeB.addEdgeToNode(lNewEdge);

		mScreenManager.toastManager().addMessage("", "Track segment created", 1500);
	}

	public void setNewScene() {
		mTrack = new Track();
	}

	public void loadTrackFromFile(String fileName) {
		mTrack = TrackIOController.loadTrackFromFile(fileName);
	}

	public void saveTrack(String pFilename) {
		FileWriter lWriter = null;
		Gson gson = new Gson();
		try {
			lWriter = new FileWriter(pFilename);
			gson.toJson(mTrack, lWriter);

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lWriter.flush();
				lWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ---------------------------------------------
	// Map Manipulation Methods
	// ---------------------------------------------

	private boolean handleEdgeTypeManipulation(LintfordCore pCore) {
		if (mSelectedNodeA == null)
			return false;
		if (activeEdgeLocalIndex != -1) {
			final var lManipulateEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

			if (lManipulateEdge == null)
				return false;

			// toggle betwen line type (cubic bezier or straight line)
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_V, this)) {
				if (lManipulateEdge.edgeType == TrackSegment.EDGE_TYPE_STRAIGHT)
					lManipulateEdge.edgeType = TrackSegment.EDGE_TYPE_CURVE;
				else
					lManipulateEdge.edgeType = TrackSegment.EDGE_TYPE_STRAIGHT;

				updateUpdateCounter();

				return true;

			}
			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_1) && mLeftMouseDown) {
				lManipulateEdge.lControl0X = mMouseGridPositionX;
				lManipulateEdge.lControl0Y = mMouseGridPositionY;

				lManipulateEdge.edgeLengthInMeters = mTrack.getEdgeLength(lManipulateEdge);
				updateUpdateCounter();

				return true;

			}
			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_2) && mLeftMouseDown) {
				lManipulateEdge.lControl1X = mMouseGridPositionX;
				lManipulateEdge.lControl1Y = mMouseGridPositionY;

				lManipulateEdge.edgeLengthInMeters = mTrack.getEdgeLength(lManipulateEdge);
				updateUpdateCounter();

				return true;

			}
		}
		return false;
	}

	private boolean handleTrackConstrainSelection(LintfordCore pCore) {
		if (mSelectedNodeA != null && mSelectedNodeB == null) {
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_L, this)) {
				activeEdgeLocalIndex--;
				if (activeEdgeLocalIndex < 0)
					activeEdgeLocalIndex = mSelectedNodeA.numberConnectedEdges() - 1;

				return true;

			} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_K, this)) {
				activeEdgeLocalIndex++;
				if (activeEdgeLocalIndex >= mSelectedNodeA.numberConnectedEdges())
					activeEdgeLocalIndex = 0;

				return true;

			}
			// Update the currently active constraint
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_H, this)) {
				auxilleryEdgeLocalIndex--;
				if (auxilleryEdgeLocalIndex < 0)
					auxilleryEdgeLocalIndex = mSelectedNodeA.numberConnectedEdges() - 1;

				return true;

			} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_J, this)) {
				auxilleryEdgeLocalIndex++;
				if (auxilleryEdgeLocalIndex >= mSelectedNodeA.numberConnectedEdges())
					auxilleryEdgeLocalIndex = 0;

				return true;

			}
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_O, this)) {
				if (activeEdgeLocalIndex != -1 && auxilleryEdgeLocalIndex != -1) {
					// Toggle allowed / not allowed (ref. edgeSelected)

					final var lSelectedEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);
					final var lConstrainedEdge = mSelectedNodeA.getEdgeByIndex(auxilleryEdgeLocalIndex);
					if (lSelectedEdge.allowedEdgeConections.contains(lConstrainedEdge.uid)) {
						lSelectedEdge.allowedEdgeConections.remove((Integer) lConstrainedEdge.uid);
						System.out.println("Removed constraint uid " + lConstrainedEdge.uid + " from Edge " + lSelectedEdge.uid);

					} else {
						lSelectedEdge.allowedEdgeConections.add(lConstrainedEdge.uid);
						System.out.println("Added allow uid " + lConstrainedEdge.uid + " to Edge " + lSelectedEdge.uid);

					}
				}
				return true;

			}
		}
		return false;
	}

	private boolean handleNodeCreation(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && mLeftMouseDown) {
			// If there is already a node here, then select it
			final var lNodeUnderMouse = getNodeAtGridLocation(mMouseGridPositionX, mMouseGridPositionY);
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

				int newRelX = (int) mMouseGridPositionX - localGridX;
				int newRelY = (int) mMouseGridPositionY - localGridY;
				if (mSelectedNodeB.x == mMouseGridPositionX) { // Horizontal
					lCreationAllowed = true;
				} else if (mSelectedNodeB.y == mMouseGridPositionY) { // vertical
					lCreationAllowed = true;
				} else if (Math.abs(newRelX) == Math.abs(newRelY)) { // diagonal
					lCreationAllowed = true;
				}
//				if (!lCreationAllowed)
//					return true;

				final var lNewNode = new TrackNode(mTrack.getNewNodeUid());
				lNewNode.x = mMouseGridPositionX;
				lNewNode.y = mMouseGridPositionY;

				mTrack.nodes().add(lNewNode);

				createEdgeBetween(lNewNode.uid, mSelectedNodeB.uid);
				mSelectedNodeB = lNewNode;

				updateUpdateCounter();

				return true;

			} else if (mSelectedNodeA != null) {
				int localGridX = (int) mSelectedNodeA.x;
				int localGridY = (int) mSelectedNodeA.y;

				int newRelX = (int) mMouseGridPositionX - localGridX;
				int newRelY = (int) mMouseGridPositionY - localGridY;
				if (mSelectedNodeA.x == mMouseGridPositionX) { // Horizontal
					lCreationAllowed = true;
				} else if (mSelectedNodeA.y == mMouseGridPositionY) { // vertical
					lCreationAllowed = true;
				} else if (Math.abs(newRelX) == Math.abs(newRelY)) { // diagonal
					lCreationAllowed = true;
				}
//				if (!lCreationAllowed)
//					return true;

				final var lNewNode = new TrackNode(mTrack.getNewNodeUid());
				lNewNode.x = mMouseGridPositionX;
				lNewNode.y = mMouseGridPositionY;

				mTrack.nodes().add(lNewNode);

				createEdgeBetween(lNewNode.uid, mSelectedNodeA.uid);
				mSelectedNodeB = lNewNode;

				updateUpdateCounter();

				return true;
			} else {
				final var lNewNode = new TrackNode(mTrack.getNewNodeUid());
				lNewNode.x = mMouseGridPositionX;
				lNewNode.y = mMouseGridPositionY;

				updateUpdateCounter();

				mTrack.nodes().add(lNewNode);
				mSelectedNodeB = lNewNode;
			}
			return true;

		}
		return false;
	}

	private boolean handleNodeSelection(LintfordCore pCore) {
		if (mLeftMouseDownTimed) {
			if (mSelectedNodeA != null && mSelectedNodeB != null) {
				selectedSignalUid = -1;

				mSelectedNodeA = null;
				mSelectedNodeB = null;
				return true;

			}
			final TrackNode lSelectedNode = getNodeAtGridLocation(mMouseWorldPositionX, mMouseWorldPositionY);
			if (lSelectedNode == null) {
				selectedSignalUid = -1;

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
					auxilleryEdgeLocalIndex = 1;
				else
					auxilleryEdgeLocalIndex = -1;

				return true;

			} else if (mSelectedNodeB == null) {
				mSelectedNodeB = lSelectedNode;
				return true;

			}
		}
		return false;
	}

	private TrackNode getNodeAtGridLocation(final float pWorldPositionX, float pWorldPositionY) {
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
				if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_I, this)) {
					if (pCore.input().mouse().isMouseLeftButtonDown()) {
						lEdge.trackJunction.signalLampOffsetX = mMouseWorldPositionX - mSelectedNodeA.x;
						lEdge.trackJunction.signalLampOffsetY = mMouseWorldPositionY - mSelectedNodeA.y;

						updateUpdateCounter();

						return true;

					}
				}
				if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_B, this)) {
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

	private boolean handleDeleteTrackNode(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D) && pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
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
		return false;
	}

	private boolean handleTrackEdgeCreation(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_C, this)) {
			if (mSelectedNodeA != null && mSelectedNodeB != null) {
				createEdgeBetween(mSelectedNodeA.uid, mSelectedNodeB.uid);

			}
			updateUpdateCounter();

			return true;
		}
		return false;
	}

	private boolean handleEdgeSpecialCases(LintfordCore pCore) {
		if (mSelectedNodeA == null || activeEdgeLocalIndex == -1)
			return false;

		final var lActiveEdge = mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_2, this)) {
			System.out.println("Setting edge name");
			mScreenManager.toastManager().addMessage("Editor", "Setting edge name", 1500);
			mSelectedEdge = lActiveEdge;
			mIsCapturedTextInput = true;
			mIsCapturingName = true;
			mInputField.setLength(0);
			pCore.input().keyboard().startBufferedTextCapture(this);

			return true;
		}
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
			lActiveEdge.setEdgeWithType(TrackSegment.EDGE_SPECIAL_TYPE_MAP_SPAWN);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_5, this)) {
			System.out.println("Setting player exit edge");
			lActiveEdge.setEdgeWithType(TrackSegment.EDGE_SPECIAL_TYPE_MAP_EXIT);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_6, this)) {
			System.out.println("Setting map edge");
			lActiveEdge.setEdgeWithType(TrackSegment.EDGE_SPECIAL_TYPE_MAP_EDGE);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_7, this)) {
			System.out.println("Setting station");
			lActiveEdge.setEdgeWithType(TrackSegment.EDGE_SPECIAL_TYPE_STATION);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_8, this)) {
			System.out.println("Setting enemy spawn edge");
			lActiveEdge.setEdgeWithType(TrackSegment.EDGE_SPECIAL_TYPE_ENEMY_SPAWN);
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
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_R)) {
		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_T)) {
		}
		if (selectedSignalUid == -1)
			return false;
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) {
		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_E)) {
		}
		return false;
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
