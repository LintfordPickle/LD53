package lintfordpickle.mailtrain.renderers.tracks;

import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.controllers.GameTrackEditorController;
import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.track.TrackNode;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import lintfordpickle.mailtrain.renderers.TrackMeshRenderer;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.renderers.RendererManager;

public class TrackGhostRenderer extends TrackMeshRenderer implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Track Ghost Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameTrackEditorController mGameTrackEditorController;
	private float mMouseCooldownTimer;

	private Texture mTextureGhostTrack;
	private final Track mGhostTrack = new Track();

	private TrackSegment mGhostSegment;
	private TrackNode mGhostNodeA;
	private TrackNode mGhostNodeB;

	private float mGhostEndPointX;
	private float mGhostEndPointY;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return true;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackGhostRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mGameTrackEditorController = (GameTrackEditorController) pCore.controllerManager().getControllerByNameRequired(GameTrackEditorController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mTextureGhostTrack = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_SLEEPER", "res/textures/textureTrackSleepers.png", GL11.GL_LINEAR, entityGroupID());

		mGhostNodeA = new TrackNode(mGhostTrack.getNewNodeUid());
		mGhostNodeB = new TrackNode(mGhostTrack.getNewNodeUid());
		mGhostTrack.nodes().add(mGhostNodeA);
		mGhostTrack.nodes().add(mGhostNodeB);

		final var lEdgeAngle = MathHelper.wrapAngle((float) Math.atan2(mGhostNodeA.y - mGhostNodeB.y, mGhostNodeA.x - mGhostNodeB.x));

		mGhostSegment = new TrackSegment(mGhostTrack, mGhostTrack.getNewEdgeUid(), mGhostNodeA.uid, mGhostNodeB.uid, lEdgeAngle);
		mGhostTrack.edges().add(mGhostSegment);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);
		if (mMouseCooldownTimer > 0) {
			mMouseCooldownTimer -= pCore.gameTime().elapsedTimeMilli();
		}
		final var selectedNodeA = mGameTrackEditorController.mSelectedNodeA;
		// final var selectedNodeB = mGameTrackEditorController.mSelectedNodeB;

		final var ghostNodeA = mGameTrackEditorController.ghostNodeA;
		// final var ghostNodeB = mGameTrackEditorController.ghostNodeB;
		if (selectedNodeA != null && ghostNodeA != null) {
			if (mGhostEndPointX != ghostNodeA.x || mGhostEndPointY != ghostNodeA.y) {
				mGhostNodeA.x = selectedNodeA.x;
				mGhostNodeA.y = selectedNodeA.y;

				mGhostNodeB.x = ghostNodeA.x;
				mGhostNodeB.y = ghostNodeA.y;

				// Track the ghost node for update changes
				mGhostEndPointX = ghostNodeA.x;
				mGhostEndPointY = ghostNodeA.y;

				loadTrackMesh(mGhostTrack);
			}
		}
	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lIsInEditMode = mGameTrackEditorController.isInEditorMode();
		
		final var ghostNodeA = mGameTrackEditorController.ghostNodeA;
		if (lIsInEditMode && ghostNodeA != null && ghostNodeA.isSelected) {
			GL11.glPointSize(6.f);
			Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), ghostNodeA.x, ghostNodeA.y, -0.01f, 1.f, 1.f, 0.f, 1.f);

			drawTrack(pCore, mGhostTrack);
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawTrack(LintfordCore pCore, Track pTrack) {
		drawMesh(pCore, mTextureGhostTrack);
	}

	@Override
	public boolean isCoolDownElapsed() {
		return mMouseCooldownTimer <= 0;
	}

	@Override
	public void resetCoolDownTimer() {
		mMouseCooldownTimer = 200;
	}



}
