package lintfordpickle.mailtrain.renderers.tracks;

import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.GameTrackEditorController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.renderers.TrackMeshRenderer;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.batching.TextureBatchPCT;
import net.lintford.library.core.graphics.sprites.SpriteFrame;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.renderers.RendererManager;

public class TrackRenderer extends TrackMeshRenderer implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Track Renderer";

	private static final Vector2f TempTrackVec2 = new Vector2f();

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private AudioFireAndForgetManager mTrainSoundManager;

	private SpriteSheetDefinition mTrackSpriteSheet;
	private TrackController mTrackController;
	private GameTrackEditorController mGameTrackEditorcontroller;
	private Texture mTextureStonebed;
	private Texture mTextureSleepers;
	private Texture mTextureMetal;
	private Texture mTextureBackplate;

	private SpriteFrame wiresTextureFrame;

	private SpriteFrame closedSignalTextureFrame;
	private SpriteFrame warningSignalTextureFrame;
	private SpriteFrame openSignalTextureFrame;

	private float mLeftMouseCooldownTimer;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 2;
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

	public TrackRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
		mGameTrackEditorcontroller = (GameTrackEditorController) pCore.controllerManager().getControllerByNameRequired(GameTrackEditorController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mTrackSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_ENVIRONMENT", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		mTextureStonebed = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_STONEBED", "res/textures/textureTrackStonebed.png", GL11.GL_LINEAR, entityGroupID());
		mTextureSleepers = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_SLEEPER", "res/textures/textureTrackSleepers.png", GL11.GL_LINEAR, entityGroupID());
		mTextureBackplate = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_BACKPLATE", "res/textures/textureTrackBackplate.png", GL11.GL_LINEAR, entityGroupID());
		mTextureMetal = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_METAL", "res/textures/textureTrackMetal.png", GL11.GL_LINEAR, entityGroupID());

		wiresTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALWIRES");

		closedSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSOCCUPIED");
		warningSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSWARNING");
		openSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSOPEN");

		pResourceManager.audioManager().loadAudioFile("SOUND_SIGNAL_CHANGE", "res/sounds/soundSignalChange.wav", false);

		mTrainSoundManager = new AudioFireAndForgetManager(pResourceManager.audioManager());
		mTrainSoundManager.acquireAudioSources(2);

		loadTrackMesh(mTrackController.track());
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mTextureStonebed = null;
		mTextureSleepers = null;
		mTextureBackplate = null;
		mTextureMetal = null;

		mTrainSoundManager.unassign();
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		mLeftMouseCooldownTimer -= pCore.appTime().elapsedTimeMilli();

		final float lMouseWorldSpaceX = pCore.gameCamera().getMouseWorldSpaceX();
		final float lMouseWorldSpaceY = pCore.gameCamera().getMouseWorldSpaceY();
		if (pCore.input().mouse().isMouseLeftButtonDownTimed(this)) {
			final var lTrack = mTrackController.track();

			final int lEdgeCount = lTrack.edges().size();
			for (int i = 0; i < lEdgeCount; i++) {
				final var lEdge = lTrack.edges().get(i);
				if (lEdge != null && lEdge.trackJunction != null && lEdge.trackJunction.isSignalActive) {
					final var lSignalNode = lTrack.getNodeByUid(lEdge.trackJunction.signalNodeUid);

					final var lBoxPosX = lSignalNode.x + lEdge.trackJunction.signalBoxWorldX;
					final var lBoxPosY = lSignalNode.y + lEdge.trackJunction.signalBoxWorldY;
					if (lEdge != null && Vector2f.dst(lMouseWorldSpaceX, lMouseWorldSpaceY, lBoxPosX, lBoxPosY) < 10.f) {
						lEdge.trackJunction.toggleSignal();

						final var lBoxWorldPositionX = lSignalNode.x;
						final var lBoxWorldPositionY = lSignalNode.y;

						if (ConstantsGame.SOUNDS_ENABLED)
							mTrainSoundManager.play("SOUND_SIGNAL_CHANGE", lBoxWorldPositionX, lBoxWorldPositionY, 0.f, 0.f);

					}
				}
			}
		}
		return super.handleInput(pCore);
	}

	private int mTrackLogicalCounter = -1;

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);
		if (mTrackLogicalCounter != mTrackController.trackBuildLogicalCounter()) {
			loadTrackMesh(mTrackController.track());
			mTrackLogicalCounter = mTrackController.trackBuildLogicalCounter();
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackController.isInitialized())
			return;

		final var lTrack = mTrackController.track();
		drawTrack(pCore, lTrack);

		final var lSelectedtRackSegment = mGameTrackEditorcontroller.selectedTrackSegment;
		if (lSelectedtRackSegment != null) {
			final var lNodeA = mTrackController.track().getNodeByUid(lSelectedtRackSegment.nodeAUid);
			final var lNodeB = mTrackController.track().getNodeByUid(lSelectedtRackSegment.nodeBUid);
			if (lNodeA != null && lNodeB != null) {
				final float p0x = lNodeA.x;
				final float p0y = lNodeA.y;
				final float p1x = lNodeB.x;
				final float p1y = lNodeB.y;

				Debug.debugManager().drawers().drawLineImmediate(pCore.gameCamera(), p0x, p0y, p1x, p1y, -0.01f, .95f, .04f, .03f);
			}
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawTrack(LintfordCore pCore, RailTrackInstance pTrack) {
		drawMesh(pCore, mTextureStonebed);
		drawMesh(pCore, mTextureSleepers);
		drawSignals(pCore);
		drawMesh(pCore, mTextureBackplate);
		drawMesh(pCore, mTextureMetal);

		final var lTrack = mTrackController.track();
		final var lEdgeList = lTrack.edges();

		final var lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lEdgeList.get(i);

			if (lEdge.trackJunction.isSignalActive)
				drawJunctionBox(pCore, mRendererManager.uiSpriteBatch(), lTrack, lEdge);
		}
	}

	private void drawSignals(LintfordCore pCore) {
		final var lTrack = mTrackController.track();
		final var lTextureBatch = mRendererManager.uiSpriteBatch();

		if (lTrack.areSignalsDirty)
			return; // need to wait for the signal blocks to be built

		lTextureBatch.begin(pCore.gameCamera());
		final var lSignalList = lTrack.trackSignalSegments.instances();
		final int lNumSignals = lSignalList.size();
		for (int i = 0; i < lNumSignals; i++) {
			final var lSignal = lSignalList.get(i);

			if (!lSignal.isSignalHead())
				continue; // don't render the segments

			final var lTrackSegment = lSignal.trackSegment;
			if (lTrackSegment == null)
				continue;

			final var lSignalBlock = lSignal.signalBlock;
			if (lSignalBlock == null)
				continue;

			final var lTrackSegmentLength = lTrackSegment.edgeLengthInMeters;

			final var lDestNode = lTrack.getNodeByUid(lSignal.destinationNodeUid());
			final var lSourceNodeUid = lTrackSegment.getOtherNodeUid(lSignal.destinationNodeUid());
			final var lSourceNode = lTrack.getNodeByUid(lSourceNodeUid);

			if (lSourceNode == null || lDestNode == null)
				continue;

			final var lDistanceIntoNode = lSignal.startDistance();

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
			final float lOffAmtSignal = 10f;

			lTextureBatch.drawAroundCenter(mTrackSpriteSheet.texture(), wiresTextureFrame, lWorldX + lNormY * lOffAmtWires, lWorldY - lNormX * lOffAmtWires, wiresTextureFrame.width(), wiresTextureFrame.height(), -0.01f,
					lRot + (float) Math.PI, 0, 0, 1.f, ColorConstants.WHITE);

			if (lSignalBlock.signalState() == SignalState.Occupied)
				lTextureBatch.drawAroundCenter(mTrackSpriteSheet.texture(), closedSignalTextureFrame, lWorldX + lNormY * lOffAmtSignal, lWorldY - lNormX * lOffAmtSignal, lW, lH, -0.01f, lRot, 0, 0, 1.f,
						ColorConstants.WHITE);
			else if (lSignalBlock.signalState() == SignalState.Warning)
				lTextureBatch.drawAroundCenter(mTrackSpriteSheet.texture(), warningSignalTextureFrame, lWorldX + lNormY * lOffAmtSignal, lWorldY - lNormX * lOffAmtSignal, lW, lH, -0.01f, lRot, 0, 0, 1.f,
						ColorConstants.WHITE);
			else
				lTextureBatch.drawAroundCenter(mTrackSpriteSheet.texture(), openSignalTextureFrame, lWorldX + lNormY * lOffAmtSignal, lWorldY - lNormX * lOffAmtSignal, lW, lH, -0.01f, lRot, 0, 0, 1.f,
						ColorConstants.WHITE);

		}
		lTextureBatch.end();
	}

	private void drawJunctionBox(LintfordCore pCore, TextureBatchPCT pTextureBatch, RailTrackInstance pTrack, RailTrackSegment pActiveEdge) {
		final var lIsLeftSignalActive = pActiveEdge.trackJunction.leftEnabled;
		final var lActiveEdgeUid = lIsLeftSignalActive ? pActiveEdge.trackJunction.leftEdgeUid : pActiveEdge.trackJunction.rightEdgeUid;
		if (lActiveEdgeUid == -1)
			return;
		final var lActiveEdge = pTrack.getEdgeByUid(lActiveEdgeUid);
		if (lActiveEdge == null) {
			// FIXME: This stil occurs during editing
			return;
		}
		final int pCommonNodeUid = RailTrackSegment.getCommonNodeUid(pActiveEdge, lActiveEdge);

		final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);

		final var lWorldTexture = mTrackSpriteSheet.texture();
		pTextureBatch.begin(pCore.gameCamera());
		{
			// signal post

			final var lSignalBounds = lIsLeftSignalActive ? mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLEFT") : mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALRIGHT");

			final float lLampOffsetX = pActiveEdge.trackJunction.signalLampWorldX;
			final float lLampOffsetY = pActiveEdge.trackJunction.signalLampWorldY;

			final float lWidth = lSignalBounds.width();
			final float lHeight = lSignalBounds.height();

			pTextureBatch.draw(lWorldTexture, lSignalBounds, lActiveNode.x - lWidth * .5f + lLampOffsetX, lActiveNode.y - lHeight + lLampOffsetY, lWidth, lHeight, -0.01f, ColorConstants.WHITE);
		}
		{
			// signal box

			final var lSignalBox = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALBOX");

			final float lBoxOffsetX = pActiveEdge.trackJunction.signalBoxWorldX;
			final float lBoxOffsetY = pActiveEdge.trackJunction.signalBoxWorldY;

			final float lWidth = lSignalBox.width();
			final float lHeight = lSignalBox.height();

			pTextureBatch.draw(lWorldTexture, lSignalBox, lActiveNode.x - lWidth * .5f + lBoxOffsetX, lActiveNode.y - lHeight * .5f + lBoxOffsetY, lWidth, lHeight, -0.01f, ColorConstants.WHITE);

		}
		pTextureBatch.end();
	}

	private void drawSignalBox(LintfordCore pCore, TextureBatchPCT pTextureBatch, RailTrackInstance pTrack, RailTrackSegment pActiveEdge, RailTrackNode pTrackNode) {
		final var lIsLeftSignalActive = pActiveEdge.trackJunction.leftEnabled;
		final var lActiveEdgeUid = lIsLeftSignalActive ? pActiveEdge.trackJunction.leftEdgeUid : pActiveEdge.trackJunction.rightEdgeUid;
		final var lActiveEdge = pTrack.getEdgeByUid(lActiveEdgeUid);

		final int pCommonNodeUid = RailTrackSegment.getCommonNodeUid(pActiveEdge, lActiveEdge);

		final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);
		final var lOtherNodeUid = lActiveEdge.getOtherNodeUid(lActiveNode.uid);
		final var lOtherNode = pTrack.getNodeByUid(lOtherNodeUid);
		final float lVectorX = lOtherNode.x - lActiveNode.x;
		final float lVectorY = lOtherNode.y - lActiveNode.y;

		TempTrackVec2.set(lVectorX, lVectorY);
		TempTrackVec2.nor();

		final var lWorldTexture = mTrackSpriteSheet.texture();

		final var lSignalArrow = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALARROW");
		final var lSignalArrowAngle = (float) Math.atan2(TempTrackVec2.y, TempTrackVec2.x) + (float) Math.toRadians(90.f);
		{
			final float lSrcX = lSignalArrow.x();
			final float lSrcY = lSignalArrow.y();
			final float lSrcW = lSignalArrow.width();
			final float lSrcH = lSignalArrow.height();

			pTextureBatch.drawAroundCenter(lWorldTexture, lSrcX, lSrcY, lSrcW, lSrcH, lActiveNode.x, lActiveNode.y, lSrcW, lSrcH, -0.1f, lSignalArrowAngle, .0f, lSrcH * .5f, 1.f, ColorConstants.WHITE);
		}
		{
			// signal lamp

			final float lLampOffsetX = pActiveEdge.trackJunction.signalLampWorldX;
			final float lLampOffsetY = pActiveEdge.trackJunction.signalLampWorldY;

			final var lSignalBounds = lIsLeftSignalActive ? mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLEFT") : mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALRIGHT");

			final float lLampWidth = lSignalBounds.width();
			final float lLampHeight = lSignalBounds.height();

			pTextureBatch.draw(lWorldTexture, lSignalBounds, lActiveNode.x - 16.f + lLampOffsetX, lActiveNode.y - 32.f + lLampOffsetY, lLampWidth, lLampHeight, -0.1f, ColorConstants.WHITE);

		}
		{
			// signal box (clickable bit)

			final float lBoxOffsetX = pActiveEdge.trackJunction.signalBoxWorldX;
			final float lBoxOffsetY = pActiveEdge.trackJunction.signalBoxWorldY;

			final var lSignalBounds = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALBOX");

			final float lBoxWidth = lSignalBounds.width();
			final float lBoxHeight = lSignalBounds.height();

			pTextureBatch.draw(lWorldTexture, lSignalBounds, lActiveNode.x - lBoxWidth * .5f + lBoxOffsetX, lActiveNode.y - lBoxHeight * .5f + lBoxOffsetY, lBoxWidth, lBoxHeight, -0.1f, ColorConstants.WHITE);

		}
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
