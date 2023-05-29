package lintfordpickle.mailtrain.controllers.trains;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.trains.Train;
import lintfordpickle.mailtrain.data.scene.trains.TrainManager;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.mouse.IInputProcessor;

public class PlayerTrainController extends BaseController implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Player Train Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrackController mTrackController;
	private TrainController mTrainController;
	private PlayerTrainController mPlayerTrainController;

	private TrainManager mTrainManager;
	private Train mPlayerLocomotiveTrain;

	private final List<Train> mUpdateTrainList = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mTrainManager != null;
	}

	public Train playerLocomotive() {
		return mPlayerLocomotiveTrain;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public PlayerTrainController(ControllerManager controllerManager, GameSceneInstance gameScene, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mTrainManager = gameScene.trainManager();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mTrackController = (TrackController) lControllerManager.getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupUid());
		mTrainController = (TrainController) lControllerManager.getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupUid());
		mPlayerTrainController = (PlayerTrainController) lControllerManager.getControllerByNameRequired(PlayerTrainController.CONTROLLER_NAME, entityGroupUid());

	}

	@Override
	public void unloadController() {
		super.unloadController();
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		// TODO: Player controls on keyboard (W / S / SPACE)
		final var lPlayerLoc = mPlayerTrainController.playerLocomotive();

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) {
			lPlayerLoc.targetSpeedInMetersPerSecond += 4.f;
			if (lPlayerLoc.targetSpeedInMetersPerSecond > lPlayerLoc.leadCar.maxAccel())
				lPlayerLoc.targetSpeedInMetersPerSecond = lPlayerLoc.leadCar.maxAccel();
		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S)) {
			lPlayerLoc.targetSpeedInMetersPerSecond -= 4.f;
			if (lPlayerLoc.targetSpeedInMetersPerSecond < -lPlayerLoc.leadCar.maxAccel())
				lPlayerLoc.targetSpeedInMetersPerSecond = -lPlayerLoc.leadCar.maxAccel();
		}

		// TODO: Brakes don't work
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_SPACE)) {
			lPlayerLoc.targetSpeedInMetersPerSecond = 0;
			lPlayerLoc.fullBrake();
		}

		// DEBUG
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_8)) {
			lPlayerLoc.killSpeed();
		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_7)) {
			// 1. get the distance from the current main train to the start of the next signals
			final var lDestNodeUid = playerLocomotive().leadCar.frontAxle.destinationNodeUid;
			final var lCurrentSegment = playerLocomotive().leadCar.frontAxle.currentSegment;
			final var lSignalSegments = lCurrentSegment.getSignalsList(lDestNodeUid);

			final var lDistanceToNextSignalBlock = mTrainController.getDistanceToNextSignalBlock(lSignalSegments, playerLocomotive());

			playerLocomotive().brakeAtPosition(lDistanceToNextSignalBlock);

		}

		// TODO: Move this to the Gui
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_1, this)) {
			mTrainController.unhitchTrainCar(playerLocomotive(), 1);

		} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_2, this)) {
			mTrainController.unhitchTrainCar(playerLocomotive(), 2);

		} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_3, this)) {
			mTrainController.unhitchTrainCar(playerLocomotive(), 3);
		}

		else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_H, this)) {
			mTrainController.hitchLastCar(playerLocomotive());

		}
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lActiveTrains = mTrainManager.activeTrains();
		final var lActiveTrainCount = lActiveTrains.size();

		mUpdateTrainList.clear();
		for (int i = 0; i < lActiveTrainCount; i++) {
			final var lTrain = lActiveTrains.get(i);

			mUpdateTrainList.add(lTrain);

		}
		for (int i = 0; i < lActiveTrainCount; i++) {
			final var lTrain = mUpdateTrainList.get(i);
			if (lTrain.isDestroyed()) {
				lActiveTrains.remove(lTrain);
				continue;

			}
			// have the train check the next signal and if its clear
			lTrain.update(pCore, mTrackController.track());

			// Check for stops on special zone segments

		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public Train addPlayerTrain(RailTrackSegment startingSegment) {
		var lPlayerSpawnSegment = startingSegment;

		if (startingSegment == null) {
			lPlayerSpawnSegment = mTrackController.track().getSegmentByUid(0);
		}

		mPlayerLocomotiveTrain = mTrainController.addNewTrain(lPlayerSpawnSegment, 1);
		return mPlayerLocomotiveTrain;
	}

	public void TESTaddTrainsToMapSegments() {
		final var lPlayerSpawnSegment = mTrackController.track().getSegmentByUid(5);

		mTrainController.addNewTrain(lPlayerSpawnSegment, 1);
	}

	// ---

	@Override
	public boolean isCoolDownElapsed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetCoolDownTimer() {
		// TODO Auto-generated method stub

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