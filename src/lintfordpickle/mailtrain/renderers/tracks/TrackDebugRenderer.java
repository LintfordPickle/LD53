package lintfordpickle.mailtrain.renderers.tracks;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.fonts.FontUnit;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrackDebugRenderer extends BaseRenderer implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Debug Track Renderer";

	private boolean DEBUG_DRAW_NODE_INFO = true;
	private boolean DEBUG_DRAW_SEGMENT_INFO = true;

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

	public TrackDebugRenderer(RendererManager rendererManager, int entityGroupUid) {
		super(rendererManager, RENDERER_NAME, entityGroupUid);
	}

	// ---------------------------------------------
	// Core-Methods+-
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		mTrackController = (TrackController) core.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mGameTextFont = resourceManager.fontManager().getFontUnit("FONT_GAME_TEXT");

		resourceManager.audioManager().loadAudioFile("SOUND_SIGNAL_CHANGE", "res/sounds/soundSignalChange.wav", false);
	}

	@Override
	public boolean handleInput(LintfordCore core) {
		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_F6, this)) {
			DEBUG_DRAW_NODE_INFO = !DEBUG_DRAW_NODE_INFO;
		} else if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_F7, this)) {
			DEBUG_DRAW_SEGMENT_INFO = !DEBUG_DRAW_SEGMENT_INFO;
		}
		return super.handleInput(core);
	}

	@Override
	public void draw(LintfordCore core) {
		if (!mTrackController.isInitialized())
			return;
		if (DEBUG_DRAW_NODE_INFO || DEBUG_DRAW_SEGMENT_INFO) {
			mGameTextFont.begin(core.gameCamera());

			if (DEBUG_DRAW_NODE_INFO)
				debugDrawNodes(core);

			if (DEBUG_DRAW_SEGMENT_INFO)
				debugDrawSegments(core);

			mGameTextFont.end();
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void debugDrawNodes(LintfordCore core) {
		final var lTrack = mTrackController.track();
		final var lNodeList = lTrack.nodes();

		final var lNodeCount = lNodeList.size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = lNodeList.get(i);
			if (lNode.isSelected) {
				GL11.glPointSize(8.f);
				Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lNode.x, lNode.y, -0.01f, 1.f, 1.f, 0.f, 1.f);
			} else {
				GL11.glPointSize(4.f);
				Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lNode.x, lNode.y, -0.01f, 1.f, 0.f, 0.f, 1.f);
			}
			mGameTextFont.drawText("N:" + lNode.uid, lNode.x, lNode.y - 8.f, -0.01f, ColorConstants.WHITE, .5f, -1);

		}
	}

	public void debugDrawSegments(LintfordCore core) {
		final var lTrack = mTrackController.track();
		final var lSegmentList = lTrack.segments();

		Debug.debugManager().drawers().beginLineRenderer(core.gameCamera(), GL11.GL_LINES, 2.f);

		final var lSegmentCount = lSegmentList.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = lSegmentList.get(i);

			final var lNodeA = lTrack.getNodeByUid(lSegment.nodeAUid);
			final var lNodeB = lTrack.getNodeByUid(lSegment.nodeBUid);
			{ // node angles
				if (lSegment.segmentType > 0) {
					final float lLineLength = 10.f;

					lSegment.nodeAAngle = (float) Math.atan2(lNodeA.y - lSegment.control0Y, lNodeA.x - lSegment.control0X);
					lSegment.nodeBAngle = (float) Math.atan2(lNodeB.y - lSegment.control1Y, lNodeB.x - lSegment.control1X);

					final float lNodeAAngleX = (float) Math.cos(lSegment.nodeAAngle);
					final float lNodeAAngleY = (float) Math.sin(lSegment.nodeAAngle);
					Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeA.x + lNodeAAngleX * lLineLength, lNodeA.y + lNodeAAngleY * lLineLength, 1.f, 0.f, 0.f);

					final float lNodeBAngleX = (float) Math.cos(lSegment.nodeBAngle);
					final float lNodeBAngleY = (float) Math.sin(lSegment.nodeBAngle);
					Debug.debugManager().drawers().drawLine(lNodeB.x, lNodeB.y, lNodeB.x + lNodeBAngleX * lLineLength, lNodeB.y + lNodeBAngleY * lLineLength, 1.f, 0.f, 0.f);

				}
			}
			Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y, 1.f, 1.f, 0.f);

			final float lWorldPositionX = (lNodeA.x + lNodeB.x) / 2.f;
			final float lWorldPositionY = (lNodeA.y + lNodeB.y) / 2.f;

			mGameTextFont.drawText("E:" + lSegment.uid, lWorldPositionX, lWorldPositionY - 5, -0.1f, ColorConstants.WHITE, .5f, -1);

			{ // Segment length (meters)
				final var lSegmentLength = lSegment.segmentLengthInMeters;

				// Calculate the center point of the segment
				final float lCenterX = (lNodeA.x + lNodeB.x) * 0.5f;
				final float lCenterY = (lNodeA.y + lNodeB.y) * 0.5f;

				mGameTextFont.drawText(String.format("%.1fm", lSegmentLength), lCenterX, lCenterY + mGameTextFont.fontHeight() * 0.4f, -0.01f, ColorConstants.WHITE, 0.5f, -1);
			}
		}
		Debug.debugManager().drawers().endLineRenderer();
	}

	// ---------------------------------------------

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
