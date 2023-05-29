package lintfordpickle.mailtrain.renderers;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.controllers.editor.EditorBrush;
import lintfordpickle.mailtrain.controllers.editor.EditorBrushController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackNode;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
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
	private EditorBrushController mEditorBrushController;

	private Texture mTextureStonebed;
	private Texture mTextureSleepers;
	private Texture mTextureMetal;
	private Texture mTextureBackplate;

	private FontUnit mGameTextFont;

	private LineBatch mLineBatch;

	protected int mTrackLogicalCounter;
	protected float mUiTextScale = 1.f;
	protected float mGameTextScale = .4f;

	private boolean mDrawEditorNodes;
	private boolean mDrawEditorSegments;
	private boolean mDrawEditorSignals;
	private boolean mDrawEditorJunctions;

	private boolean mLeftMouseDownTimed;
	private float mMouseWorldPositionX;
	private float mMouseWorldPositionY;
	private float mMouseGridPositionX;
	private float mMouseGridPositionY;

	private float mMouseTimer;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean drawEditorNodes() {
		return mDrawEditorNodes;
	}

	public void drawEditorNodes(boolean drawEditorNodes) {
		mDrawEditorNodes = drawEditorNodes;
	}

	public boolean drawEditorSegments() {
		return mDrawEditorSegments;
	}

	public void drawEditorSegments(boolean drawEditorSegments) {
		mDrawEditorSegments = drawEditorSegments;
	}

	public boolean drawEditorJunctions() {
		return mDrawEditorJunctions;
	}

	public void drawEditorJunctions(boolean drawEditorJunctions) {
		mDrawEditorJunctions = drawEditorJunctions;
	}

	public void drawEditorSignals(boolean newValue) {
		mDrawEditorSignals = newValue;
	}

	public boolean drawEditorSignals() {
		return mDrawEditorSignals;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	public float worldToGrid(final float pWorldCoord) {
		return RailTrackInstance.worldToGrid(pWorldCoord, TrackController.GRID_SIZE_DEPRECATED);
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
	public void initialize(LintfordCore core) {
		final var lControllerManager = core.controllerManager();

		mTrackEditorController = (TrackEditorController) lControllerManager.getControllerByNameRequired(TrackEditorController.CONTROLLER_NAME, entityGroupID());
		mEditorBrushController = (EditorBrushController) lControllerManager.getControllerByNameRequired(EditorBrushController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mTracksSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_ENVIRONMENT", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		mTextureStonebed = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_STONEBED", "res/textures/textureTrackStonebed.png", GL11.GL_LINEAR, entityGroupID());
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
	public boolean handleInput(LintfordCore core) {
		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R, this))
			mTrackLogicalCounter = -1;

		mLeftMouseDownTimed = core.input().mouse().isMouseLeftButtonDownTimed(this);

		mMouseWorldPositionX = core.gameCamera().getMouseWorldSpaceX();
		mMouseWorldPositionY = core.gameCamera().getMouseWorldSpaceY();
		mMouseGridPositionX = worldToGrid(mMouseWorldPositionX);
		mMouseGridPositionY = worldToGrid(mMouseWorldPositionY);

		if (core.input().mouse().isMouseOverThisComponent(hashCode()) == false)
			return false;

		final var lIsLayerActive = mEditorBrushController.isLayerActive(EditorLayer.Track);

		if (lIsLayerActive && mEditorBrushController.brush().isActionSet() == false) {
			if (handleCustomNodeInput(core))
				return true;

			if (mLeftMouseDownTimed) {
				if (mTrackEditorController.handleNodeSelection(mMouseWorldPositionX, mMouseWorldPositionY))
					return true;
			}

			if (handleSignalBoxes(core))
				return true;

			if (handleClearEditor(core))
				return true;

			if (handleEdgeSpecialCases(core))
				return true;

			if (handleSignalBlockControls(core))
				return true;
		}

		if (handleBrushActions(core))
			return true;

		var lInputHandled = super.handleInput(core);
		return lInputHandled;
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		if (mMouseTimer > 0.f)
			mMouseTimer -= (float) core.gameTime().elapsedTimeMilli();

		// Check if we need to rebuild the track mesh
		final boolean lInitialUpdate = mTrackLogicalCounter == -1;
		if (lInitialUpdate || mTrackLogicalCounter != mTrackEditorController.logicalUpdateCounter()) {
			loadTrackMesh(mTrackEditorController.track());

			mTrackLogicalCounter = mTrackEditorController.logicalUpdateCounter();

		}
	}

	@Override
	public void draw(LintfordCore core) {
		if (!mTrackEditorController.isInitialized())
			return;

		drawMesh(core, mTextureStonebed);
		drawMesh(core, mTextureSleepers);
		drawMesh(core, mTextureBackplate);
		drawMesh(core, mTextureMetal);

		if (mDrawEditorSegments)
			debugDrawEdges(core);

		if (mDrawEditorNodes)
			debugDrawNodes(core);

		if (mDrawEditorSignals)
			drawTrackSignalBlocks(core, mRendererManager.uiSpriteBatch(), mTrackEditorController.track());

		drawTrackInfo(core);

		drawEditorActions(core);
	}

	// ---------------------------------------------
	// Input Methods
	// ---------------------------------------------

	private boolean handleBrushActions(LintfordCore core) {
		final int lCurrentBrushAction = mEditorBrushController.brush().brushActionUid();
		if (lCurrentBrushAction != EditorBrush.NO_ACTION_UID) {
			switch (lCurrentBrushAction) {
			case TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_NODE:
				if (mLeftMouseDownTimed) {
					mTrackEditorController.moveSelectedANode(mMouseGridPositionX, mMouseGridPositionY);
					mEditorBrushController.finishAction(hashCode());
				}
				break;

			case TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_1:
				if (mLeftMouseDownTimed) {
					mTrackEditorController.moveSelectedSegmentControlNode1To(mMouseGridPositionX, mMouseGridPositionY);
					mEditorBrushController.finishAction(hashCode());
				}
				break;

			case TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_2:
				if (mLeftMouseDownTimed) {
					mTrackEditorController.moveSelectedSegmentControlNode2To(mMouseGridPositionX, mMouseGridPositionY);
					mEditorBrushController.finishAction(hashCode());
				}
				break;

			case TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_JUNCTION_BOX:
				if (mLeftMouseDownTimed) {
					mTrackEditorController.moveSelectedSegmentJunctionBoxTo(mMouseGridPositionX, mMouseGridPositionY);
					mEditorBrushController.finishAction(hashCode());
				}
				break;

			}
		}

		return false;
	}

	private boolean handleCustomNodeInput(LintfordCore core) {
		if (mEditorBrushController.isLayerActive(EditorLayer.Track) == false)
			return false;

		if (handleMoveTrackNode(core))
			return true;

		return false;
	}

	public boolean handleNodeCreation(float worldPositionX, float worldPositionY) {
		final var lMouseGridPositionX = worldToGrid(worldPositionX);
		final var lMouseGridPositionY = worldToGrid(worldPositionY);

		return mTrackEditorController.createNodeAt(lMouseGridPositionX, lMouseGridPositionY);
	}

	private boolean handleSignalBoxes(LintfordCore pCore) {
		final var lEdge = mTrackEditorController.getSelectedEdge();

		if (lEdge == null)
			return false;

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_U, this)) {
			mTrackEditorController.toggleSelectedSwitchMainLine();

		} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_Z, this)) {
			mTrackEditorController.toggleSelectedSwitchAuxiliaryLine();
		}

		return false;
	}

	private boolean handleMoveTrackNode(LintfordCore pCore) {
		if (mEditorBrushController.brush().isActionSet() == false) {
			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_M)) {
				mEditorBrushController.setAction(TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_NODE, "Move Selected Node", hashCode());
			}
		}

		return false;
	}

	private boolean handleClearEditor(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DELETE) && pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			mTrackEditorController.clearTrackEditor(pCore);

			// mScreenManager.toastManager().addMessage("Editor", "Cleared all track nodes", 1500);

			return true;
		}
		return false;
	}

	private boolean handleEdgeSpecialCases(LintfordCore pCore) {
		final var lActiveEdge = mTrackEditorController.getSelectedEdge(); // mSelectedNodeA.getEdgeByIndex(activeEdgeLocalIndex);

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_4, this)) {
			System.out.println("Setting player spawn edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_SPAWN);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_5, this)) {
			System.out.println("Setting player exit edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_EXIT);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_6, this)) {
			System.out.println("Setting map edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_EDGE);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_7, this)) {
			System.out.println("Setting station");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_STATION);
			return true;
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_8, this)) {
			System.out.println("Setting enemy spawn edge");
			lActiveEdge.setEdgeWithType(RailTrackSegment.EDGE_SPECIAL_TYPE_ENEMY_SPAWN);
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
		if (mTrackEditorController.editorPrimaryEdgeLocalIndex() == -1)
			return false;

		// Need to get the active node (A)
		if (mTrackEditorController.selectedNodeA() == null)
			return false;

		final var lSelectedNodeA = mTrackEditorController.selectedNodeA();
		final var lActiveEdge = lSelectedNodeA.trackSwitch.getConnectedSegmentByIndex(mTrackEditorController.editorPrimaryEdgeLocalIndex());

		// Check for signal creation
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_Q, this)) {
			lActiveEdge.addTrackSignal(mTrackEditorController.track(), /* RandomNumbers.random(0.f, 1.f) */ .5f, lActiveEdge.getOtherNodeUid(lSelectedNodeA.uid));

			mTrackEditorController.track().areSignalsDirty = true;

			// mScreenManager.toastManager().addMessage("", "Added signal to track segment " + lActiveEdge.uid, 1500);
		}

		return false;
	}

	// --- Actions
	public void setMoveSelectedNode() {

	}

	public void setMoveControlNodeA() {

	}

	public void setMoveControlNodeB() {

	}

	// ---------------------------------------------
	// Draw Methods
	// ---------------------------------------------

	private void drawEditorActions(LintfordCore core) {
		final var lCurrentActionUid = mEditorBrushController.brush().brushActionUid();
		if (lCurrentActionUid == EditorBrush.NO_ACTION_UID)
			return;

		if (lCurrentActionUid == TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_1) {
			final var lSelectedEdge = mTrackEditorController.getSelectedEdge();
			if (lSelectedEdge != null) {
				final float lWorldMouseX = core.gameCamera().getMouseWorldSpaceX();
				final float lWorldMouseY = core.gameCamera().getMouseWorldSpaceY();

				Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), lSelectedEdge.control0X, lSelectedEdge.control0Y, lWorldMouseX, lWorldMouseY);
			}

		} else if (lCurrentActionUid == TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_CONTROL_2) {
			final var lSelectedEdge = mTrackEditorController.getSelectedEdge();
			if (lSelectedEdge != null) {
				final float lWorldMouseX = core.gameCamera().getMouseWorldSpaceX();
				final float lWorldMouseY = core.gameCamera().getMouseWorldSpaceY();

				Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), lSelectedEdge.control1X, lSelectedEdge.control1Y, lWorldMouseX, lWorldMouseY);
			}

		} else if (lCurrentActionUid == TrackEditorController.CONTROLLER_EDITOR_ACTION_MOVE_NODE) {
			final var lSelectedNode = mTrackEditorController.selectedNodeA();
			if (lSelectedNode != null) {
				final float lWorldMouseX = core.gameCamera().getMouseWorldSpaceX();
				final float lWorldMouseY = core.gameCamera().getMouseWorldSpaceY();

				Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), lSelectedNode.x, lSelectedNode.y, lWorldMouseX, lWorldMouseY);
			}
		}

	}

	public void debugDrawNodes(LintfordCore core) {
		final var lTrack = mTrackEditorController.track();
		final var lNodeList = lTrack.nodes();

		final var lSelectedNodeA = mTrackEditorController.selectedNodeA();
		final var lSelectedNodeB = mTrackEditorController.selectedNodeB();

		GL11.glPointSize(4.f);

		mGameTextFont.begin(core.gameCamera());

		final var lNodeCount = lNodeList.size();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = lNodeList.get(i);

			if (lNode == lSelectedNodeA) {
				Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lNode.x, lNode.y, -0.01f, 0.f, 0.f, 1.f, 1.f);
			} else if (lNode == lSelectedNodeB) {
				Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lNode.x, lNode.y, -0.01f, 0.f, 1.f, 0.f, 1.f);
			} else {
				Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lNode.x, lNode.y, -0.01f, 1.f, 0.f, 0.f, 1.f);
			}
			mGameTextFont.drawText("n" + lNode.uid, lNode.x, lNode.y - 16.f, -0.01f, ColorConstants.WHITE, .4f, -1);

			//if (mDrawEditorJunctions)
				drawJunctionBox(core, mRendererManager.uiSpriteBatch(), mTrackEditorController.track(), lNode);

		}
		mGameTextFont.end();
	}

	public void debugDrawEdges(LintfordCore pCore) {
		final var lTrack = mTrackEditorController.track();
		final var lEdgeList = lTrack.edges();

		final var lSelectedNodeA = mTrackEditorController.selectedNodeA();

		final var lSelectedNodeMainLineEdgeUid = lSelectedNodeA != null ? lSelectedNodeA.trackSwitch.mainSegmentUid() : -1;
		final var lSelectedNodeAuxLineEdgeUid = lSelectedNodeA != null ? lSelectedNodeA.trackSwitch.activeAuxiliarySegmentUid() : -1;

		final var lEditorPrimaryHighlightIndex = lSelectedNodeA != null ? mTrackEditorController.editorPrimaryEdgeLocalIndex() : -1;
		final var lEditorSecondaryHighlightIndex = lSelectedNodeA != null ? mTrackEditorController.editorSecondaryEdgeLocalIndex() : -1;

		RailTrackSegment lHighlightEdge = null;
		RailTrackSegment lConstrainEdge = null;

		if (lSelectedNodeA != null && lEditorPrimaryHighlightIndex != -1)
			lHighlightEdge = lSelectedNodeA.trackSwitch.getConnectedSegmentByIndex(lEditorPrimaryHighlightIndex);

		if (lSelectedNodeA != null && lEditorSecondaryHighlightIndex != -1)
			lConstrainEdge = lSelectedNodeA.trackSwitch.getConnectedSegmentByIndex(lEditorSecondaryHighlightIndex);

		Debug.debugManager().drawers().beginLineRenderer(pCore.gameCamera(), GL11.GL_LINES, 2.f);

		mGameTextFont.begin(pCore.gameCamera());

		final var lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lEdgeList.get(i);

			var lNodeA = lTrack.getNodeByUid(lEdge.nodeAUid);
			var lNodeB = lTrack.getNodeByUid(lEdge.nodeBUid);

			var isMainLine = lSelectedNodeMainLineEdgeUid != -1 && lSelectedNodeMainLineEdgeUid == lEdge.uid;
			var isAuxiliaryLine = lSelectedNodeAuxLineEdgeUid != -1 && lSelectedNodeAuxLineEdgeUid == lEdge.uid;

			float lR = 1.f;
			float lG = 1.f;
			float lB = 1.f;
			if (lHighlightEdge != null && lHighlightEdge.uid == lEdge.uid)
				lG = 0.f;

			boolean lIsEdgeAllowed = false;
			if (lHighlightEdge != null && lConstrainEdge != null && lConstrainEdge.uid == lEdge.uid) {
				lB = 0.f;
				// TOOD: update this
				lIsEdgeAllowed = false; // lHighlightEdge.allowedEdgeConections.contains((Integer) lConstrainEdge.uid);
				if (lIsEdgeAllowed) {
					lR = 0.f;
					lG = 1.f;
					lB = 0.f;
				} else {
					lR = 1.f;
					lG = 0.f;
					lB = 0.f;
				}
			}

			if (lEdge.specialEdgeType != RailTrackSegment.EDGE_SPECIAL_TYPE_UNASSIGNED) {
				lR = 1.f;
				lG = 1.f;
				lB = 0.f;
			}

			if (lEdge.edgeType == RailTrackSegment.EDGE_TYPE_STRAIGHT) { // Straight edge
				Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y, lR, lG, lB);

				if (isMainLine) {
					final var lHx = lNodeA.x + (lNodeB.x - lNodeA.x) * .75f;
					final var lHY = lNodeA.y + (lNodeB.y - lNodeA.y) * .75f;

					Debug.debugManager().drawers().drawLine(lNodeA.x - 2, lNodeA.y - 2, lHx - 2, lHY - 2, 1, 1, 0);

				} else if (isAuxiliaryLine) {
					final var lHx = lNodeA.x + (lNodeB.x - lNodeA.x) * .75f;
					final var lHY = lNodeA.y + (lNodeB.y - lNodeA.y) * .75f;

					Debug.debugManager().drawers().drawLine(lNodeA.x - 2, lNodeA.y - 2, lHx - 2, lHY - 2, 1, 0, 1);
				}

			} else { // S-Curve
				float lLastX = lNodeA.x;
				float lLastY = lNodeA.y;
				for (float t = 0f; t <= 1.1f; t += 0.1f) {
					final float lNewPointX = MathHelper.bezier4CurveTo(t, lNodeA.x, lEdge.control0X, lEdge.control1X, lNodeB.x);
					final float lNewPointY = MathHelper.bezier4CurveTo(t, lNodeA.y, lEdge.control0Y, lEdge.control1Y, lNodeB.y);

					Debug.debugManager().drawers().drawLine(lLastX, lLastY, lNewPointX, lNewPointY, lR, lG, lB);

					lLastX = lNewPointX;
					lLastY = lNewPointY;
				}

				if (isMainLine) {
					final var lHx = lNodeA.x + (lNodeB.x - lNodeA.x) * .75f;
					final var lHY = lNodeA.y + (lNodeB.y - lNodeA.y) * .75f;

					Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lHx, lHY, 1, 1, 0);

				} else if (isAuxiliaryLine) {
					final var lHx = lNodeA.x + (lNodeB.x - lNodeA.x) * .75f;
					final var lHY = lNodeA.y + (lNodeB.y - lNodeA.y) * .75f;

					Debug.debugManager().drawers().drawLine(lNodeA.x, lNodeA.y, lHx, lHY, 1, 0, 1);
				}

				final var lIsPrimaryEdge = lHighlightEdge != null && lHighlightEdge.uid == lEdge.uid;
				final var lIsSecondaryEdge = lConstrainEdge != null && lConstrainEdge.uid == lEdge.uid;

				if (lIsPrimaryEdge || lIsSecondaryEdge/*lSelectedEdge != null && lSelectedEdge.uid == lEdge.uid*/) {
					Debug.debugManager().drawers().drawLine(lEdge.control0X, lEdge.control0Y, lNodeA.x, lNodeA.y, lR, lG, lB);
					Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lEdge.control0X, lEdge.control0Y, 5, 8, GL11.GL_LINE_STRIP, 1, 1, 1, 1);
					mGameTextFont.drawText("A", lEdge.control0X + 5, lEdge.control0Y, -0.1f, ColorConstants.WHITE, .4f, -1);

					Debug.debugManager().drawers().drawLine(lEdge.control1X, lEdge.control1Y, lNodeB.x, lNodeB.y, lR, lG, lB);
					Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lEdge.control1X, lEdge.control1Y, 5, 8, GL11.GL_LINE_STRIP, 1, 1, 1, 1);
					mGameTextFont.drawText("B", lEdge.control1X + 5, lEdge.control1Y, -0.1f, ColorConstants.WHITE, .4f, -1);
				}
			}

			final float lWorldPositionX = (lNodeA.x + lNodeB.x) / 2.f;
			final float lWorldPositionY = (lNodeA.y + lNodeB.y) / 2.f;

			debugDrawEdgeSignal(pCore, lTrack, lEdge);

			mGameTextFont.drawText("E:" + lEdge.uid, lWorldPositionX, lWorldPositionY, -0.1f, ColorConstants.WHITE, .4f, -1);

			drawEdgeInformation(pCore, mRendererManager.uiSpriteBatch(), lTrack, lEdge);
		}

		mGameTextFont.end();

		Debug.debugManager().drawers().endLineRenderer();
	}

	private void debugDrawEdgeSignal(LintfordCore pCore, RailTrackInstance pTrack, RailTrackSegment pEdge) {
		//		if (pEdge.trackJunction != null && pEdge.trackJunction.isSignalActive) {
		//			{
		//				final var lLeftEdgeUid = pEdge.trackJunction.leftEdgeUid;
		//				final var lLeftEdge = pTrack.getEdgeByUid(lLeftEdgeUid);
		//
		//				final int pCommonNodeUid = RailTrackSegment.getCommonNodeUid(pEdge, lLeftEdge);
		//
		//				final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);
		//				final var lOtherNodeUid = lLeftEdge.getOtherNodeUid(lActiveNode.uid);
		//				final var lOtherNode = pTrack.getNodeByUid(lOtherNodeUid);
		//				final float lVectorX = lOtherNode.x - lActiveNode.x;
		//				final float lVectorY = lOtherNode.y - lActiveNode.y;
		//
		//				var ll = new Vector2f(lVectorX, lVectorY);
		//				ll.nor();
		//
		//				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lActiveNode.x + ll.x * 20.f, lActiveNode.y + ll.y * 20.f, 5.f);
		//			}
		//
		//			{
		//				final var lRightEdgeUid = pEdge.trackJunction.rightEdgeUid;
		//				final var lRightEdge = pTrack.getEdgeByUid(lRightEdgeUid);
		//				if (lRightEdge == null) {
		//					return;
		//				}
		//				final int pCommonNodeUid = RailTrackSegment.getCommonNodeUid(pEdge, lRightEdge);
		//
		//				final var lActiveNode = pTrack.getNodeByUid(pCommonNodeUid);
		//				final var lOtherNodeUid = lRightEdge.getOtherNodeUid(lActiveNode.uid);
		//				final var lOtherNode = pTrack.getNodeByUid(lOtherNodeUid);
		//				final float lVectorX = lOtherNode.x - lActiveNode.x;
		//				final float lVectorY = lOtherNode.y - lActiveNode.y;
		//
		//				var ll = new Vector2f(lVectorX, lVectorY);
		//				ll.nor();
		//
		//				Debug.debugManager().drawers().drawCircleImmediate(pCore.gameCamera(), lActiveNode.x + ll.x * 20.f, lActiveNode.y + ll.y * 20.f, 3.f);
		//			}
		//		}
	}

	public void drawTrackInfo(LintfordCore pCore) {
		final var lHudRect = pCore.HUD().boundingRectangle();
		final var lFontUnit = mRendererManager.uiTextFont();

		final var lTrack = mTrackEditorController.track();
		final var lNodeCount = lTrack.getNumberTrackNodes();
		final var lEdgeCount = lTrack.getNumberTrackEdges();
		final var lSignals = lTrack.trackSignalSegments.instances().size();

		lFontUnit.begin(pCore.HUD());
		lFontUnit.drawText("Nodes: " + lNodeCount, lHudRect.right() - 120.f, lHudRect.top() + 10, -0.01f, ColorConstants.WHITE, 1.f);
		lFontUnit.drawText("Edges: " + lEdgeCount, lHudRect.right() - 120.f, lHudRect.top() + 30, -0.01f, ColorConstants.WHITE, 1.f);
		lFontUnit.drawText("SignalSegments: " + lSignals, lHudRect.right() - 180.f, lHudRect.top() + 50, -0.01f, ColorConstants.WHITE, 1.f);
		lFontUnit.end();
	}

	private void drawTrackSignalBlocks(LintfordCore pCore, TextureBatchPCT pTextureBatch, RailTrackInstance pTrack) {
		// TODO:
	}

	private void drawJunctionBox(LintfordCore core, TextureBatchPCT textureBatch, RailTrackInstance trackInstance, RailTrackNode activeNode) {
		if (activeNode == null || activeNode.trackSwitch.isSwitchActive() == false)
			return;

		final var lWorldTexture = mTracksSpriteSheet.texture();

		textureBatch.begin(core.gameCamera());

		{
			// Junction box
			final var lSignalBox = mTracksSpriteSheet.getSpriteFrame(mTracksSpriteSheet.getSpriteFrameIndexByName("TEXTURESIGNALBOX"));

			final float lBoxWorldX = activeNode.trackSwitch.signalBoxWorldX;
			final float lBoxWorldY = activeNode.trackSwitch.signalBoxWorldY;

			final float lWidth = lSignalBox.width();
			final float lHeight = lSignalBox.height();

			textureBatch.draw(lWorldTexture, lSignalBox, lBoxWorldX - lWidth * .5f, lBoxWorldY - lHeight * .5f, lWidth, lHeight, -0.01f, ColorConstants.WHITE);

		}
		textureBatch.end();
	}

	private void drawEdgeInformation(LintfordCore pCore, TextureBatchPCT pTextureBatch, RailTrackInstance pTrack, RailTrackSegment pActiveEdge) {
		final var lNodeA = pTrack.getNodeByUid(pActiveEdge.nodeAUid);
		final var lNodeB = pTrack.getNodeByUid(pActiveEdge.nodeBUid);

		// Calculate the center point of the edge
		final float lCenterX = (lNodeA.x + lNodeB.x) * 0.5f;
		final float lCenterY = (lNodeA.y + lNodeB.y) * 0.5f;

		if (pActiveEdge.specialEdgeType != RailTrackSegment.EDGE_SPECIAL_TYPE_UNASSIGNED) {
			String lSpecialType = "";
			if (pActiveEdge.isEdgeOfType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_SPAWN)) {
				lSpecialType += "PLAYER SPAWN";
			}

			if (pActiveEdge.isEdgeOfType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_EXIT)) {
				lSpecialType += "  PLAYER EXIT";
			}

			if (pActiveEdge.isEdgeOfType(RailTrackSegment.EDGE_SPECIAL_TYPE_MAP_EDGE)) {
				lSpecialType += "  MAP EDGE";
			}

			if (pActiveEdge.isEdgeOfType(RailTrackSegment.EDGE_SPECIAL_TYPE_STATION)) {
				lSpecialType += "  STATION";
			}

			final float lTextWidthHalf = mGameTextFont.getStringWidth(lSpecialType, mGameTextScale) * .5f;
			final float lTextHeight = mGameTextFont.getStringHeight(lSpecialType, mGameTextScale);

			mGameTextFont.drawText(lSpecialType, lCenterX - lTextWidthHalf, lCenterY - lTextHeight, -0.01f, ColorConstants.WHITE, mGameTextScale, -1);
		}

		if (pActiveEdge.segmentName != null && pActiveEdge.segmentName.length() > 0) {
			final float lTextWidthHalf = mGameTextFont.getStringWidth(pActiveEdge.segmentName, mGameTextScale) * .5f;
			final float lTextHeight = mGameTextFont.getStringHeight(pActiveEdge.segmentName, mGameTextScale);

			mGameTextFont.drawText(pActiveEdge.segmentName, lCenterX - lTextWidthHalf, lCenterY - lTextHeight * 2, -0.01f, ColorConstants.GREEN, mGameTextScale, -1);
		}

		if (pActiveEdge.specialName != null && pActiveEdge.specialName.length() > 0) {
			final float lTextWidthHalf = mGameTextFont.getStringWidth(pActiveEdge.specialName, mGameTextScale) * .5f;
			final float lTextHeight = mGameTextFont.getStringHeight(pActiveEdge.specialName, mGameTextScale);

			mGameTextFont.drawText(pActiveEdge.specialName, lCenterX - lTextWidthHalf, lCenterY - lTextHeight * 3, -0.01f, ColorConstants.GREEN, mGameTextScale, -1);
		}
	}

	// ---------------------------------------------
	// IInputProcessor
	// ---------------------------------------------

	@Override
	public boolean isCoolDownElapsed() {
		return mMouseTimer <= 0;
	}

	@Override
	public void resetCoolDownTimer() {
		mMouseTimer = 150;
	}

	@Override
	public boolean allowKeyboardInput() {
		return true;
	}

	@Override
	public boolean allowGamepadInput() {
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		return false;
	}

}
