package lintfordpickle.mailtrain.renderers;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.TrackEditorController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.linebatch.LineBatch;
import net.lintford.library.core.graphics.sprites.SpriteFrame;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class EditorSignalRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Debug Constants
	// ---------------------------------------------

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Signal Block Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SpriteSheetDefinition mTrackSpriteSheet; // junctions and signals
	private TrackEditorController mTrackEditorController;

	private SpriteFrame wiresTextureFrame;
	private SpriteFrame openSignalTextureFrame;

	private LineBatch mLineBatch;

	protected float mUiTextScale = 1.f;
	protected float mGameTextScale = .4f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mTrackEditorController != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorSignalRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mLineBatch = new LineBatch();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackEditorController = (TrackEditorController) pCore.controllerManager().getControllerByNameRequired(TrackEditorController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mTrackSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_ENVIRONMENT", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		wiresTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALWIRES");
		openSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSOPEN");

		mLineBatch.loadResources(pResourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mLineBatch.unloadResources();
		mTrackSpriteSheet = null;
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackEditorController.isInitialized())
			return;

		drawSignals(pCore);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawSignals(LintfordCore pCore) {
		final var lTrack = mTrackEditorController.track();
		final var lTextureBatch = mRendererManager.uiSpriteBatch();

		lTextureBatch.begin(pCore.gameCamera());
		final var lSignalList = lTrack.trackSignalSegments.instances();
		final int lNumSignals = lSignalList.size();
		for (int i = 0; i < lNumSignals; i++) {
			final var lSignal = lSignalList.get(i);

			if (!lSignal.isSignalHead)
				continue; // don't render the segments

			final var lTrackSegment = lSignal.trackSegment;
			if (lTrackSegment == null)
				continue;

			final var lTrackSegmentLength = lTrackSegment.edgeLengthInMeters;

			final var lDestNode = lTrack.getNodeByUid(lSignal.destinationNodeUid);
			if (lDestNode == null)
				return;
			final var lSourceNodeUid = lTrackSegment.getOtherNodeUid(lSignal.destinationNodeUid);
			final var lSourceNode = lTrack.getNodeByUid(lSourceNodeUid);

			if (lSourceNode == null)
				continue;

			final var lDistanceIntoNode = lSignal.startDistance;

			final float lW = openSignalTextureFrame.width();
			final float lH = openSignalTextureFrame.height();

			final float lVectorX = lSourceNode.x - lDestNode.x;
			final float lVectorY = lSourceNode.y - lDestNode.y;
			final float lRot = (float) Math.atan2(lVectorY, lVectorX);

			final float lNormX = lVectorX / lTrackSegmentLength;
			final float lNormY = lVectorY / lTrackSegmentLength;

			final float lWorldX = lTrack.getPositionAlongEdgeX(lTrackSegment, lSourceNodeUid, lDistanceIntoNode);
			final float lWorldY = lTrack.getPositionAlongEdgeY(lTrackSegment, lSourceNodeUid, lDistanceIntoNode);

			final float lOffAmtWires = -6.f;
			final float lOffAmtSignal = 10.f;
			final float lScale = 1.f;
			lTextureBatch.drawAroundCenter(mTrackSpriteSheet.texture(), wiresTextureFrame, lWorldX + lNormY * lOffAmtWires, lWorldY - lNormX * lOffAmtWires, wiresTextureFrame.width(), wiresTextureFrame.height(), -0.01f,
					lRot + (float) Math.PI, 0, 0, lScale, ColorConstants.WHITE);
			lTextureBatch.drawAroundCenter(mTrackSpriteSheet.texture(), openSignalTextureFrame, lWorldX + lNormY * lOffAmtSignal, lWorldY - lNormX * lOffAmtSignal, lW, lH, -0.01f, lRot, 0, 0, lScale,
					ColorConstants.WHITE);

		}
		lTextureBatch.end();
	}

}
