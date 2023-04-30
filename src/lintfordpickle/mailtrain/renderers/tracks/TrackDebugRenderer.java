package lintfordpickle.mailtrain.renderers.tracks;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.fonts.FontUnit;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrackDebugRenderer extends BaseRenderer implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Debug Track Renderer";

	private boolean DEBUG_DRAW_NODE_INFO = true;
	private boolean DEBUG_DRAW_EDGE_INFO = true;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrackController mTrackController;
	private FontUnit mGameTextFont;

	private float mLeftMouseCooldownTimer;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 10;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	public boolean isCoolDownElapsed() {
		return mLeftMouseCooldownTimer <= 0.f;
	}

	public void resetCoolDownTimer() {
		mLeftMouseCooldownTimer = 200.f;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackDebugRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);
	}

	// ---------------------------------------------
	// Core-Methods+-
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mGameTextFont = pResourceManager.fontManager().getFontUnit("FONT_GAME_TEXT");

		pResourceManager.audioManager().loadAudioFile("SOUND_SIGNAL_CHANGE", "res/sounds/soundSignalChange.wav", false);
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_F6, this)) {
			DEBUG_DRAW_NODE_INFO = !DEBUG_DRAW_NODE_INFO;
		} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_F7, this)) {
			DEBUG_DRAW_EDGE_INFO = !DEBUG_DRAW_EDGE_INFO;
		}
		return super.handleInput(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackController.isInitialized())
			return;
		if (DEBUG_DRAW_NODE_INFO || DEBUG_DRAW_EDGE_INFO) {
			mGameTextFont.begin(pCore.gameCamera());

			if (DEBUG_DRAW_NODE_INFO)
				debugDrawNodes(pCore);

			if (DEBUG_DRAW_EDGE_INFO)
				debugDrawEdges(pCore);

			mGameTextFont.end();
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void debugDrawNodes(LintfordCore pCore) {
		final var lTrack = mTrackController.track();
		final var lNodeList = lTrack.nodes();

		final var lNodeCount = lNodeList.size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = lNodeList.get(i);
			if (lNode.isSelected) {
				GL11.glPointSize(8.f);
				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lNode.x, lNode.y, -0.01f, 1.f, 1.f, 0.f, 1.f);
			} else {
				GL11.glPointSize(4.f);
				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lNode.x, lNode.y, -0.01f, 1.f, 0.f, 0.f, 1.f);
			}
			mGameTextFont.drawText("N:" + lNode.uid, lNode.x, lNode.y - 8.f, -0.01f, ColorConstants.WHITE, .5f, -1);

		}
	}

	public void debugDrawEdges(LintfordCore pCore) {
		final var lTrack = mTrackController.track();
		final var lEdgeList = lTrack.edges();

		Debug.debugManager().drawers().beginLineRenderer(pCore.gameCamera(), GL11.GL_LINES, 2.f);

		final var lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lEdgeList.get(i);

			final var lNodeA = lTrack.getNodeByUid(lEdge.nodeAUid);
			final var lNodeB = lTrack.getNodeByUid(lEdge.nodeBUid);
			{ // node angles
				if (lEdge.edgeType > 0) {
					final float lLineLength = 10.f;

					lEdge.nodeAAngle = (float) Math.atan2(lNodeA.y - lEdge.lControl0Y, lNodeA.x - lEdge.lControl0X);
					lEdge.nodeBAngle = (float) Math.atan2(lNodeB.y - lEdge.lControl1Y, lNodeB.x - lEdge.lControl1X);

					final float lNodeAAngleX = (float) Math.cos(lEdge.nodeAAngle);
					final float lNodeAAngleY = (float) Math.sin(lEdge.nodeAAngle);
					Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeA.x + lNodeAAngleX * lLineLength, lNodeA.y + lNodeAAngleY * lLineLength, 1.f, 0.f, 0.f);

					final float lNodeBAngleX = (float) Math.cos(lEdge.nodeBAngle);
					final float lNodeBAngleY = (float) Math.sin(lEdge.nodeBAngle);
					Debug.debugManager().drawers().drawLine(lNodeB.x, lNodeB.y, lNodeB.x + lNodeBAngleX * lLineLength, lNodeB.y + lNodeBAngleY * lLineLength, 1.f, 0.f, 0.f);

				}
			}
			Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y, 1.f, 1.f, 0.f);

			final float lWorldPositionX = (lNodeA.x + lNodeB.x) / 2.f;
			final float lWorldPositionY = (lNodeA.y + lNodeB.y) / 2.f;

			mGameTextFont.drawText("E:" + lEdge.uid, lWorldPositionX, lWorldPositionY - 5, -0.1f, ColorConstants.WHITE, .5f, -1);
			if (lEdge.trackJunction != null && lEdge.trackJunction.isSignalActive) {
				final var lActiveEdgeUid = lEdge.trackJunction.leftEnabled ? lEdge.trackJunction.leftEdgeUid : lEdge.trackJunction.rightEdgeUid;
				final var lActiveEdge = lTrack.getEdgeByUid(lActiveEdgeUid);

				final int pCommonNodeUid = TrackSegment.getCommonNodeUid(lEdge, lActiveEdge);

				final var lActiveNode = lTrack.getNodeByUid(pCommonNodeUid);
				final var lOtherNodeUid = lActiveEdge.getOtherNodeUid(lActiveNode.uid);
				final var lOtherNode = lTrack.getNodeByUid(lOtherNodeUid);
				final float lVectorX = lOtherNode.x - lActiveNode.x;
				final float lVectorY = lOtherNode.y - lActiveNode.y;

				// TODO: garbage
				var ll = new Vector2f(lVectorX, lVectorY);
				ll.nor();

				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lNodeA.x + ll.x * 20.f, lNodeA.y + ll.y * 20.f, 4.f);

			}
			{
				// Control Nodes
				if (lEdge.edgeType > 0) {
					final float lControl0X = lEdge.lControl0X;
					final float lControl0Y = lEdge.lControl0Y;

					final float lControl1X = lEdge.lControl1X;
					final float lControl1Y = lEdge.lControl1Y;

					Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lControl1X, lControl1Y, 0.f, 1.f, 1.f);
					Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lControl0X, lControl0Y, 4.f);

					Debug.debugManager().drawers().drawLine(lNodeB.x, lNodeB.y, lControl0X, lControl0Y, 0.f, 1.f, 1.f);
					Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lControl1X, lControl1Y, 2.f);
				}
			}
			{ // Edge length (meters)
				final var lEdgeLength = lEdge.edgeLengthInMeters;

				// Calculate the center point of the edge
				final float lCenterX = (lNodeA.x + lNodeB.x) * 0.5f;
				final float lCenterY = (lNodeA.y + lNodeB.y) * 0.5f;

				mGameTextFont.drawText(String.format("%.1fm", lEdgeLength), lCenterX, lCenterY + mGameTextFont.fontHeight() * 0.4f, -0.01f, ColorConstants.WHITE, 0.5f, -1);
			}
		}
		Debug.debugManager().drawers().endLineRenderer();
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

}
