package lintfordpickle.mailtrain.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import lintfordpickle.mailtrain.data.track.TrackSegment.SegmentSignals;
import lintfordpickle.mailtrain.data.trains.Train;
import lintfordpickle.mailtrain.data.trains.TrainCar;
import lintfordpickle.mailtrain.data.trains.TrainHitch;
import lintfordpickle.mailtrain.data.trains.TrainManager;
import lintfordpickle.mailtrain.data.trains.definitions.TrainCarDefinition;
import lintfordpickle.mailtrain.data.world.scenes.GameScene;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.ResourceController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.Vector2f;

public class TrainController extends BaseController implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Train Controller";

	// pixels
	private static final float TRAIN_DISTANCE_BETWEEN_FRONT_AND_AXLE = 8.0f;
	private static final float TRAIN_DISTANCE_BETWEEN_REAR_AND_AXLE = 8.0f;
	private static final float TRAIN_DISTANCE_BETWEEN_AXLES = 48.0f;
	private static final float DISTANCE_BETWEENCONNECTED_TRAINS = 12.0f;

	// TODO: Do this properly (housekeeping)
	private static int mTrainCarPoolUidCounter = 0;
	private int mTrainCounter = 0;

	public int getNewTrainNumber() {
		return mTrainCounter++;
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private AudioFireAndForgetManager mTrainSoundManager;

	private TrackController mTrackController;
	private TrainManager mTrainManager;
	private Train mMainTrain;

	private final List<Train> mUpdateTrainList = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mTrainManager != null;
	}

	public TrainManager trainManager() {
		return mTrainManager;
	}

	public Train mainTrain() {
		return mMainTrain;
	}

	public int getNumActiveTrains() {
		return mTrainManager.activeTrains().size();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainController(ControllerManager pControllerManager, GameScene pGameWorld, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mTrainManager = pGameWorld.trainManager();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mTrackController = (TrackController) lControllerManager.getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupUid());

		final var lResourceController = (ResourceController) lControllerManager.getControllerByNameRequired(ResourceController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		final var lResourceManager = lResourceController.resourceManager();

		lResourceManager.audioManager().loadAudioFile("SOUND_HORN", "res/sounds/soundTrainHorn.wav", false);
		lResourceManager.audioManager().loadAudioFile("SOUND_CRASH", "res/sounds/soundCrash.wav", false);

		mTrainSoundManager = new AudioFireAndForgetManager(lResourceManager.audioManager());
		mTrainSoundManager.acquireAudioSources(4);
	}

	@Override
	public void unloadController() {
		super.unloadController();

		mTrainSoundManager.unassign();
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		// FIXME: Debug code
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_9, this)) {
			mainTrain().killSpeed();

		}
		// FIXME: Debug code
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_7, this)) {
			// 1. get the distance from the current main train to the start of the next signals
			final var lDestNodeUid = mainTrain().leadCar.frontAxle.destinationNodeUid;
			final var lCurrentSegment = mainTrain().leadCar.frontAxle.currentEdge;
			final var lSignalSegments = lCurrentSegment.getSignalsList(lDestNodeUid);

			final var lDistanceToNextSignalBlock = getDistanceToNextSignalBlock(lSignalSegments, mainTrain());

			mainTrain().brakeAtPosition(lDistanceToNextSignalBlock);

		}
		// FIXME: Move this to the Gui
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_1, this)) {
			unhitchTrainCar(mainTrain(), 1);

		} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_2, this)) {
			unhitchTrainCar(mainTrain(), 2);

		} else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_3, this)) {
			unhitchTrainCar(mainTrain(), 3);

		}

		else if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_H, this)) {
			hitchLastCar(mainTrain());

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

	public Train addNewMainTrain() {
		final var lPlayerSpawnEdge = mTrackController.track().getEdgeByUid(0);

		mMainTrain = addNewTrain(lPlayerSpawnEdge, 3);
		return mMainTrain;
	}

	public Train addNewTrain(TrackSegment pSpawnEdge) {
		return addNewTrain(pSpawnEdge, 0);
	}

	public Train addNewTrain(TrackSegment pSpawnEdge, int pNumCarriages) {
		if (ConstantsGame.DEBUG_FORCE_NO_CARRIAGES)
			pNumCarriages = 0;

		final int lNewTrainNumber = getNewTrainNumber();

		// Create a new train
		final var lNewTrain = mTrainManager.getFreePooledItem();
		lNewTrain.init(lNewTrainNumber);

		lNewTrain.setSpeed(10000.f);

		// Add a locomotive engine to the front of the train
		final var lLocomotiveCar = createNewTrainCar(lNewTrain, null, TrainCarDefinition.Locomotive00Definition);
		var lFollowingCar = lLocomotiveCar;
		for (int i = 0; i < pNumCarriages; i++) {
			lFollowingCar = createNewTrainCar(lNewTrain, lFollowingCar, TrainCarDefinition.EmptyCarriage00Definition);

		}
		if (pSpawnEdge == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "The track doesn't contain a spawn point!");
			pSpawnEdge = mTrackController.track().edges().get(0);

		}
		lNewTrain.drivingForward(true);
		placeTrainOnTracks(lNewTrain, pSpawnEdge, pSpawnEdge.nodeAUid, 1.0f);

		lNewTrain.addFollowEdge(pSpawnEdge, pSpawnEdge.nodeAUid);

		mTrainManager.activeTrains().add(lNewTrain);
		if (ConstantsGame.SOUNDS_ENABLED) {
			// mTrainSoundManager.play("SOUND_HORN", lNewTrain.worldPositionX(), lNewTrain.worldPositionY(), 0.f, 0.f);

		}
		return lNewTrain;
	}

	private TrainCar createNewTrainCar(Train pParentTrain, TrainCar pTrainCarInFront, TrainCarDefinition pDefinition) {
		final var lNewTrainCar = new TrainCar(mTrainCarPoolUidCounter++);

		lNewTrainCar.train = pParentTrain;

		lNewTrainCar.init(pDefinition);
		pParentTrain.addTrainCarsToBackOfTrain(lNewTrainCar.frontHitch);

		return lNewTrainCar;
	}

	private void placeTrainOnTracks(Train pTrain, TrackSegment pSpawnEdge, int pDestinationNodeUid, float pDistanceAlongEdge) {
		final float lEdgeLength = pSpawnEdge.edgeLengthInMeters;
		final float lEdgeUnit = (1.f / lEdgeLength);

		// Axle length starts at front of edge
		float lAxleLocation = pDistanceAlongEdge;

		final int lNumCarsInTrain = pTrain.getNumberOfCarsInTrain();
		for (int i = 0; i < lNumCarsInTrain; i++) {
			final var lTrainCar = pTrain.getCarByIndex(i);

			// TODO: Issue when spawn platform is not long enough for the train (rear carriages have incorrect node info).

			lAxleLocation -= lEdgeUnit * TRAIN_DISTANCE_BETWEEN_FRONT_AND_AXLE;

			lTrainCar.frontAxle.currentEdge = pSpawnEdge;
			lTrainCar.frontAxle.destinationNodeUid = pSpawnEdge.nodeAUid;
			lTrainCar.frontAxle.normalizedDistanceAlongEdge = lAxleLocation;

			// Fill the next follow edge with the information about the edge we start on (in spawn case)
			lTrainCar.frontAxle.nextFollowEdge.edge = pSpawnEdge;
			lTrainCar.frontAxle.nextFollowEdge.targetNodeUid = pDestinationNodeUid;

			lAxleLocation -= lEdgeUnit * TRAIN_DISTANCE_BETWEEN_AXLES;

			lTrainCar.rearAxle.currentEdge = pSpawnEdge;
			lTrainCar.rearAxle.destinationNodeUid = pSpawnEdge.nodeAUid;
			lTrainCar.rearAxle.normalizedDistanceAlongEdge = lAxleLocation;

			// Fill the next follow edge with the information about the edge we start on (in spawn case)
			lTrainCar.rearAxle.nextFollowEdge.edge = pSpawnEdge;
			lTrainCar.rearAxle.nextFollowEdge.targetNodeUid = pDestinationNodeUid;

			lAxleLocation -= lEdgeUnit * TRAIN_DISTANCE_BETWEEN_REAR_AND_AXLE;
			lAxleLocation -= lEdgeUnit * DISTANCE_BETWEENCONNECTED_TRAINS;

			pTrain.updateAxleWorldPosition(mTrackController.track(), lTrainCar.frontAxle);
			pTrain.updateAxleWorldPosition(mTrackController.track(), lTrainCar.rearAxle);
		}
	}

	public void removeTrain(Train pTrain) {
		if (pTrain == null)
			return;

		final var lTrainsList = mTrainManager.activeTrains();
		if (lTrainsList.contains(pTrain)) {
			lTrainsList.remove(pTrain);

		}
		pTrain.cleanUp();

		mTrainManager.returnPooledItem(pTrain);
	}

	public void unhitchTrainCar(Train pOrigTrain, int pTrainCarNumber) {
		if (pTrainCarNumber <= 0)
			return; // cannot unhitch locomotive
		if (pOrigTrain.getNumberOfCarsInTrain() <= pTrainCarNumber)
			return;

		// Get new train instance to hang the train cars onto
		Train lNewTrain = mTrainManager.getFreePooledItem();
		lNewTrain.init(getNewTrainNumber());

		TrainCar lCurTrainCar = null;
		for (int i = pTrainCarNumber; i < pOrigTrain.getNumberOfCarsInTrain(); i++) {
			lCurTrainCar = pOrigTrain.detachTrainCar(i);
			if (lCurTrainCar == null) {
				System.out.println("Couldn't detach TrainCar " + pTrainCarNumber + " from Train " + pOrigTrain.poolUid);

				return;

			}
			lNewTrain.addTrainCarsToBackOfTrain(lCurTrainCar.getFreeHitch());

		}
		// slow the new train down slightly (as this will be without a locomotive)
		lNewTrain.setSpeed(pOrigTrain.getSpeed() * 0.98f);
		lNewTrain.targetSpeedInMetersPerSecond = 0.f;

		lNewTrain.drivingForward(pOrigTrain.drivingForward());
		lNewTrain.reorientateTrainCarsToLocomotive(mTrackController.track());

		if (lNewTrain.getNumberOfCarsInTrain() > 0)
			mTrainManager.activeTrains().add(lNewTrain);
	}

	// Hitch a TrainCar to the last car
	private void hitchLastCar(Train pTrain) {
		// We can only hitch from the last car in our train

		final var lTrainCar = pTrain.lastCar;
		final var lRearAxle = pTrain.getNumberOfCarsInTrain() == 1 ? lTrainCar.rearAxle : lTrainCar.getAxleOnFreeHitch();

		final float lWorldPositionOfAxleX = lRearAxle.worldPositionX;
		final float lWorldPositionOfAxleY = lRearAxle.worldPositionY;

		// Problem here - wrong hitch
		final var lHitchUpto = getTrainCarInHitchVacinity(pTrain.lastCar, lWorldPositionOfAxleX, lWorldPositionOfAxleY);

		if (lHitchUpto == null)
			return;

		// TODO: Check the hitch status (opened, damaged etc.)

		pTrain.mergeTrains(mTrackController.track(), lHitchUpto.parentCar.train, lHitchUpto);
		pTrain.reorientateTrainCarsToLocomotive(mTrackController.track());
	}

	public TrainHitch getTrainCarInHitchVacinity(TrainCar pExcludeTrainCar, float pWorldX, float pWorldY) {
		final var lActiveTrains = mTrainManager.activeTrains();
		final int lNumOfActiveTrains = lActiveTrains.size();
		for (int i = 0; i < lNumOfActiveTrains; i++) {
			final var lTrain = lActiveTrains.get(i);
			if (lTrain.leadCar != null && lTrain.leadCar != pExcludeTrainCar) {
				final var lFreeHitch = lTrain.getNumberOfCarsInTrain() == 1 ? lTrain.leadCar.frontHitch : lTrain.leadCar.getFreeHitch();
				final var lMatchingFreeAxle = lTrain.leadCar.getMatchingAxle(lFreeHitch);

				final var lFrontAxleWorldX = lMatchingFreeAxle.worldPositionX;
				final var lFrontAxleWorldY = lMatchingFreeAxle.worldPositionY;

				final var lDist = Vector2f.dst(lFrontAxleWorldX, lFrontAxleWorldY, pWorldX, pWorldY);
				if (lDist < 32.0f) {
					return lFreeHitch;

				}
			}
			if (lTrain.lastCar != null && lTrain.lastCar != pExcludeTrainCar) {
				final var lFreeHitch = lTrain.getNumberOfCarsInTrain() == 1 ? lTrain.lastCar.rearHitch : lTrain.lastCar.getFreeHitch();
				final var lMatchingFreeAxle = lTrain.lastCar.getMatchingAxle(lFreeHitch);

				final var lRearAxleWorldX = lMatchingFreeAxle.worldPositionX;
				final var lRearAxleWorldY = lMatchingFreeAxle.worldPositionY;

				final var lDist = Vector2f.dst(lRearAxleWorldX, lRearAxleWorldY, pWorldX, pWorldY);
				if (lDist < 32.0f) {
					return lFreeHitch;

				}
			}
		}
		return null;
	}

	public float getDistanceToNextSignalBlock(SegmentSignals pCurrentSignalBlock, Train pTrain) {
		final var lTrainCar = pTrain.drivingForward() ? pTrain.leadCar : pTrain.lastCar;
		final var lLeadAxle = pTrain.drivingForward() ? lTrainCar.frontAxle : lTrainCar.getAxleOnFreeHitch();

		final var lDestNodeUid = lLeadAxle.destinationNodeUid;
		final var lDistance = lLeadAxle.normalizedDistanceAlongEdge;
		final var lCurrentSegment = lLeadAxle.currentEdge;

		return mTrackController.getDistanceToNextSignalBlock(lCurrentSegment, lDistance, lDestNodeUid);
	}

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
