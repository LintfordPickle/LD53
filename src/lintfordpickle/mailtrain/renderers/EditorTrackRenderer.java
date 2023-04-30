package lintfordpickle.mailtrain.renderers;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.track.TrackNode;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.batching.TextureBatchPCT;
import net.lintford.library.core.graphics.fonts.FontUnit;
import net.lintford.library.core.graphics.linebatch.LineBatch;
import net.lintford.library.core.graphics.shaders.ShaderSubPixel;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Matrix4f;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.renderers.RendererManager;

public class EditorTrackRenderer extends TrackMeshRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Track Editor Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SpriteSheetDefinition mTracksSpriteSheet;
	private TrackEditorController mTrackEditorController;

	private Texture mTextureSleepers;
	private Texture mTextureMetal;
	private Texture mTextureBackplate;

	private FontUnit mGameTextFont;

	private LineBatch mLineBatch;

	protected int mTrackLogicalCounter;
	protected float mUiTextScale = 1.f;
	protected float mGameTextScale = .4f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return false;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorTrackRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mLineBatch = new LineBatch();

		mShader = new ShaderSubPixel("TrackShader", VERT_FILENAME, FRAG_FILENAME);

		mModelMatrix = new Matrix4f();
		mTrackLogicalCounter = -1;
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

		mTracksSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_ENVIRONMENT", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		mTextureSleepers = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_SLEEPER", "res/textures/textureTrackSleepers.png", GL11.GL_LINEAR, entityGroupID());
		mTextureBackplate = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_BACKPLATE", "res/textures/textureTrackBackplate.png", GL11.GL_LINEAR, entityGroupID());
		mTextureMetal = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_METAL", "res/textures/textureTrackMetal.png", GL11.GL_LINEAR, entityGroupID());

		mGameTextFont = pResourceManager.fontManager().getFontUnit("FONT_GAME_TEXT");

		mLineBatch.loadResources(pResourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mLineBatch.unloadResources();
		mTracksSpriteSheet = null;
		mGameTextFont = null;
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R, this)) {
			mTrackLogicalCounter = -1;
		}
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		// Check if we need to rebuild the track mesh
		final boolean lInitialUpdate = mTrackLogicalCounter == -1;
		if (lInitialUpdate || mTrackLogicalCounter != mTrackEditorController.logicalUpdateCounter()) {
			loadTrackMesh(mTrackEditorController.track());

			mTrackLogicalCounter = mTrackEditorController.logicalUpdateCounter();

		}
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackEditorController.isInitialized())
			return;

//		drawMesh(pCore, mTextureStonebed);
		drawMesh(pCore, mTextureSleepers);
		drawMesh(pCore, mTextureBackplate);
		drawMesh(pCore, mTextureMetal);

		debugDrawEdges(pCore);
		debugDrawNodes(pCore);

		// Draw world origin
		Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), 0.f, 0.f);

		drawTrackSignalBlocks(pCore, mRendererManager.uiSpriteBatch(), mTrackEditorController.track());
		
		drawTrackInfo(pCore);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	// Debug ------------------------

	public void debugDrawNodes(LintfordCore pCore) {
		final var lTrack = mTrackEditorController.track();
		final var lNodeList = lTrack.nodes();

		final var lSelectedNodeA = mTrackEditorController.mSelectedNodeA;
		final var lSelectedNodeB = mTrackEditorController.mSelectedNodeB;

		final var lHudRect = pCore.HUD().boundingRectangle();
		if (lSelectedNodeA != null) {
			mGameTextFont.begin(pCore.HUD());
			mGameTextFont.drawText("Selected Node Uid A : " + lSelectedNodeA.uid, lHudRect.left() + 5, lHudRect.top() + 5, -0.1f, ColorConstants.WHITE, mUiTextScale, -1);
			mGameTextFont.end();

		}
		if (lSelectedNodeB != null) {
			mGameTextFont.begin(pCore.HUD());
			mGameTextFont.drawText("Selected Node Uid B : " + lSelectedNodeB.uid, lHudRect.left() + 5, lHudRect.top() + 25, -0.1f, ColorConstants.WHITE, mUiTextScale, -1);
			mGameTextFont.end();

		}
		GL11.glPointSize(4.f);

		mGameTextFont.begin(pCore.gameCamera());

		final var lNodeCount = lNodeList.size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = lNodeList.get(i);
			if (lNode == lSelectedNodeA) {
				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lNode.x, lNode.y, -0.01f, 0.f, 0.f, 1.f, 1.f);
			} else if (lNode == lSelectedNodeB) {
				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lNode.x, lNode.y, -0.01f, 0.f, 1.f, 0.f, 1.f);
			} else {
				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lNode.x, lNode.y, -0.01f, 1.f, 0.f, 0.f, 1.f);
			}
			mGameTextFont.drawText("n" + lNode.uid, lNode.x, lNode.y - 16.f, -0.01f, ColorConstants.WHITE, .4f, -1);

		}
		mGameTextFont.end();
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && lSelectedNodeB != null) {
			final float lWorldMouseX = pCore.gameCamera().getMouseWorldSpaceX();
			final float lWorldMouseY = pCore.gameCamera().getMouseWorldSpaceY();

			Debug.debugManager().drawers().drawLineImmediate(pCore.gameCamera(), lSelectedNodeB.x, lSelectedNodeB.y, lWorldMouseX, lWorldMouseY);
		} else if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && lSelectedNodeA != null) {
			final float lWorldMouseX = pCore.gameCamera().getMouseWorldSpaceX();
			final float lWorldMouseY = pCore.gameCamera().getMouseWorldSpaceY();

			Debug.debugManager().drawers().drawLineImmediate(pCore.gameCamera(), lSelectedNodeA.x, lSelectedNodeA.y, lWorldMouseX, lWorldMouseY);
		}
	}

	public void debugDrawEdges(LintfordCore pCore) {
		final var lHudBounds = pCore.HUD().boundingRectangle();

		final var lTrack = mTrackEditorController.track();
		final var lEdgeList = lTrack.edges();

		final var lSelectedNodeA = mTrackEditorController.mSelectedNodeA;
		final var lEdgeIndex = lSelectedNodeA != null ? mTrackEditorController.activeEdgeLocalIndex : -1;
		final var lEdgeIndexConstraint = lSelectedNodeA != null ? mTrackEditorController.auxilleryEdgeLocalIndex : -1;

		TrackSegment lHighlightEdge = null;
		TrackSegment lConstrainEdge = null;
		if (lSelectedNodeA != null && lEdgeIndex != -1) {
			lHighlightEdge = lSelectedNodeA.getEdgeByIndex(lEdgeIndex);
		}
		if (lSelectedNodeA != null && lEdgeIndexConstraint != -1) {
			lConstrainEdge = lSelectedNodeA.getEdgeByIndex(lEdgeIndexConstraint);

		}
		Debug.debugManager().drawers().beginLineRenderer(pCore.gameCamera(), GL11.GL_LINES, 2.f);

		mGameTextFont.begin(pCore.gameCamera());

		final var lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lEdgeList.get(i);

			float lTextOffsetY = 0.f;

			var lNodeA = lTrack.getNodeByUid(lEdge.nodeAUid);
			var lNodeB = lTrack.getNodeByUid(lEdge.nodeBUid);

			// Render on node is the top-left most node of the two
			TrackNode lRenderOnNode = lNodeA;

			float lR = 1.f;
			float lG = 1.f;
			float lB = 1.f;
			if (lHighlightEdge != null && lHighlightEdge.uid == lEdge.uid) {
				lG = 0.f;

			}
			boolean lShowConstraint = false;
			boolean lIsEdgeAllowed = false;
			int lEdgeType = 0;
			boolean lFlipped3 = false;
			if (lHighlightEdge != null && lConstrainEdge != null && lConstrainEdge.uid == lEdge.uid) {
				lB = 0.f;

				lEdgeType = lHighlightEdge.edgeType;

				lIsEdgeAllowed = lHighlightEdge.allowedEdgeConections.contains((Integer) lConstrainEdge.uid);
				lShowConstraint = true;
			}
			if (lEdge.specialEdgeType != TrackSegment.EDGE_SPECIAL_TYPE_UNASSIGNED) {
				lR = 1.f;
				lG = 1.f;
				lB = 0.f;

			}
			if (lEdge.edgeType == TrackSegment.EDGE_TYPE_STRAIGHT) { // Straight edge
				Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y, lR, lG, lB);

			} else { // S-Curve
				if (lNodeA.x > lNodeB.x) {
					var temp = lNodeB;
					lNodeB = lNodeA;
					lNodeA = temp;
				}
				if (lNodeA.y > lNodeB.y) {
					var temp = lNodeB;
					lNodeB = lNodeA;
					lNodeA = temp;
				}
				float lLastX = lNodeA.x;
				float lLastY = lNodeA.y;
				for (float t = 0f; t <= 1.1f; t += 0.1f) {
					final float lNewPointX = MathHelper.bezier4CurveTo(t, lNodeA.x, lEdge.lControl0X, lEdge.lControl1X, lNodeB.x);
					final float lNewPointY = MathHelper.bezier4CurveTo(t, lNodeA.y, lEdge.lControl0Y, lEdge.lControl1Y, lNodeB.y);

					Debug.debugManager().drawers().drawLine(lLastX, lLastY, lNewPointX, lNewPointY, lR, lG, lB);

					lLastX = lNewPointX;
					lLastY = lNewPointY;

				}
			}
			final float lWorldPositionX = (lNodeA.x + lNodeB.x) / 2.f;
			final float lWorldPositionY = (lNodeA.y + lNodeB.y) / 2.f;

			debugDrawEdgeSignal(pCore, lTrack, lEdge);

			drawJunctionBox(pCore, mRendererManager.uiSpriteBatch(), lTrack, lEdge);

			mGameTextFont.drawText("e" + lEdge.uid, lWorldPositionX, lWorldPositionY, -0.1f, ColorConstants.WHITE, .4f, -1);

			drawEdgeInformation(pCore, mRendererManager.uiSpriteBatch(), lTrack, lEdge);
			if (lShowConstraint) {
				lTextOffsetY += 8.f;
				mGameTextFont.drawText("allowed: " + (lIsEdgeAllowed ? "yes" : "no"), lRenderOnNode.x, lRenderOnNode.y + (lTextOffsetY += 8.f), -0.01f, ColorConstants.WHITE, mGameTextScale, -1);
				mGameTextFont.drawText("" + lEdgeType, lRenderOnNode.x, lRenderOnNode.y + (lTextOffsetY += 8.f), -0.01f, ColorConstants.WHITE, mGameTextScale, -1);
				mGameTextFont.drawText("" + lFlipped3, lRenderOnNode.x, lRenderOnNode.y + (lTextOffsetY += 8.f), -0.01f, ColorConstants.WHITE, mGameTextScale, -1);

			}
		}
		mGameTextFont.end();
		if (lHighlightEdge != null) {
			mGameTextFont.begin(pCore.HUD());
			mGameTextFont.drawText("Selected Edge Uid " + lHighlightEdge.uid, lHudBounds.left() + 5.f, lHudBounds.top() + 70.f, -0.01f, ColorConstants.WHITE, mUiTextScale, -1);
			mGameTextFont.end();
			if (lHighlightEdge.edgeType == TrackSegment.EDGE_TYPE_CURVE) {
				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lHighlightEdge.lControl0X, lHighlightEdge.lControl0Y, 5.f);
				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lHighlightEdge.lControl1X, lHighlightEdge.lControl1Y, 5.f);

			}
		}
		if (lConstrainEdge != null) {
			mGameTextFont.begin(pCore.HUD());
			mGameTextFont.drawText("Constrained Edge Uid " + lConstrainEdge.uid, lHudBounds.left() + 5.f, lHudBounds.top() + 90.f, -0.01f, ColorConstants.WHITE, mUiTextScale, -1);
			mGameTextFont.end();
		}
		Debug.debugManager().drawers().endLineRenderer();
	}

	private void debugDrawEdgeSignal(LintfordCore pCore, Track pTrack, TrackSegment pEdge) {
		if (pEdge.trackJunction != null && pEdge.trackJunction.isSignalActive) {
			{
				final var lLeftEdgeUid = pEdge.trackJunction.leftEdgeUid;
				final var lLeftEdge = pTrack.getEdgeByUid(lLeftEdgeUid);

				final int pCommonNodeUid = TrackSegment.getCommonNodeUid(pEdge, lLeftEdge);

				final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);
				final var lOtherNodeUid = lLeftEdge.getOtherNodeUid(lActiveNode.uid);
				final var lOtherNode = pTrack.getNodeByUid(lOtherNodeUid);
				final float lVectorX = lOtherNode.x - lActiveNode.x;
				final float lVectorY = lOtherNode.y - lActiveNode.y;

				var ll = new Vector2f(lVectorX, lVectorY);
				ll.nor();

				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lActiveNode.x + ll.x * 20.f, lActiveNode.y + ll.y * 20.f, 5.f);
			}
			{
				final var lRightEdgeUid = pEdge.trackJunction.rightEdgeUid;
				final var lRightEdge = pTrack.getEdgeByUid(lRightEdgeUid);
				if (lRightEdge == null) {
					return;
				}
				final int pCommonNodeUid = TrackSegment.getCommonNodeUid(pEdge, lRightEdge);

				final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);
				final var lOtherNodeUid = lRightEdge.getOtherNodeUid(lActiveNode.uid);
				final var lOtherNode = pTrack.getNodeByUid(lOtherNodeUid);
				final float lVectorX = lOtherNode.x - lActiveNode.x;
				final float lVectorY = lOtherNode.y - lActiveNode.y;

				var ll = new Vector2f(lVectorX, lVectorY);
				ll.nor();

				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lActiveNode.x + ll.x * 20.f, lActiveNode.y + ll.y * 20.f, 3.f);
			}
		}
	}

	// ------------------------

	public void drawTrackInfo(LintfordCore pCore) {
		final var lHudRect = pCore.HUD().boundingRectangle();
		final var lFontUnit = mRendererManager.uiTextFont();

		final var lNodeCount = mTrackEditorController.track().getNumberTrackNodes();
		final var lEdgeCount = mTrackEditorController.track().getNumberTrackEdges();

		lFontUnit.begin(pCore.HUD());
		lFontUnit.drawText("Nodes: " + lNodeCount, lHudRect.right() - 120.f, lHudRect.top() + 10, -0.01f, ColorConstants.WHITE, 1.f);
		lFontUnit.drawText("Edges: " + lEdgeCount, lHudRect.right() - 120.f, lHudRect.top() + 30, -0.01f, ColorConstants.WHITE, 1.f);
		lFontUnit.end();
	}

	// ------------------------

	private void drawTrackSignalBlocks(LintfordCore pCore, TextureBatchPCT pTextureBatch, Track pTrack) {
		// TODO:
	}

	private void drawJunctionBox(LintfordCore pCore, TextureBatchPCT pTextureBatch, Track pTrack, TrackSegment pActiveEdge) {
		final var lIsLeftSignalActive = pActiveEdge.trackJunction.leftEnabled;
		final var lActiveEdgeUid = lIsLeftSignalActive ? pActiveEdge.trackJunction.leftEdgeUid : pActiveEdge.trackJunction.rightEdgeUid;
		if (lActiveEdgeUid == -1)
			return;
		final var lActiveEdge = pTrack.getEdgeByUid(lActiveEdgeUid);
		if (lActiveEdge == null) {
			// FIXME: This still occurs during editing
			return;
		}
		final int pCommonNodeUid = TrackSegment.getCommonNodeUid(pActiveEdge, lActiveEdge);

		final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);

		final var lWorldTexture = mTracksSpriteSheet.texture();
		pTextureBatch.begin(pCore.gameCamera());
		{
			// signal post
			final var lSignalBounds = lIsLeftSignalActive ? mTracksSpriteSheet.getSpriteFrame(mTracksSpriteSheet.getSpriteFrameIndexByName("TEXTURESIGNALLEFT"))
					: mTracksSpriteSheet.getSpriteFrame(mTracksSpriteSheet.getSpriteFrameIndexByName("TEXTURESIGNALRIGHT"));

			final float lLampOffsetX = pActiveEdge.trackJunction.signalLampOffsetX;
			final float lLampOffsetY = pActiveEdge.trackJunction.signalLampOffsetY;

			final float lWidth = lSignalBounds.width();
			final float lHeight = lSignalBounds.height();

			pTextureBatch.draw(lWorldTexture, lSignalBounds, lActiveNode.x - lWidth * .5f + lLampOffsetX, lActiveNode.y - lHeight + lLampOffsetY, lWidth, lHeight, -0.01f, ColorConstants.WHITE);
		}
		{
			// signal box

			final var lSignalBox = mTracksSpriteSheet.getSpriteFrame(mTracksSpriteSheet.getSpriteFrameIndexByName("TEXTURESIGNALBOX"));

			final float lBoxOffsetX = pActiveEdge.trackJunction.signalBoxOffsetX;
			final float lBoxOffsetY = pActiveEdge.trackJunction.signalBoxOffsetY;

			final float lWidth = lSignalBox.width();
			final float lHeight = lSignalBox.height();

			pTextureBatch.draw(lWorldTexture, lSignalBox, lActiveNode.x - lWidth * .5f + lBoxOffsetX, lActiveNode.y - lHeight * .5f + lBoxOffsetY, lWidth, lHeight, -0.01f, ColorConstants.WHITE);

		}
		pTextureBatch.end();
	}

	private void drawEdgeInformation(LintfordCore pCore, TextureBatchPCT pTextureBatch, Track pTrack, TrackSegment pActiveEdge) {
		final var lNodeA = pTrack.getNodeByUid(pActiveEdge.nodeAUid);
		final var lNodeB = pTrack.getNodeByUid(pActiveEdge.nodeBUid);

		// Calculate the center point of the edge
		final float lCenterX = (lNodeA.x + lNodeB.x) * 0.5f;
		final float lCenterY = (lNodeA.y + lNodeB.y) * 0.5f;
		{ // Edge length (meters)
			final var lEdgeLength = pActiveEdge.edgeLengthInMeters;

			mGameTextFont.drawText(String.format("%.1fm", lEdgeLength), lCenterX, lCenterY + mGameTextFont.fontHeight() * mGameTextScale, -0.01f, ColorConstants.WHITE, mGameTextScale, -1);
		}

		if (pActiveEdge.specialEdgeType != TrackSegment.EDGE_SPECIAL_TYPE_UNASSIGNED) {
			String lSpecialType = "";
			if (pActiveEdge.isEdgeOfType(TrackSegment.EDGE_SPECIAL_TYPE_MAP_SPAWN)) {
				lSpecialType += "PLAYER SPAWN";
			}

			if (pActiveEdge.isEdgeOfType(TrackSegment.EDGE_SPECIAL_TYPE_MAP_EXIT)) {
				lSpecialType += "  PLAYER EXIT";
			}

			if (pActiveEdge.isEdgeOfType(TrackSegment.EDGE_SPECIAL_TYPE_MAP_EDGE)) {
				lSpecialType += "  MAP EDGE";
			}

			if (pActiveEdge.isEdgeOfType(TrackSegment.EDGE_SPECIAL_TYPE_STATION)) {
				lSpecialType += "  STATION";
			}

			final float lTextWidthHalf = mGameTextFont.getStringWidth(lSpecialType, mGameTextScale) * .5f;
			final float lTextHeight = mGameTextFont.getStringHeight(lSpecialType, mGameTextScale);

			mGameTextFont.drawText(lSpecialType, lCenterX - lTextWidthHalf, lCenterY - lTextHeight, -0.01f, ColorConstants.WHITE, mGameTextScale, -1);
		}

		if (pActiveEdge.segmentName != null && pActiveEdge.segmentName.length() > 0) {
			final float lTextWidthHalf = mGameTextFont.getStringWidth(pActiveEdge.segmentName, mGameTextScale) * .5f;
			final float lTextHeight = mGameTextFont.getStringHeight(pActiveEdge.segmentName, mGameTextScale);

			mGameTextFont.drawText(pActiveEdge.segmentName, lCenterX - lTextWidthHalf, lCenterY - lTextHeight * 2, -0.01f, ColorConstants.RED, mGameTextScale, -1);
		}

		if (pActiveEdge.specialName != null && pActiveEdge.specialName.length() > 0) {
			final float lTextWidthHalf = mGameTextFont.getStringWidth(pActiveEdge.specialName, mGameTextScale) * .5f;
			final float lTextHeight = mGameTextFont.getStringHeight(pActiveEdge.specialName, mGameTextScale);

			mGameTextFont.drawText(pActiveEdge.specialName, lCenterX - lTextWidthHalf, lCenterY - lTextHeight * 3, -0.01f, ColorConstants.GREEN, mGameTextScale, -1);
		}
	}

}
