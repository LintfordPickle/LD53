package lintfordpickle.mailtrain.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.track.TrackNode;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.screenmanager.ScreenManager;

public class GameTrackEditorController extends BaseController implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static List<TrackSegment> mTempEdgeList = new ArrayList<>();

	public static final String CONTROLLER_NAME = "Track Editor Controller";

	public enum EditorMode {
		normal, place_track, place_road, demolish
	};

	private ScreenManager mScreenManager;
	private Track mTrack;
	private TrackController mTrackController;

	private EditorMode mEditorMode;

	public TrackNode mSelectedNodeA; // always 'building-from' this node
	public TrackNode mSelectedNodeB;

	public TrackNode ghostNodeA; // node we are currently placing
	public TrackNode ghostNodeB; // node we are currently placing

	public TrackSegment selectedTrackSegment;

	public int selectedSignalUid = 0;

	public int activeEdgeLocalIndex = -1; // index is local to the selected node
	public int auxilleryEdgeLocalIndex = -1; // index is local to the selected node

	private boolean mLeftMouseDownTimed;
	private boolean mLeftMouseDown;
	private boolean mLeftMouseDownProcessed;
	private float mLeftClickDownTimer;
	private boolean mIsInNodePlacementMode;

	private float mMouseWorldPositionX;
	private float mMouseWorldPositionY;
	private float mMouseGridPositionX;
	private float mMouseGridPositionY;

	final float MIN_SEGMENT_ANGLE_TOLERENCE = (float) Math.toRadians(90.f);

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isInEditorMode() {
		return mEditorMode == EditorMode.place_track || mEditorMode == EditorMode.place_road;
	}

	public void setEditorMode(EditorMode pEditorMode) {
		if (pEditorMode == null) {
			mEditorMode = EditorMode.normal;
			return;
		}
		mEditorMode = pEditorMode;
		if (mEditorMode == EditorMode.normal) {
			mSelectedNodeA = null;
			mSelectedNodeB = null;

			ghostNodeA.isSelected = false;
			ghostNodeB.isSelected = false;

			selectedTrackSegment = null;
		}
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

	public GameTrackEditorController(ControllerManager pControllerManager, ScreenManager pScreenManager, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mScreenManager = pScreenManager;
		mEditorMode = EditorMode.normal;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupUid());
		mTrack = mTrackController.track();

		ghostNodeA = new TrackNode(mTrack.getNewNodeUid());
		ghostNodeB = new TrackNode(mTrack.getNewEdgeUid());
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		// Scenery controller
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_RIGHT_ALT, this)) {
			updateTrackBuildLogicalCounter();
		}
		final var lLeftMouseDown = pCore.input().mouse().isMouseLeftButtonDown();
		if (lLeftMouseDown && mLeftMouseDown == false) {
			mLeftClickDownTimer = 0;
			mLeftMouseDownProcessed = false;
			mScreenManager.toastManager().addMessage("", "left mouse processed = false", 1500);
		}
		if (mLeftMouseDown) {
			mLeftClickDownTimer += pCore.gameTime().elapsedTimeMilli();
		}
		mLeftMouseDown = lLeftMouseDown;
		if (mLeftMouseDown == false && mIsInNodePlacementMode) {
			mIsInNodePlacementMode = false;
			if (mSelectedNodeA != null)
				mSelectedNodeA.isSelected = false;
			mSelectedNodeA = null;
		}
		mLeftMouseDownTimed = pCore.input().mouse().isMouseLeftButtonDownTimed(this);

		mMouseWorldPositionX = pCore.gameCamera().getMouseWorldSpaceX();
		mMouseWorldPositionY = pCore.gameCamera().getMouseWorldSpaceY();
		mMouseGridPositionX = worldToGrid(mMouseWorldPositionX);
		mMouseGridPositionY = worldToGrid(mMouseWorldPositionY);
		switch (mEditorMode) {
		case place_track:

			if (mSelectedNodeA == null && mSelectedNodeB == null) { // placing starting point
				ghostNodeA.isSelected = true;
				ghostNodeA.x = mMouseGridPositionX;
				ghostNodeA.y = mMouseGridPositionY;
			}

			if (mSelectedNodeA != null && mSelectedNodeB == null) { // placing a new segment
				ghostNodeA.isSelected = true;
				ghostNodeA.x = mMouseGridPositionX;
				ghostNodeA.y = mMouseGridPositionY;
			}

			if (mSelectedNodeA != null && mSelectedNodeB != null) { // modifing existing segment
				ghostNodeA.isSelected = false;
				ghostNodeB.isSelected = false;
			}

			if (mLeftMouseDownTimed && !mLeftMouseDownProcessed) {
				final var lWasNodeClicked = getNodeAtGridLocation(mMouseWorldPositionX, mMouseWorldPositionY);

				// check if we deselected node a
				if (mSelectedNodeA != null && (lWasNodeClicked != null && mSelectedNodeA == lWasNodeClicked)) {
					mSelectedNodeA.isSelected = false;
					mSelectedNodeA = null;

					mLeftMouseDownProcessed = true;
					return true;
				}
				// check if we selected node a
				if (mSelectedNodeA == null) {
					if (handleNodeSelection(pCore)) {
						mLeftMouseDownProcessed = true;

					} else {
						// split edge if selected

						// or lay new node
						handleNodeCreation(pCore);
						mLeftMouseDownProcessed = true;
					}
				} else {
					if (lWasNodeClicked != null) {
						// another node was selected to link up to
						// TODO: check validity before creation
						createEdgeBetween(mSelectedNodeA.uid, lWasNodeClicked.uid);
						mSelectedNodeA = null;
						mSelectedNodeB = null;
						updateTrackBuildLogicalCounter();
						mLeftMouseDownProcessed = true;
						return true;
					}
					// lay track somewhere
					if (handleNodeCreation(pCore)) {
						mLeftMouseDownProcessed = true;
					}
				}
			}

			break;
		case demolish:
			if (handleNodeSelection(pCore))
				handleDeleteTrackNode(pCore);
			break;
		default:
			if (mLeftMouseDown && !mLeftMouseDownProcessed) {
				if (mSelectedNodeA != null) {
					if (mLeftClickDownTimer > 500.f) {
						mIsInNodePlacementMode = true;
						System.out.println("movement " + mLeftClickDownTimer);
						// handle node placement
						mSelectedNodeA.x = mMouseGridPositionX;
						mSelectedNodeA.y = mMouseGridPositionY;

						// TODO: Snap to grid

						updateTrackBuildLogicalCounter();
					}
				} else {
					if (handleNodeSelection(pCore) == false) {
						if (mSelectedNodeA != null)
							mSelectedNodeA.isSelected = false;
						mSelectedNodeA = null;
						if (mSelectedNodeB != null)
							mSelectedNodeB.isSelected = false;
						mSelectedNodeB = null;
						ghostNodeA.isSelected = false;
						ghostNodeB.isSelected = false;
						if (handleEdgeSelection() == false) {
							selectedTrackSegment = null;
						}
					}
				}
			}

			break;
		}
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
			return; // Nope

		final var lNodeA = mTrack.getNodeByUid(pNodeAUid);
		final var lNodeB = mTrack.getNodeByUid(pNodeBUid);

		if (lNodeA == null || lNodeB == null)
			return; // nope

		final var lNewEdgeAngle = (float) Math.atan2(Math.abs(lNodeB.y - lNodeA.y), Math.abs(lNodeB.x - lNodeA.x));
		final var lNewEdge = new TrackSegment(mTrack, mTrack.getNewEdgeUid(), pNodeAUid, pNodeBUid, lNewEdgeAngle);
		mTrack.edges().add(lNewEdge);

		lNewEdge.edgeType = (lNodeA.x == lNodeB.x || lNodeA.y == lNodeB.y) ? TrackSegment.EDGE_TYPE_STRAIGHT : TrackSegment.EDGE_TYPE_CURVE;

		final int lNodeAEdgeCount = lNodeA.numberConnectedEdges();
		for (int i = 0; i < lNodeAEdgeCount; i++) {
			final var lOldEdge = lNodeA.getEdgeByIndex(i);
			if (lOldEdge == null)
				return;
			if (Math.abs(lOldEdge.edgeAngle - lNewEdge.edgeAngle) < MIN_SEGMENT_ANGLE_TOLERENCE) {
				if (!lOldEdge.allowedEdgeConections.contains((Integer) lNewEdge.uid)) {
					lOldEdge.allowedEdgeConections.add((Integer) lNewEdge.uid);
				}
				if (!lNewEdge.allowedEdgeConections.contains((Integer) lOldEdge.uid)) {
					lNewEdge.allowedEdgeConections.add((Integer) lOldEdge.uid);
				}
				mScreenManager.toastManager().addMessage("Track", "Segment connected", 150);
			} else {
				mScreenManager.toastManager().addMessage("Track", "Angle too large!", 150);
			}
		}
		final int lNodeBEdgeCount = lNodeB.numberConnectedEdges();
		for (int i = 0; i < lNodeBEdgeCount; i++) {
			final var lOldEdge = lNodeB.getEdgeByIndex(i);
			if (lOldEdge == null)
				continue;
			if (Math.abs(lOldEdge.edgeAngle - lNewEdge.edgeAngle) < MIN_SEGMENT_ANGLE_TOLERENCE) {
				if (!lOldEdge.allowedEdgeConections.contains((Integer) lNewEdge.uid)) {
					lOldEdge.allowedEdgeConections.add((Integer) lNewEdge.uid);
				}
				if (!lNewEdge.allowedEdgeConections.contains((Integer) lOldEdge.uid)) {
					lNewEdge.allowedEdgeConections.add((Integer) lOldEdge.uid);
				}
				mScreenManager.toastManager().addMessage("Track", "Segment connected", 150);
			} else {
				mScreenManager.toastManager().addMessage("Track", "Angle too large!", 150);
			}
		}
		lNewEdge.lControl0X = lNodeA.x;
		lNewEdge.lControl0Y = lNodeA.y;

		lNewEdge.lControl1X = lNodeB.x;
		lNewEdge.lControl1Y = lNodeB.y;

		lNewEdge.edgeLengthInMeters = mTrack.getEdgeLength(lNewEdge);

		// set the node angles
		lNewEdge.nodeAAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeA.y - lNodeB.y, lNodeA.x - lNodeB.x));
		lNewEdge.nodeBAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lNodeA.y, lNodeB.x - lNodeA.x));

		lNodeA.addEdgeToNode(lNewEdge);
		lNodeB.addEdgeToNode(lNewEdge);

		mScreenManager.toastManager().addMessage("", "Track segment created", 1500);
	}

	// ---------------------------------------------
	// Map Manipulation Methods
	// ---------------------------------------------

	private void updateTrackBuildLogicalCounter() {
		rebuildControlNodes();

		mTrackController.updateTrackBuildLogicalCounter();
	}

	private void rebuildControlNodes() {
		final var lEdgeList = mTrack.edges();
		final var lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lEdgeList.get(i);

			final var lNodeA = mTrack.getNodeByUid(lEdge.nodeAUid);
			final var lNodeB = mTrack.getNodeByUid(lEdge.nodeBUid);

			// Recalc type of edge
			lEdge.edgeType = (lNodeA.x == lNodeB.x || lNodeA.y == lNodeB.y) ? TrackSegment.EDGE_TYPE_STRAIGHT : TrackSegment.EDGE_TYPE_CURVE;

			Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y);
			if (lEdge.edgeType == 0) {
				lEdge.lControl0X = lNodeA.x;
				lEdge.lControl0Y = lNodeA.y;

				lEdge.lControl1X = lNodeB.x;
				lEdge.lControl1Y = lNodeB.y;

				lEdge.nodeAAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeA.y - lNodeB.y, lNodeA.x - lNodeB.x));
				lEdge.nodeBAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lNodeA.y, lNodeB.x - lNodeA.x));
			} else {
				final float lControlMinLength = 32.f;
				float lControlLength = Vector2f.dst(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y) / 2.f;
				lControlLength = MathHelper.max(lControlMinLength, lControlLength);

				lEdge.lControl0X = lNodeA.x;
				lEdge.lControl0Y = lNodeA.y;

				lEdge.lControl1X = lNodeB.x;
				lEdge.lControl1Y = lNodeB.y;

				int edge0Uid = lEdge.getOtherAllowedEdgeConnectionUids();
				if (edge0Uid != -1) {
					final var lEdge0 = mTrack.getEdgeByUid(edge0Uid);
					if (lEdge0 == null)
						continue;

					// Red
					final float lNodeAngle = lEdge0.getNodeAngle(lNodeA.uid);
					lEdge.lControl0X = lNodeA.x + (float) Math.cos(lNodeAngle) * lControlLength;
					lEdge.lControl0Y = lNodeA.y + (float) Math.sin(lNodeAngle) * lControlLength;

					lEdge.nodeAAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeA.y - lEdge.lControl0Y, lNodeA.x - lEdge.lControl0X));

				}
				int edge1Uid = lEdge.getOtherAllowedEdgeConnectionUids2();
				if (edge1Uid != -1) {
					final var lEdge1 = mTrack.getEdgeByUid(edge1Uid);
					if (lEdge1 == null)
						continue;

					// Green
					final float lNodeAngle = lEdge1.getNodeAngle(lNodeB.uid);
					lEdge.lControl1X = lNodeB.x + (float) Math.cos(lNodeAngle) * lControlLength;
					lEdge.lControl1Y = lNodeB.y + (float) Math.sin(lNodeAngle) * lControlLength;

					lEdge.nodeBAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lEdge.lControl1Y, lNodeB.x - lEdge.lControl1X));
				}
				if (lNodeA != null && lNodeB != null && lNodeA.y > lNodeB.y) {
					float tx = lEdge.lControl0X;
					float ty = lEdge.lControl0Y;
					lEdge.lControl0X = lEdge.lControl1X;
					lEdge.lControl0Y = lEdge.lControl1Y;
					lEdge.lControl1X = tx;
					lEdge.lControl1Y = ty;
				}
				if (lEdge.uid == 18) {
					System.out.println(lNodeA.x + "," + lNodeA.y + " (" + lEdge.lControl0X + "," + lEdge.lControl0Y + ")");
					System.out.println(lNodeB.x + "," + lNodeB.y + " (" + lEdge.lControl1X + "," + lEdge.lControl1Y + ")");
					System.out.println("------------------");
				}
				lEdge.edgeLengthInMeters = mTrack.getEdgeLength(lEdge);
			}
		}
	}

	private boolean handleNodeSelection(LintfordCore pCore) {
		if (mLeftMouseDown == false || mLeftMouseDownProcessed == true)
			return false;

		// after this point, we are processing a click
		if (mSelectedNodeA != null) { // deselect node on click
			mSelectedNodeA.isSelected = false;
			mSelectedNodeA = null;
			return true;
		}
		if (mSelectedNodeA == null) {
			final var lSelectedNode = getNodeAtGridLocation(mMouseWorldPositionX, mMouseWorldPositionY);
			if (lSelectedNode != null) {
				// a node was clicked
				if (mSelectedNodeA == null) {
					mSelectedNodeA = lSelectedNode;
					mSelectedNodeA.isSelected = true;

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

				}
			}
		}
		return false;
	}

	private boolean handleNodeCreation(LintfordCore pCore) {
		// If there is already a node here, then select it
		final var lNodeUnderMouse = getNodeAtGridLocation(mMouseGridPositionX, mMouseGridPositionY);
		if (lNodeUnderMouse != null) {
			if (mSelectedNodeB != null && lNodeUnderMouse != mSelectedNodeB) {
				// There maybe a chance to connect nodes A and the just selected one together
				createEdgeBetween(mSelectedNodeB.uid, lNodeUnderMouse.uid);

				updateTrackBuildLogicalCounter();

				return true;
			}
			return false;
		}
		// There are 3 situations we want to be creating track nodes:
		// no A or B - then create node at GA
		// A but no B - then create node at GA - create edge between A and GA
		// A and B - destroy segment between A-B, create node at GA, connect A-GA-B
		if (mSelectedNodeA == null && mSelectedNodeB == null) {
			final var lNewNode = new TrackNode(mTrack.getNewNodeUid());
			lNewNode.x = mMouseGridPositionX;
			lNewNode.y = mMouseGridPositionY;

			mTrack.nodes().add(lNewNode);

			mSelectedNodeA = lNewNode;

			updateTrackBuildLogicalCounter();

			return true;
		}
		if (mSelectedNodeA != null && mSelectedNodeB == null) {
			final var lNewNode = new TrackNode(mTrack.getNewNodeUid());
			lNewNode.x = mMouseGridPositionX;
			lNewNode.y = mMouseGridPositionY;

			mTrack.nodes().add(lNewNode);

			createEdgeBetween(mSelectedNodeA.uid, lNewNode.uid);
			mSelectedNodeA.isSelected = false;
			mSelectedNodeA = lNewNode;

			updateTrackBuildLogicalCounter();
			return true;
		}
		if (mSelectedNodeA != null && mSelectedNodeB != null) {
			// Destroy segment
		}
		return false;
	}

	private boolean handleEdgeSelection() {
		final var lEdgeList = mTrack.edges();
		final int lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			// TODO: Add faster AABB check on segment bounds
			final var lEdge = lEdgeList.get(i);
			if (lEdge.edgeType == TrackSegment.EDGE_TYPE_STRAIGHT) {
				// TODO: add AABB to TrackSegment, then we can skip this check
				final var lNodeA = mTrack.getNodeByUid(lEdge.nodeAUid);
				final var lNodeB = mTrack.getNodeByUid(lEdge.nodeBUid);
				float lDist = getCollisionPointOnSegment(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y, mMouseWorldPositionX, mMouseWorldPositionY);
				if (lDist < 16.f) {
					selectedTrackSegment = lEdge;
					return true;
				}
			} else {
				// check points on curve for match
			}
		}
		return false;
	}

	private TrackNode getNodeAtGridLocation(final float pWorldPositionX, float pWorldPositionY) {
		// TODO :This can be made to an AABB grid colision (no need for sqrt)
		final int lNodeCount = mTrack.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrack.nodes().get(i);
			if (Vector2f.dst(pWorldPositionX, pWorldPositionY, lNode.x, lNode.y) < 8.f) {
				return lNode;

			}
		}
		return null;
	}

	private boolean handleDeleteTrackNode(LintfordCore pCore) {
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
		updateTrackBuildLogicalCounter();

		return true;
	}

	@Override
	public boolean allowKeyboardInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowGamepadInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		// TODO Auto-generated method stub
		return false;
	}

	// --- Helper

	public static final Vector2f colPoint = new Vector2f();

	// return the sq distance to the closest point along the given line (
	public static float getCollisionPointOnSegment(float p1X, float p1Y, float p2X, float p2Y, float pX, float PY) {
		final float xDelta = p2X - p1X;
		final float yDelta = p2Y - p1Y;

		if ((xDelta == 0) && (yDelta == 0)) {
			throw new IllegalArgumentException("p1 and p2 cannot be the same point");
		}

		final float u = ((pX - p1X) * xDelta + (PY - p1Y) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
		if (u < 0) {
			colPoint.x = p1X;
			colPoint.y = p1Y;
		} else if (u > 1) {
			colPoint.x = p2X;
			colPoint.y = p2Y;
		} else {
			colPoint.x = p1X + u * xDelta;
			colPoint.y = p1Y + u * yDelta;
		}

		return Vector2f.dst(colPoint.x, colPoint.y, pX, PY);
	}

}
