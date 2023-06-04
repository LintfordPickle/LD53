package lintfordpickle.mailtrain.renderers.tracks;

import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.renderers.TrackMeshRenderer;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
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

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private AudioFireAndForgetManager mTrainSoundManager;

	private SpriteSheetDefinition mTrackSpriteSheet;
	private TrackController mTrackController;
	private Texture mTextureStonebed;
	private Texture mTextureSleepers;
	private Texture mTextureMetal;
	private Texture mTextureBackplate;

	private SpriteFrame wiresTextureFrame;

	private SpriteFrame closedSignalTextureFrame;
	private SpriteFrame warningSignalTextureFrame;
	private SpriteFrame openSignalTextureFrame;

	private float mLeftMouseCooldownTimer;
	private int mTrackLogicalCounter = -1;

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

	public TrackRenderer(RendererManager rendererManager, int entityGroupUid) {
		super(rendererManager, RENDERER_NAME, entityGroupUid);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		mTrackController = (TrackController) core.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mTrackSpriteSheet = resourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_ENVIRONMENT", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		mTextureStonebed = resourceManager.textureManager().loadTexture("TEXTURE_TRACK_STONEBED", "res/textures/textureTrackStonebed.png", GL11.GL_LINEAR, entityGroupID());
		mTextureSleepers = resourceManager.textureManager().loadTexture("TEXTURE_TRACK_SLEEPER", "res/textures/textureTrackSleepers.png", GL11.GL_LINEAR, entityGroupID());
		mTextureBackplate = resourceManager.textureManager().loadTexture("TEXTURE_TRACK_BACKPLATE", "res/textures/textureTrackBackplate.png", GL11.GL_LINEAR, entityGroupID());
		mTextureMetal = resourceManager.textureManager().loadTexture("TEXTURE_TRACK_METAL", "res/textures/textureTrackMetal.png", GL11.GL_LINEAR, entityGroupID());

		wiresTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALWIRES");

		closedSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSOCCUPIED");
		warningSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSWARNING");
		openSignalTextureFrame = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLIGHTSOPEN");

		resourceManager.audioManager().loadAudioFile("SOUND_SIGNAL_CHANGE", "res/sounds/soundSignalChange.wav", false);

		mTrainSoundManager = new AudioFireAndForgetManager(resourceManager.audioManager());
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
	public boolean handleInput(LintfordCore core) {
		mLeftMouseCooldownTimer -= core.appTime().elapsedTimeMilli();

		final float lMouseWorldSpaceX = core.gameCamera().getMouseWorldSpaceX();
		final float lMouseWorldSpaceY = core.gameCamera().getMouseWorldSpaceY();
		if (core.input().mouse().isMouseLeftButtonDownTimed(this)) {
			final var lTrack = mTrackController.track();

			// TODO: Track switching input handler
			// TODO: Add the switches and signal input handlers to the HashGrid

			final int lNodeCount = lTrack.nodes().size();
			for (int i = 0; i < lNodeCount; i++) {
				final var lNodeInst = lTrack.nodes().get(i);
				if (lNodeInst != null && lNodeInst.trackSwitch.isSwitchActive()) {

					final var lBoxPosX = lNodeInst.trackSwitch.signalBoxWorldX;
					final var lBoxPosY = lNodeInst.trackSwitch.signalBoxWorldY;
					if (lNodeInst != null && Vector2f.dst(lMouseWorldSpaceX, lMouseWorldSpaceY, lBoxPosX, lBoxPosY) < 10.f) {
						lNodeInst.trackSwitch.cycleSwitchAuxSegmentsForward();

						System.out.println("Toggled switch on node " + lNodeInst.uid + " to active aux line " + lNodeInst.trackSwitch.activeAuxiliarySegmentUid());

					}
				}
			}
		}
		return super.handleInput(core);
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);
		if (mTrackLogicalCounter != mTrackController.trackBuildLogicalCounter()) {
			loadTrackMesh(mTrackController.track());
			mTrackLogicalCounter = mTrackController.trackBuildLogicalCounter();
		}
	}

	@Override
	public void draw(LintfordCore core) {
		if (!mTrackController.isInitialized())
			return;

		final var lTrack = mTrackController.track();
		drawTrack(core, lTrack);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawTrack(LintfordCore core, RailTrackInstance trackInstance) {
		drawMesh(core, mTextureStonebed);
		drawMesh(core, mTextureSleepers);
		drawSignals(core);
		drawMesh(core, mTextureBackplate);
		drawMesh(core, mTextureMetal);

		final var lTrack = mTrackController.track();

		final var lNodeList = lTrack.nodes();
		final int lNodeCount = lNodeList.size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNodeInst = lNodeList.get(i);

			if (lNodeInst.trackSwitch.isSwitchActive())
				drawSwitchBox(core, mRendererManager.uiSpriteBatch(), lTrack, lNodeInst);
		}

		final var lSegmentList = lTrack.segments();
		final var lSegmentCount = lSegmentList.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = lSegmentList.get(i);
			drawSignalBox(core, mRendererManager.uiSpriteBatch(), lTrack, lSegment, null);

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

			if (lSignal.length() == 0 || lSignal.length() == 1)
				continue; // not active

			if (!lSignal.isSignalHead())
				continue; // don't render the segments

			final var lTrackSegment = lSignal.trackSegment;
			if (lTrackSegment == null)
				continue;

			final var lSignalBlock = lSignal.signalBlock;
			if (lSignalBlock == null)
				continue;

			final var lTrackSegmentLength = lTrackSegment.segmentLengthInMeters;

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

			final float lWorldX = lTrack.getPositionAlongSegmentX(lTrackSegment, lSourceNodeUid, lDistanceIntoNode);
			final float lWorldY = lTrack.getPositionAlongSegmentY(lTrackSegment, lSourceNodeUid, lDistanceIntoNode);

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

	private void drawSwitchBox(LintfordCore core, TextureBatchPCT textureBatch, RailTrackInstance trackInstance, RailTrackNode activeNode) {
		if (activeNode == null || activeNode.trackSwitch.isSwitchActive() == false)
			return;

		final var lWorldTexture = mTrackSpriteSheet.texture();

		textureBatch.begin(core.gameCamera());

		{
			// Junction box
			final var lSignalBox = mTrackSpriteSheet.getSpriteFrame(mTrackSpriteSheet.getSpriteFrameIndexByName("TEXTURESIGNALBOX"));

			final float lBoxWorldX = activeNode.trackSwitch.signalBoxWorldX;
			final float lBoxWorldY = activeNode.trackSwitch.signalBoxWorldY;

			final float lWidth = lSignalBox.width();
			final float lHeight = lSignalBox.height();

			textureBatch.draw(lWorldTexture, lSignalBox, lBoxWorldX - lWidth * .5f, lBoxWorldY - lHeight * .5f, lWidth, lHeight, -0.01f, ColorConstants.WHITE);

		}
		textureBatch.end();
	}

	private void drawSignalBox(LintfordCore core, TextureBatchPCT textureBatch, RailTrackInstance track, RailTrackSegment activeEdge, RailTrackNode trackNode) {
		//		final var lIsLeftSignalActive = pActiveEdge.trackJunction.leftEnabled;
		//		final var lActiveEdgeUid = lIsLeftSignalActive ? pActiveEdge.trackJunction.leftEdgeUid : pActiveEdge.trackJunction.rightEdgeUid;
		//		final var lActiveEdge = pTrack.getEdgeByUid(lActiveEdgeUid);
		//
		//		final int pCommonNodeUid = RailTrackSegment.getCommonNodeUid(pActiveEdge, lActiveEdge);
		//
		//		final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);
		//		final var lOtherNodeUid = lActiveEdge.getOtherNodeUid(lActiveNode.uid);
		//		final var lOtherNode = pTrack.getNodeByUid(lOtherNodeUid);
		//		final float lVectorX = lOtherNode.x - lActiveNode.x;
		//		final float lVectorY = lOtherNode.y - lActiveNode.y;
		//
		//		TempTrackVec2.set(lVectorX, lVectorY);
		//		TempTrackVec2.nor();
		//
		//		final var lWorldTexture = mTrackSpriteSheet.texture();
		//
		//		final var lSignalArrow = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALARROW");
		//		final var lSignalArrowAngle = (float) Math.atan2(TempTrackVec2.y, TempTrackVec2.x) + (float) Math.toRadians(90.f);
		//		{
		//			final float lSrcX = lSignalArrow.x();
		//			final float lSrcY = lSignalArrow.y();
		//			final float lSrcW = lSignalArrow.width();
		//			final float lSrcH = lSignalArrow.height();
		//
		//			pTextureBatch.drawAroundCenter(lWorldTexture, lSrcX, lSrcY, lSrcW, lSrcH, lActiveNode.x, lActiveNode.y, lSrcW, lSrcH, -0.1f, lSignalArrowAngle, .0f, lSrcH * .5f, 1.f, ColorConstants.WHITE);
		//		}
		//		{
		//			// signal lamp
		//
		//			final float lLampOffsetX = pActiveEdge.trackJunction.signalLampWorldX;
		//			final float lLampOffsetY = pActiveEdge.trackJunction.signalLampWorldY;
		//
		//			final var lSignalBounds = lIsLeftSignalActive ? mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALLEFT") : mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALRIGHT");
		//
		//			final float lLampWidth = lSignalBounds.width();
		//			final float lLampHeight = lSignalBounds.height();
		//
		//			pTextureBatch.draw(lWorldTexture, lSignalBounds, lActiveNode.x - 16.f + lLampOffsetX, lActiveNode.y - 32.f + lLampOffsetY, lLampWidth, lLampHeight, -0.1f, ColorConstants.WHITE);
		//
		//		}
		//		{
		//			// signal box (clickable bit)
		//
		//			final float lBoxOffsetX = pActiveEdge.trackJunction.signalBoxWorldX;
		//			final float lBoxOffsetY = pActiveEdge.trackJunction.signalBoxWorldY;
		//
		//			final var lSignalBounds = mTrackSpriteSheet.getSpriteFrame("TEXTURESIGNALBOX");
		//
		//			final float lBoxWidth = lSignalBounds.width();
		//			final float lBoxHeight = lSignalBounds.height();
		//
		//			pTextureBatch.draw(lWorldTexture, lSignalBounds, lActiveNode.x - lBoxWidth * .5f + lBoxOffsetX, lActiveNode.y - lBoxHeight * .5f + lBoxOffsetY, lBoxWidth, lBoxHeight, -0.1f, ColorConstants.WHITE);
		//
		//		}
	}
}
