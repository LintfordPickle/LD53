package lintfordpickle.mailtrain.controllers.tracks;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.controllers.trains.TrainController;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment.SegmentSignalsCollection;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.scene.trains.TrainAxle;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.Vector2f;

public class TrackController extends BaseController implements IInputProcessor {

	// TODO: try and move away from the hard-coded grid stuff
	public final static float GRID_SIZE_DEPRECATED = 32.f;

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Track Controller";

	private static final List<Integer> TempList = new ArrayList<>();

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrainController mTrainController;
	private GameSceneInstance mGameScene;

	private int mTrackBuildLogicalCounter = 0;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int trackBuildLogicalCounter() {
		return mGameScene.trackManager().track().trackLogicalCounter();
	}

	public void updateTrackBuildLogicalCounter() {
		mTrackBuildLogicalCounter++;
		mGameScene.trackManager().track().trackLogicalCounter(mTrackBuildLogicalCounter);

		Debug.debugManager().logger().i(getClass().getSimpleName(), "Track logical build counter (" + mTrackBuildLogicalCounter + ")");
	}

	@Override
	public boolean isInitialized() {
		return mGameScene != null;
	}

	public RailTrackInstance track() {
		return mGameScene.trackManager().track();
	}

	public float worldToGrid(float worldCoord) {
		return RailTrackInstance.worldToGrid(worldCoord, GRID_SIZE_DEPRECATED);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackController(ControllerManager controllerManager, GameSceneInstance gameWorld, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mGameScene = gameWorld;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {

		mTrainController = (TrainController) pCore.controllerManager().getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupUid());

		mGameScene.trackManager().track().areSignalsDirty = true;
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		// rebuildTrackSignalBlocks(core, track());

		if (track().areSignalsDirty) {
			rebuildTrackSignalBlocks(core, track());
		} else {
			updateSignals(core, track());
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	// ---------------------------------------------

	public void clearAllTracks() {
		// TODO:
	}

	private void updateSignals(LintfordCore core, RailTrackInstance trackInstance) {
		// open all signals
		trackInstance.trackSignalBlocks.openSignalSegmentStates();

		// iterate over the trains and set the newly occupied blocks
		final var lTrainManager = mTrainController.trainManager();
		final int lNumTrains = lTrainManager.numInstances();
		for (int i = 0; i < lNumTrains; i++) {
			final var lTrain = lTrainManager.activeTrains().get(i);
			final var lNumTrainCars = lTrain.getNumberOfCarsInTrain();
			for (int j = 0; j < lNumTrainCars; j++) {
				final var lCar = lTrain.getCarByIndex(j);

				updateSignalSegment(core, trackInstance, lCar.frontAxle);
				updateSignalSegment(core, trackInstance, lCar.rearAxle);
			}
		}
	}

	private void updateSignalSegment(LintfordCore core, RailTrackInstance trackInstance, TrainAxle axle) {
		final var lAxleTrackSegment = axle.currentSegment;
		final var lDestinationNodeUid = axle.destinationNodeUid;
		final var lDistanceIntoSegment = axle.normalizedDistanceAlongSegment;

		if (lAxleTrackSegment != null) {
			lAxleTrackSegment.setBothSignalStates(lDestinationNodeUid, lDistanceIntoSegment, SignalState.Occupied);
		}
	}

	// ---------------------------------------------

	public float getDistanceToNextSignalBlock(RailTrackSegment trackSegment, float dist, int destNodeUid) {
		final var lTrack = track();

		float lDistanceAccumalated = 0.f;

		var lDestinationNodeUid = destNodeUid;
		var lCurrentTrackSegment = trackSegment;
		var lSignalSegments = lCurrentTrackSegment.getSignalsList(destNodeUid);
		var lOurSignalSegment = lSignalSegments.getSignal(dist);
		final var lOurSignalBlock = lOurSignalSegment.signalBlock;
		if (lOurSignalBlock == null) {
			return 0.f;
		}
		final var lOrignalSignalBlockUid = lOurSignalBlock.uid;

		final float lOffsetDistanceIntoFirstSegment = (dist - lOurSignalSegment.startDistance()) / (1.f / lCurrentTrackSegment.segmentLengthInMeters);

		// Iterate the current path, until we reach a track segment with a different signal block on it
		while (lOurSignalSegment != null) {
			lDistanceAccumalated += lOurSignalSegment.length() / (1.f / lCurrentTrackSegment.segmentLengthInMeters);

			// get next signal
			lOurSignalSegment = lSignalSegments.getNextSignal(lOurSignalSegment);
			if (lOurSignalSegment == null) {
				// Get the first signal from the next track segment
				lCurrentTrackSegment = lTrack.getNextSegment(lCurrentTrackSegment, lDestinationNodeUid);
				if (lCurrentTrackSegment != null) {
					lDestinationNodeUid = lCurrentTrackSegment.getOtherNodeUid(lDestinationNodeUid);
					lSignalSegments = lCurrentTrackSegment.getSignalsList(lDestinationNodeUid);
					lOurSignalSegment = lSignalSegments.getSignal(0.f);

					// return null to set target speed to 0.0f
					return -1;

				} else {
					lOurSignalSegment = null; // end
				}
			}
			// signal null, get first signal next track segment
			if (lOurSignalSegment != null && lOurSignalSegment.signalBlock.uid != lOrignalSignalBlockUid) {
				return lDistanceAccumalated - lOffsetDistanceIntoFirstSegment;

			}
		}
		return lDistanceAccumalated - lOffsetDistanceIntoFirstSegment;
	}

	public RailTrackSignalBlock getNextSignalBlock(RailTrackSegment trackSegment, float dist, int destNodeUid) {
		final var lTrack = track();

		var lDestinationNodeUid = destNodeUid;
		var lCurrentTrackSegment = trackSegment;
		var lSignalSegments = lCurrentTrackSegment.getSignalsList(destNodeUid);
		var lOurSignalSegment = lSignalSegments.getSignal(dist);
		final var lOurSignalBlock = lOurSignalSegment.signalBlock;
		if (lOurSignalBlock == null) {
			return null;

		}
		final var lOrignalSignalBlockUid = lOurSignalBlock.uid;

		TempList.clear();
		TempList.add(lOurSignalSegment.uid);

		// Iterate the current path, until we reach a track segment with a different signal block on it
		while (lOurSignalSegment != null) {
			// get next signal
			lOurSignalSegment = lSignalSegments.getNextSignal(lOurSignalSegment);
			if (lOurSignalSegment == null) {
				// Get the first signal from the next track segment
				lCurrentTrackSegment = lTrack.getNextSegment(lCurrentTrackSegment, lDestinationNodeUid);
				if (lCurrentTrackSegment != null) {
					lDestinationNodeUid = lCurrentTrackSegment.getOtherNodeUid(lDestinationNodeUid);
					lSignalSegments = lCurrentTrackSegment.getSignalsList(lDestinationNodeUid);
					lOurSignalSegment = lSignalSegments.getSignal(0.f);

				} else {
					lOurSignalSegment = null; // end
				}
			}
			if (lOurSignalSegment != null && lOurSignalSegment.signalBlock.uid != lOrignalSignalBlockUid) {
				return lOurSignalSegment.signalBlock;

			}
			if (TempList.contains(lOurSignalSegment.uid))
				return null;

		}
		return null;
	}

	// ---------------------------------------------

	public static float getSegmentLength(RailTrackInstance trackInstance, RailTrackSegment segment) {
		final var lNodeA = trackInstance.getNodeByUid(segment.nodeAUid);
		final var lNodeB = trackInstance.getNodeByUid(segment.nodeBUid);

		// TODO: Segment length needs to consider curves

		return Vector2f.dst(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y);
	}

	public void rebuildTrackSignalBlocks(LintfordCore core, RailTrackInstance trackInstance) {
		mTrackBuildLogicalCounter++;

		// unassign all blocks
		trackInstance.trackSignalBlocks.resetSignalSegments();
		trackInstance.trackSignalBlocks.clearInstances();

		// Rebuild the track signal blocks.
		final var lTrackSegments = trackInstance.segments();
		final int lNumTrackSegments = lTrackSegments.size();
		for (int i = 0; i < lNumTrackSegments; i++) {
			final var lTrackSegment = lTrackSegments.get(i);

			if (lTrackSegment.logicalUpdateCounter >= mTrackBuildLogicalCounter)
				continue;

			// We visit each TrackSegment only once
			lTrackSegment.logicalUpdateCounter = mTrackBuildLogicalCounter;

			// Update TrackSegment A Signals
			if (lTrackSegment.signalsA.logicalUpdateCounter() < mTrackBuildLogicalCounter) {
				lTrackSegment.signalsA.logicalUpdateCounter(mTrackBuildLogicalCounter);

				updateBuildSignalBlock(core, trackInstance, lTrackSegment, lTrackSegment.signalsA, mTrackBuildLogicalCounter);

			}
			// Update TrackSegmentB Signals
			if (lTrackSegment.signalsB.logicalUpdateCounter() < mTrackBuildLogicalCounter) {
				lTrackSegment.signalsB.logicalUpdateCounter(mTrackBuildLogicalCounter);

				updateBuildSignalBlock(core, trackInstance, lTrackSegment, lTrackSegment.signalsB, mTrackBuildLogicalCounter);

			}
		}
		trackInstance.areSignalsDirty = false;

		final var lBlockInstances = trackInstance.trackSignalBlocks.instances();
		final int lNumBlocks = lBlockInstances.size();
		for (int i = 0; i < lNumBlocks; i++) {
			final var lSignalBlock = lBlockInstances.get(i);
			final var lSignalSegments = lSignalBlock.signalSegments();

			if (lSignalSegments == null)
				return; // TODO : free and return block

			final int lNumSignalSegments = lSignalSegments.size();
			for (int j = 0; j < lNumSignalSegments; j++) {
				final var lSignal = lSignalSegments.get(j);
			}
		}
	}

	private void updateBuildSignalBlock(LintfordCore core, RailTrackInstance trackInstance, RailTrackSegment trackSegment, SegmentSignalsCollection segmentSignals, int pLUCounter) {
		// -------- Get the ball rolling

		var lCurrentSignalSegment = segmentSignals.primarySignalSegment();
		var lCurrentSignalBlock = trackInstance.trackSignalBlocks.getFreePooledItem();
		// TODO: Uid on SignalBlock is massive - there're not being reused

		lCurrentSignalBlock.signalSegments().add(lCurrentSignalSegment);
		lCurrentSignalSegment.signalBlock = lCurrentSignalBlock;

		// -------- Link to prev segment(s)

		final var lSourceNodeUid = trackSegment.getOtherNodeUid(segmentSignals.destinationNodeUid());
		final var lSourceNode = trackInstance.getNodeByUid(lSourceNodeUid);

		final int lNumSourceSideSegments = lSourceNode.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lNumSourceSideSegments; i++) {
			final var lOtherSourceSegment = lSourceNode.trackSwitch.connectedSegments().get(i);
			if (lOtherSourceSegment == trackSegment)
				continue;

			// Can we merge with a signal block from a source node?
			final var lSignalsOtherNode = lOtherSourceSegment.getSignalsList(lSourceNodeUid);
			final var lLastSignalOtherNode = lSignalsOtherNode.getSignal(1.f);
			if (lLastSignalOtherNode != null && lLastSignalOtherNode.signalBlock != null) {
				// merge *their* signals into our list. Finish by recycling the signal block.
				final var lOtherSignalBlock = lLastSignalOtherNode.signalBlock;
				if (lOtherSignalBlock != lCurrentSignalBlock) {
					lCurrentSignalBlock.acquireOtherBlock(lOtherSignalBlock);
					lOtherSignalBlock.reset();

					trackInstance.trackSignalBlocks.returnPooledItem(lOtherSignalBlock);

				}
			}
		}

		// -------- Iterate this track segment

		if (segmentSignals.isAuxiliarySignalSegmentActive()) {
			final var lNextSignal = segmentSignals.auxiliarySignalSegment();
			lCurrentSignalBlock = trackInstance.trackSignalBlocks.getFreePooledItem();

			lCurrentSignalBlock.signalSegments().add(lNextSignal);
			lNextSignal.signalBlock = lCurrentSignalBlock;
		}

		// -------- Link to next segment(s)

		int pDestinationUid = segmentSignals.destinationNodeUid();
		final var lDestinationNode = trackInstance.getNodeByUid(pDestinationUid);

		final int lNumDestinationSideSegments = lDestinationNode.trackSwitch.numberConnectedSegments();
		for (int i = 0; i < lNumDestinationSideSegments; i++) {
			final var lOtherSourceSegment = lDestinationNode.trackSwitch.connectedSegments().get(i);
			if (lOtherSourceSegment == trackSegment)
				continue;

			// Can we merge with a signal block from a destination node?
			final var lSignalsOtherNode = lOtherSourceSegment.getSignalsList(lOtherSourceSegment.getOtherNodeUid(pDestinationUid));
			final var lLastSignalOtherNode = lSignalsOtherNode.getSignal(0.f);
			if (lLastSignalOtherNode != null && lLastSignalOtherNode.signalBlock != null) {
				// merge *their* signals into our list and recycle them
				final var lOtherSignalBlock = lLastSignalOtherNode.signalBlock;
				if (lOtherSignalBlock != lCurrentSignalBlock) {
					lCurrentSignalBlock.acquireOtherBlock(lOtherSignalBlock);
					lOtherSignalBlock.reset();

					trackInstance.trackSignalBlocks.returnPooledItem(lOtherSignalBlock);

				}
			}
		}
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
