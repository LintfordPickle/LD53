package lintfordpickle.mailtrain.renderers.trains;

import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.controllers.trains.PlayerTrainController;
import lintfordpickle.mailtrain.controllers.trains.TrainController;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.trains.Train;
import lintfordpickle.mailtrain.data.scene.trains.TrainCar;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrainRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Train Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrackController mTrackController;
	private TrainController mTrainController;
	private PlayerTrainController mPlayerTrainController;

	private SpriteSheetDefinition mTrainsSpriteSheet;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 3;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrainController = (TrainController) pCore.controllerManager().getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupID());
		mPlayerTrainController = (PlayerTrainController) pCore.controllerManager().getControllerByNameRequired(PlayerTrainController.CONTROLLER_NAME, entityGroupID());

		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mTrainsSpriteSheet = pResourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_TRAINS", ConstantsGame.GAME_RESOURCE_GROUP_ID);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrainController.isInitialized())
			return;

		final var lTrack = mTrackController.track();

		final var lTrainManager = mTrainController.trainManager();
		final var lActiveTrains = lTrainManager.activeTrains();
		final int lNumTrains = lActiveTrains.size();
		for (int i = 0; i < lNumTrains; i++) {
			final var lTrain = lActiveTrains.get(i);

			drawTrain(pCore, lTrack, lTrain);
		}

		if (ConstantsGame.DEBUG_DRAW_PLAYER_TRAIN_STATS) {
			debugDrawTrainInformation(pCore, mPlayerTrainController.playerLocomotive());
			debugDrawTrainSpeedInformation(pCore, lTrack, mPlayerTrainController.playerLocomotive());
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawTrain(LintfordCore pCore, RailTrackInstance pTrack, Train pTrain) {
		final var lTextureBatch = mRendererManager.uiSpriteBatch();

		// draw the train on the front axle

		// TODO: Fix the texturebatch call order - its the same texture - don't start inside the loop (only done for debug purposes).

		final int lNumCarsInTrain = pTrain.getNumberOfCarsInTrain();
		for (int i = 0; i < lNumCarsInTrain; i++) {
			lTextureBatch.begin(pCore.gameCamera());

			final var lTrainCar = pTrain.getCarByIndex(i);
			final var lCarriageDefinition = lTrainCar.definition();

			final float lDestX = lTrainCar.frontAxle.worldPositionX;
			final float lDestY = lTrainCar.frontAxle.worldPositionY;
			final float lDestW = 64;
			final float lDestH = 32;

			final float lVectorX = lTrainCar.frontAxle.worldPositionX - lTrainCar.rearAxle.worldPositionX;
			final float lVectorY = lTrainCar.frontAxle.worldPositionY - lTrainCar.rearAxle.worldPositionY;

			final float lAngle = (float) Math.atan2(lVectorY, lVectorX);
			final float lRotX = 32.f - 8.f;
			final float lRotY = 0.f;

			final float lAlpha = ConstantsGame.DEBUG_DRAW_AXLE_POINTS ? 0.7f : 1f;
			final var lWhiteWithAlpha = ColorConstants.getWhiteWithAlpha(lAlpha);

			final var lSrcFrame = mTrainsSpriteSheet.getSpriteFrame(lCarriageDefinition.carriageBaseSpriteFrameName);

			if (lSrcFrame != null) {
				lTextureBatch.drawAroundCenter(mTrainsSpriteSheet.texture(), lSrcFrame, lDestX, lDestY, lDestW, lDestH, -0.3f, lAngle, lRotX, lRotY, 1, lWhiteWithAlpha);
			}

			lTextureBatch.end();

			lCarriageDefinition.drawCarriageInstance(pCore, mTrainsSpriteSheet, lTextureBatch, lTrainCar);

			debugDrawTrainAxlePoints(pCore, pTrack, lTrainCar);

			GL11.glPointSize(6.f);
			Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), 0, 0);
			GL11.glPointSize(2.f);
		}
	}

	private void debugDrawTrainAxlePoints(LintfordCore pCore, RailTrackInstance pTrack, TrainCar pTrainCar) {
		if (!ConstantsGame.DEBUG_DRAW_AXLE_POINTS)
			return;

		GL11.glPointSize(11.f);

		final var lCurrentEdge = pTrainCar.frontAxle.currentEdge;
		final var lOriginNodeUid = lCurrentEdge.getOtherNodeUid(pTrainCar.frontAxle.destinationNodeUid);
		final var lDistIntoTrack = pTrainCar.frontAxle.normalizedDistanceAlongEdge;

		final var lWorldPosX = pTrack.getPositionAlongEdgeX(lCurrentEdge, lOriginNodeUid, lDistIntoTrack);
		final var lWorldPosY = pTrack.getPositionAlongEdgeY(lCurrentEdge, lOriginNodeUid, lDistIntoTrack);

		Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lWorldPosX, lWorldPosY, -0.01f, 1f, 1.f, 0.f, 1.f);

		final var lCurrentEdge1 = pTrainCar.rearAxle.currentEdge;
		final var lOriginNodeUid1 = lCurrentEdge1.getOtherNodeUid(pTrainCar.rearAxle.destinationNodeUid);
		final var lDistIntoTrack1 = pTrainCar.rearAxle.normalizedDistanceAlongEdge;

		final var lWorldPosX1 = pTrack.getPositionAlongEdgeX(lCurrentEdge1, lOriginNodeUid1, lDistIntoTrack1);
		final var lWorldPosY1 = pTrack.getPositionAlongEdgeY(lCurrentEdge1, lOriginNodeUid1, lDistIntoTrack1);
		Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lWorldPosX1, lWorldPosY1, -0.01f, 1f, 0.5f, 0.f, 1.f);
	}

	private void debugDrawTrainSpeedInformation(LintfordCore pCore, RailTrackInstance pTrack, Train pTrain) {
		if (!ConstantsGame.DEBUG_DRAW_TRAIN_DEBUG_SPEED_INFO)
			return;

		if (pTrain == null)
			return;

		final float lDelta = (float) pCore.gameTime().elapsedTimeMilli() * 0.001f;

		final var lFontUnit = mRendererManager.uiTextFont();
		final var lHudRect = pCore.HUD().boundingRectangle();

		float lTextPositionX = lHudRect.left() + 5.f;
		float lTextPositionY = lHudRect.top() + 50.f;

		lFontUnit.begin(pCore.HUD());

		final var lLeadAxle = pTrain.leadCar.frontAxle;
		final var lDestinationNode = pTrack.getNodeByUid(lLeadAxle.destinationNodeUid);

		lFontUnit.drawText("current segment: " + lDestinationNode.uid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
		lFontUnit.drawText("segment length: " + lLeadAxle.currentEdge.edgeLengthInMeters + "m", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
		lFontUnit.drawText("acceleration: " + String.format("%.2f", pTrain.acceleration() * lDelta) + "m/s^2", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
		lFontUnit.drawText("target speed: " + String.format("%.2f", pTrain.targetSpeedInMetersPerSecond) + "m/s", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
		lFontUnit.drawText("actual speed: " + String.format("%.2f", pTrain.getSpeed()) + "m/s", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
		lFontUnit.drawText("max speed: " + pTrain.leadCar.topSpeed() + "km/h", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
		lFontUnit.drawText("drive forward " + pTrain.drivingForward(), lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);

		lFontUnit.end();
	}

	private void debugDrawTrainInformation(LintfordCore pCore, Train pTrain) {
		if (!ConstantsGame.DEBUG_DRAW_TRAIN_DEBUG_INFO)
			return;

		if (pTrain == null)
			return;

		final var lFontUnit = mRendererManager.uiTextFont();
		final var lHudRect = pCore.HUD().boundingRectangle();

		float lTextPositionX = lHudRect.left() + 5.f;
		float lTextPositionY = lHudRect.top() + 50.f;

		lFontUnit.begin(pCore.HUD());

		final int lNumTrainCars = ConstantsGame.DEBUG_DRAW_TRAIN_CARS_DEBUG_INFO ? pTrain.getNumberOfCarsInTrain() : 1;
		for (int i = 0; i < lNumTrainCars; i++) {
			lTextPositionY = lHudRect.top() + 50.f;

			final var lTrainCar = pTrain.getCarByIndex(i);

			final var lFrontAxle = lTrainCar.frontAxle;
			final var lRearAxle = lTrainCar.rearAxle;

			lFontUnit.drawText("Train ", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("  Speed: " + pTrain.getSpeed(), lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("  Forwards: " + pTrain.drivingForward(), lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lTextPositionY += 20.f;
			lFontUnit.drawText("Train Frontaxel", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("      Current Edge Uid: " + lFrontAxle.currentEdge.uid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("  Destination Node Uid: " + lFrontAxle.destinationNodeUid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			if (lFrontAxle.nextFollowEdge.edge != null) {
				lFontUnit.drawText("         Next Edge Uid: " + lFrontAxle.nextFollowEdge.edge.uid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			} else
				lTextPositionY += 20.f;
			lFontUnit.drawText("    Distance into Edge: " + lFrontAxle.normalizedDistanceAlongEdge, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("      Next Follow Edge: ", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			if (lRearAxle.nextFollowEdge != null) {
				lFontUnit.drawText("       Target Node Uid: " + lFrontAxle.nextFollowEdge.targetNodeUid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
				lFontUnit.drawText("           Logicounter: " + lFrontAxle.nextFollowEdge.logicalCounter, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);

			}
			lTextPositionY += 20.f;
			lTextPositionY += 20.f;
			lTextPositionY += 20.f;
			lFontUnit.drawText("Train Rearaxel", lTextPositionX, lTextPositionY += 20.f, -.01f, 1.f);
			lFontUnit.drawText("      Current Edge Uid: " + lRearAxle.currentEdge.uid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("  Destination Node Uid: " + lRearAxle.destinationNodeUid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			if (lRearAxle.nextFollowEdge.edge != null) {
				lFontUnit.drawText("         Next Edge Uid: " + lRearAxle.nextFollowEdge.edge.uid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			} else
				lTextPositionY += 20.f;
			lFontUnit.drawText("    Distance into Edge: " + lRearAxle.normalizedDistanceAlongEdge, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			lFontUnit.drawText("      Next Follow Edge: ", lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
			if (lRearAxle.nextFollowEdge != null) {
				lFontUnit.drawText("       Target Node Uid: " + lRearAxle.nextFollowEdge.targetNodeUid, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);
				lFontUnit.drawText("           Logicounter: " + lRearAxle.nextFollowEdge.logicalCounter, lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);

			}
			lTextPositionY += 20.f;
			float lAxleSpacing = Math.abs(lFrontAxle.normalizedDistanceAlongEdge) - Math.abs(lRearAxle.normalizedDistanceAlongEdge);
			lFontUnit.drawText("axle spacing: " + (String.format("%.2f", lAxleSpacing)), lTextPositionX, lTextPositionY += 20.f, -0.1f, 1.f);

			lTextPositionX += 275.f;

		}
		lTextPositionX = lHudRect.left() + 5.f;

		// Render the axle ticker information
		lTextPositionY += 20.f;
		lTextPositionY += 20.f;
		final int lFollowNodeListSize = pTrain.trackEdgeFollowList.size();
		for (int j = 0; j < lFollowNodeListSize; j++) {
			final var lTicker = pTrain.trackEdgeFollowList.get(j);
			if (lTicker.edge != null)
				lFontUnit.drawText("e" + lTicker.edge.uid + "  (" + lTicker.logicalCounter + ")", lTextPositionX, lTextPositionY += 20.f, -.01f, 1.f);

		}
		lFontUnit.end();
	}

}
