package lintfordpickle.mailtrain.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
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

	private static List<RailTrackSegment> mTempSegmentList = new ArrayList<>();

	public static final String CONTROLLER_NAME = "Track Editor Controller";

	public enum EditorMode {
		normal, place_track, place_road, demolish
	};

	private ScreenManager mScreenManager;
	private RailTrackInstance mTrack;
	private TrackController mTrackController;

	private EditorMode mEditorMode;

	public RailTrackNode mSelectedNodeA; // always 'building-from' this node
	public RailTrackNode mSelectedNodeB;

	public RailTrackNode ghostNodeA; // node we are currently placing
	public RailTrackNode ghostNodeB; // node we are currently placing

	public RailTrackSegment selectedTrackSegment;

	public int selectedSignalUid = 0;

	public int activeSegmentLocalIndex = -1; // index is local to the selected node
	public int auxillerySegmentLocalIndex = -1; // index is local to the selected node

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

	public RailTrackInstance track() {
		return mTrack;
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

		ghostNodeA = new RailTrackNode(mTrack.getNewNodeUid());
		ghostNodeB = new RailTrackNode(mTrack.getNewSegmentUid());
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
		mMouseGridPositionX = mMouseWorldPositionX;// worldToGrid(mMouseWorldPositionX);
		mMouseGridPositionY = mMouseWorldPositionY;// worldToGrid(mMouseWorldPositionY);

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
						// split segment if selected

						// or lay new node
						handleNodeCreation(pCore);
						mLeftMouseDownProcessed = true;
					}
				} else {
					if (lWasNodeClicked != null) {
						// another node was selected to link up to
						// TODO: check validity before creation
						createSegmentBetween(mSelectedNodeA.uid, lWasNodeClicked.uid);
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
						if (handleSegmentSelection() == false) {
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

	private void deleteNode(RailTrackNode pNode) {
		if (pNode == null) {
			return;
		}
		mTempSegmentList.clear();

		final var lSegmentCount = pNode.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lSegmentCount; i++) {
			mTempSegmentList.add(pNode.trackSwitch.getConnectedSegmentByIndex(i));
		}
		for (int i = 0; i < lSegmentCount; i++) {
			deleteSegment(mTempSegmentList.get(i));
		}
		mTrack.nodes().remove(pNode);
	}

	private void deleteSegment(RailTrackSegment segment) {
		if (segment == null)
			return;
		final int lNodeCount = mTrack.nodes().size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mTrack.nodes().get(i);
			if (lNode.trackSwitch.getConnectedSegmentByUid(segment.uid) != null) {
				lNode.removeSegmentByUid(segment.uid);
			}
		}
		RailTrackSegment lSegmentToDelete = null;
		final int lSegmentCount = mTrack.segments().size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = mTrack.segments().get(i);
			if (lSegment.nodeAUid == segment.nodeAUid || lSegment.nodeAUid == segment.nodeBUid) {
				if (lSegment.nodeBUid == segment.nodeAUid || lSegment.nodeBUid == segment.nodeBUid) {
					lSegmentToDelete = lSegment;
				}
			}
		}
		mTrack.segments().remove(lSegmentToDelete);
		segment = null;
	}

	private RailTrackSegment getCommonSegment(final int pUidA, final int pUidB) {
		final var lNodeA = mTrack.getNodeByUid(pUidA);
		final var lNodeB = mTrack.getNodeByUid(pUidB);

		if (lNodeA == null || lNodeB == null)
			return null;

		final int lSegmentCountNodeA = lNodeA.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lSegmentCountNodeA; i++) {
			final var lOtherNodeUid = lNodeA.trackSwitch.getConnectedSegmentByIndex(i).getOtherNodeUid(pUidA);
			if (pUidB == lOtherNodeUid) {
				return lNodeA.trackSwitch.getConnectedSegmentByIndex(i);
			}
		}
		return null;
	}

	private void createSegmentBetween(int pNodeAUid, int pNodeBUid) {
		if (pNodeAUid == pNodeBUid)
			return; // Nope

		boolean lSegmentExists = mTrack.doesSegmentExistsBetween(pNodeAUid, pNodeBUid);
		if (lSegmentExists)
			return; // Nope

		final var lNodeA = mTrack.getNodeByUid(pNodeAUid);
		final var lNodeB = mTrack.getNodeByUid(pNodeBUid);

		if (lNodeA == null || lNodeB == null)
			return; // nope

		final var lNewSegmentAngle = (float) Math.atan2(Math.abs(lNodeB.y - lNodeA.y), Math.abs(lNodeB.x - lNodeA.x));
		final var lNewSegment = new RailTrackSegment(mTrack, mTrack.getNewSegmentUid(), pNodeAUid, pNodeBUid, lNewSegmentAngle);
		mTrack.segments().add(lNewSegment);

		lNewSegment.segmentType = (lNodeA.x == lNodeB.x || lNodeA.y == lNodeB.y) ? RailTrackSegment.SEGMENT_TYPE_STRAIGHT : RailTrackSegment.SEGMENT_TYPE_CURVE;

		final int lNodeASegmentCount = lNodeA.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lNodeASegmentCount; i++) {
			final var lOldSegment = lNodeA.trackSwitch.getConnectedSegmentByIndex(i);
			if (lOldSegment == null)
				return;
			if (Math.abs(lOldSegment.segmentAngle - lNewSegment.segmentAngle) < MIN_SEGMENT_ANGLE_TOLERENCE) {
				//				if (!lOldEdge.allowedEdgeConections.contains((Integer) lNewEdge.uid)) {
				//					lOldEdge.allowedEdgeConections.add((Integer) lNewEdge.uid);
				//				}
				//				if (!lNewEdge.allowedEdgeConections.contains((Integer) lOldEdge.uid)) {
				//					lNewEdge.allowedEdgeConections.add((Integer) lOldEdge.uid);
				//				}
				mScreenManager.toastManager().addMessage("Track", "Segment connected", 150);
			} else {
				mScreenManager.toastManager().addMessage("Track", "Angle too large!", 150);
			}
		}

		final int lNodeBSegmentCount = lNodeB.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lNodeBSegmentCount; i++) {
			final var lOldSegment = lNodeB.trackSwitch.getConnectedSegmentByIndex(i);
			if (lOldSegment == null)
				continue;
			if (Math.abs(lOldSegment.segmentAngle - lNewSegment.segmentAngle) < MIN_SEGMENT_ANGLE_TOLERENCE) {
				//				if (!lOldEdge.allowedEdgeConections.contains((Integer) lNewEdge.uid)) {
				//					lOldEdge.allowedEdgeConections.add((Integer) lNewEdge.uid);
				//				}
				//				if (!lNewEdge.allowedEdgeConections.contains((Integer) lOldEdge.uid)) {
				//					lNewEdge.allowedEdgeConections.add((Integer) lOldEdge.uid);
				//				}
				mScreenManager.toastManager().addMessage("Track", "Segment connected", 150);
			} else {
				mScreenManager.toastManager().addMessage("Track", "Angle too large!", 150);
			}
		}

		lNewSegment.control0X = lNodeA.x;
		lNewSegment.control0Y = lNodeA.y;

		lNewSegment.control1X = lNodeB.x;
		lNewSegment.control1Y = lNodeB.y;

		lNewSegment.segmentLengthInMeters = mTrack.getSegmentLength(lNewSegment);

		// set the node angles
		lNewSegment.nodeAAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeA.y - lNodeB.y, lNodeA.x - lNodeB.x));
		lNewSegment.nodeBAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lNodeA.y, lNodeB.x - lNodeA.x));

		lNodeA.addSegmentToNode(lNewSegment);
		lNodeB.addSegmentToNode(lNewSegment);

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
		final var lSegmentList = mTrack.segments();
		final var lSegmentCount = lSegmentList.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = lSegmentList.get(i);

			final var lNodeA = mTrack.getNodeByUid(lSegment.nodeAUid);
			final var lNodeB = mTrack.getNodeByUid(lSegment.nodeBUid);

			// Recalc type of segment
			lSegment.segmentType = (lNodeA.x == lNodeB.x || lNodeA.y == lNodeB.y) ? RailTrackSegment.SEGMENT_TYPE_STRAIGHT : RailTrackSegment.SEGMENT_TYPE_CURVE;

			Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y);
			if (lSegment.segmentType == 0) {
				lSegment.control0X = lNodeA.x;
				lSegment.control0Y = lNodeA.y;

				lSegment.control1X = lNodeB.x;
				lSegment.control1Y = lNodeB.y;

				lSegment.nodeAAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeA.y - lNodeB.y, lNodeA.x - lNodeB.x));
				lSegment.nodeBAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lNodeA.y, lNodeB.x - lNodeA.x));
			} else {
				final float lControlMinLength = 32.f;
				float lControlLength = Vector2f.dst(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y) / 2.f;
				lControlLength = MathHelper.max(lControlMinLength, lControlLength);

				lSegment.control0X = lNodeA.x;
				lSegment.control0Y = lNodeA.y;

				lSegment.control1X = lNodeB.x;
				lSegment.control1Y = lNodeB.y;

				//				int edge0Uid = lEdge.getOtherAllowedEdgeConnectionUids();
				//				if (edge0Uid != -1) {
				//					final var lEdge0 = mTrack.getEdgeByUid(edge0Uid);
				//					if (lEdge0 == null)
				//						continue;
				//
				//					// Red
				//					final float lNodeAngle = lEdge0.getNodeAngle(lNodeA.uid);
				//					lEdge.control0X = lNodeA.x + (float) Math.cos(lNodeAngle) * lControlLength;
				//					lEdge.control0Y = lNodeA.y + (float) Math.sin(lNodeAngle) * lControlLength;
				//
				//					lEdge.nodeAAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeA.y - lEdge.control0Y, lNodeA.x - lEdge.control0X));
				//
				//				}
				//				
				//				int edge1Uid = lEdge.getOtherAllowedEdgeConnectionUids2();
				//				if (edge1Uid != -1) {
				//					final var lEdge1 = mTrack.getEdgeByUid(edge1Uid);
				//					if (lEdge1 == null)
				//						continue;
				//
				//					// Green
				//					final float lNodeAngle = lEdge1.getNodeAngle(lNodeB.uid);
				//					lEdge.control1X = lNodeB.x + (float) Math.cos(lNodeAngle) * lControlLength;
				//					lEdge.control1Y = lNodeB.y + (float) Math.sin(lNodeAngle) * lControlLength;
				//
				//					lEdge.nodeBAngle = MathHelper.wrapAngle((float) Math.atan2(lNodeB.y - lEdge.control1Y, lNodeB.x - lEdge.control1X));
				//				}

				if (lNodeA != null && lNodeB != null && lNodeA.y > lNodeB.y) {
					float tx = lSegment.control0X;
					float ty = lSegment.control0Y;
					lSegment.control0X = lSegment.control1X;
					lSegment.control0Y = lSegment.control1Y;
					lSegment.control1X = tx;
					lSegment.control1Y = ty;
				}

				if (lSegment.uid == 18) {
					System.out.println(lNodeA.x + "," + lNodeA.y + " (" + lSegment.control0X + "," + lSegment.control0Y + ")");
					System.out.println(lNodeB.x + "," + lNodeB.y + " (" + lSegment.control1X + "," + lSegment.control1Y + ")");
					System.out.println("------------------");
				}

				lSegment.segmentLengthInMeters = mTrack.getSegmentLength(lSegment);
			}
		}
	}

	private boolean handleNodeSelection(LintfordCore core) {
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

					if (mSelectedNodeA.trackSwitch.numberConnectedSegments() > 0)
						activeSegmentLocalIndex = 0;
					else
						activeSegmentLocalIndex = -1;

					if (mSelectedNodeA.trackSwitch.numberConnectedSegments() > 1)
						auxillerySegmentLocalIndex = 1;
					else
						auxillerySegmentLocalIndex = -1;

					return true;

				}
			}
		}
		return false;
	}

	private boolean handleNodeCreation(LintfordCore core) {
		// If there is already a node here, then select it
		final var lNodeUnderMouse = getNodeAtGridLocation(mMouseGridPositionX, mMouseGridPositionY);
		if (lNodeUnderMouse != null) {
			if (mSelectedNodeB != null && lNodeUnderMouse != mSelectedNodeB) {
				// There maybe a chance to connect nodes A and the just selected one together
				createSegmentBetween(mSelectedNodeB.uid, lNodeUnderMouse.uid);

				updateTrackBuildLogicalCounter();

				return true;
			}
			return false;
		}
		// There are 3 situations we want to be creating track nodes:
		// no A or B - then create node at GA
		// A but no B - then create node at GA - create segment between A and GA
		// A and B - destroy segment between A-B, create node at GA, connect A-GA-B
		if (mSelectedNodeA == null && mSelectedNodeB == null) {
			final var lNewNode = new RailTrackNode(mTrack.getNewNodeUid());
			lNewNode.x = mMouseGridPositionX;
			lNewNode.y = mMouseGridPositionY;

			mTrack.nodes().add(lNewNode);

			mSelectedNodeA = lNewNode;

			updateTrackBuildLogicalCounter();

			return true;
		}
		if (mSelectedNodeA != null && mSelectedNodeB == null) {
			final var lNewNode = new RailTrackNode(mTrack.getNewNodeUid());
			lNewNode.x = mMouseGridPositionX;
			lNewNode.y = mMouseGridPositionY;

			mTrack.nodes().add(lNewNode);

			createSegmentBetween(mSelectedNodeA.uid, lNewNode.uid);
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

	private boolean handleSegmentSelection() {
		final var lSegmentList = mTrack.segments();
		final int lSegmentCount = lSegmentList.size();
		for (int i = 0; i < lSegmentCount; i++) {
			// TODO: Add faster AABB check on segment bounds
			final var lSegment = lSegmentList.get(i);
			if (lSegment.segmentType == RailTrackSegment.SEGMENT_TYPE_STRAIGHT) {
				// TODO: add AABB to TrackSegment, then we can skip this check
				final var lNodeA = mTrack.getNodeByUid(lSegment.nodeAUid);
				final var lNodeB = mTrack.getNodeByUid(lSegment.nodeBUid);
				float lDist = getCollisionPointOnSegment(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y, mMouseWorldPositionX, mMouseWorldPositionY);
				if (lDist < 16.f) {
					selectedTrackSegment = lSegment;
					return true;
				}
			} else {
				// check points on curve for match
			}
		}
		return false;
	}

	private RailTrackNode getNodeAtGridLocation(final float pWorldPositionX, float pWorldPositionY) {
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
			// remove segments from between these nodes
			final var lCommonSegment = getCommonSegment(mSelectedNodeA.uid, mSelectedNodeB.uid);
			if (lCommonSegment != null) {
				deleteSegment(lCommonSegment);

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
