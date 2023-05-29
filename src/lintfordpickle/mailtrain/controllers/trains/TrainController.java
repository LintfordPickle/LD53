package lintfordpickle.mailtrain.controllers.trains;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.TriggerController;
import lintfordpickle.mailtrain.controllers.scanline.ScanlineProjectileController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment.SegmentSignalsCollection;
import lintfordpickle.mailtrain.data.scene.trains.Train;
import lintfordpickle.mailtrain.data.scene.trains.TrainCar;
import lintfordpickle.mailtrain.data.scene.trains.TrainHitch;
import lintfordpickle.mailtrain.data.scene.trains.TrainManager;
import lintfordpickle.mailtrain.data.scene.trains.definitions.TrainCarDefinition;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.ResourceController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.Vector2f;

public class TrainController extends BaseController implements ITrainWhisperer, IInputProcessor {

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

	private ScanlineProjectileController mScanlineProjectileController;
	private TriggerController mTriggerController;
	private TrackController mTrackController;

	private TrainManager mTrainManager;

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

	public int getNumActiveTrains() {
		return mTrainManager.activeTrains().size();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainController(ControllerManager controllerManager, GameSceneInstance gameScene, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mTrainManager = gameScene.trainManager();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mTriggerController = (TriggerController) lControllerManager.getControllerByNameRequired(TriggerController.CONTROLLER_NAME, entityGroupUid());
		mTrackController = (TrackController) lControllerManager.getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupUid());
		mScanlineProjectileController = (ScanlineProjectileController) lControllerManager.getControllerByNameRequired(ScanlineProjectileController.CONTROLLER_NAME, entityGroupUid());

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

			lTrain.update(pCore, mTrackController.track());

			// Check for stops on special zone segments
			if (lTrain.getSpeed() == 0) {
				final var lSegment = lTrain.leadCar.frontAxle.currentSegment;
				if (lSegment != null) {

					// TODO: The triggering basics are there - but more thought needed as to segment types/names/triggers etc.
					if (lSegment.isSegmentOfType(RailTrackSegment.SEGMENT_SPECIAL_TYPE_STATION)) {
						final var lName = lSegment.segmentName;
						if (lName != null) {
							mTriggerController.setTrigger(TriggerController.TRIGGER_TYPE_NEW_SCENE, -1, lName);
						}
					}
				}
			}
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public Train addNewTrain(RailTrackSegment spawnTrackSegment) {
		return addNewTrain(spawnTrackSegment, 0);
	}

	public Train addNewTrain(RailTrackSegment spawnSegment, int numCarriages) {
		if (ConstantsGame.DEBUG_FORCE_NO_CARRIAGES)
			numCarriages = 0;

		final int lNewTrainNumber = getNewTrainNumber();

		// Create a new train
		final var lNewTrain = mTrainManager.getFreePooledItem();
		lNewTrain.init(lNewTrainNumber);

		// Add a locomotive engine to the front of the train
		final var lLocomotiveCar = createNewTrainCar(lNewTrain, null, TrainCarDefinition.Locomotive00Definition);
		var lFollowingCar = lLocomotiveCar;
		for (int i = 0; i < numCarriages; i++) {
			lFollowingCar = createNewTrainCar(lNewTrain, lFollowingCar, TrainCarDefinition.Cannon00Definition);
		}

		if (spawnSegment == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "The track doesn't contain a spawn point!");
			spawnSegment = mTrackController.track().segments().get(0);
		}

		lNewTrain.drivingForward(true);
		placeTrainOnTracks(lNewTrain, spawnSegment, spawnSegment.nodeAUid, 1.0f);

		lNewTrain.addFollowSegment(spawnSegment, spawnSegment.nodeAUid);

		mTrainManager.activeTrains().add(lNewTrain);
		if (ConstantsGame.SOUNDS_ENABLED) {
			// mTrainSoundManager.play("SOUND_HORN", lNewTrain.worldPositionX(), lNewTrain.worldPositionY(), 0.f, 0.f);

		}
		return lNewTrain;
	}

	private TrainCar createNewTrainCar(Train pParentTrain, TrainCar trainCarInFront, TrainCarDefinition trainCarDefinition) {
		final var lNewTrainCar = new TrainCar(mTrainCarPoolUidCounter++);

		lNewTrainCar.train = pParentTrain;

		lNewTrainCar.init(trainCarDefinition);
		lNewTrainCar.trainCallbackListener(this);

		pParentTrain.addTrainCarsToBackOfTrain(lNewTrainCar.frontHitch);

		return lNewTrainCar;
	}

	private void placeTrainOnTracks(Train train, RailTrackSegment spawnSegment, int destinationNodeUid, float distanceAlongSegment) {
		final float lSegmentLength = spawnSegment.segmentLengthInMeters;
		final float lSegmentUnit = (1.f / lSegmentLength);

		// Axle length starts at front of segment
		float lAxleLocation = distanceAlongSegment;

		final int lNumCarsInTrain = train.getNumberOfCarsInTrain();
		for (int i = 0; i < lNumCarsInTrain; i++) {
			final var lTrainCar = train.getCarByIndex(i);

			// TODO: Issue when spawn platform is not long enough for the train (rear carriages have incorrect node info).

			lAxleLocation -= lSegmentUnit * TRAIN_DISTANCE_BETWEEN_FRONT_AND_AXLE;

			lTrainCar.frontAxle.currentSegment = spawnSegment;
			lTrainCar.frontAxle.destinationNodeUid = spawnSegment.nodeAUid;
			lTrainCar.frontAxle.normalizedDistanceAlongSegment = lAxleLocation;

			// Fill the next follow segment with the information about the segment we start on (in spawn case)
			lTrainCar.frontAxle.nextFollowSegment.Segment = spawnSegment;
			lTrainCar.frontAxle.nextFollowSegment.targetNodeUid = destinationNodeUid;

			lAxleLocation -= lSegmentUnit * TRAIN_DISTANCE_BETWEEN_AXLES;

			lTrainCar.rearAxle.currentSegment = spawnSegment;
			lTrainCar.rearAxle.destinationNodeUid = spawnSegment.nodeAUid;
			lTrainCar.rearAxle.normalizedDistanceAlongSegment = lAxleLocation;

			// Fill the next follow segment with the information about the segment we start on (in spawn case)
			lTrainCar.rearAxle.nextFollowSegment.Segment = spawnSegment;
			lTrainCar.rearAxle.nextFollowSegment.targetNodeUid = destinationNodeUid;

			lAxleLocation -= lSegmentUnit * TRAIN_DISTANCE_BETWEEN_REAR_AND_AXLE;
			lAxleLocation -= lSegmentUnit * DISTANCE_BETWEENCONNECTED_TRAINS;

			train.updateAxleWorldPosition(mTrackController.track(), lTrainCar.frontAxle);
			train.updateAxleWorldPosition(mTrackController.track(), lTrainCar.rearAxle);
		}
	}

	public void removeTrain(Train pTrain) {
		if (pTrain == null)
			return;

		// TOOD: Missing removing the TrainCars ??

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
				System.out.println("Couldn't detach TrainCar " + pTrainCarNumber + " from Train " + pOrigTrain.uid);

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
	public void hitchLastCar(Train pTrain) {
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

	public float getDistanceToNextSignalBlock(SegmentSignalsCollection pCurrentSignalBlock, Train pTrain) {
		final var lTrainCar = pTrain.drivingForward() ? pTrain.leadCar : pTrain.lastCar;
		final var lLeadAxle = pTrain.drivingForward() ? lTrainCar.frontAxle : lTrainCar.getAxleOnFreeHitch();

		final var lDestNodeUid = lLeadAxle.destinationNodeUid;
		final var lDistance = lLeadAxle.normalizedDistanceAlongSegment;
		final var lCurrentSegment = lLeadAxle.currentSegment;

		return mTrackController.getDistanceToNextSignalBlock(lCurrentSegment, lDistance, lDestNodeUid);
	}

	// ---------------------------------------------
	// IInputProcessor-Methods
	// ---------------------------------------------

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

	// ---------------------------------------------
	// Train Callback-Methods
	// ---------------------------------------------

	@Override
	public void shootScanlineProjectile(int ownerId, float sx, float sy, float angle, float dist) {
		mScanlineProjectileController.shootScanline(ownerId, sx, sy, angle, dist);
	}

}
