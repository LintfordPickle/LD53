package lintfordpickle.mailtrain.controllers.tracks;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import lintfordpickle.mailtrain.controllers.trains.TrainController;
import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import lintfordpickle.mailtrain.data.track.TrackSegment.SegmentSignals;
import lintfordpickle.mailtrain.data.track.signals.TrackSignalBlock;
import lintfordpickle.mailtrain.data.track.signals.TrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.trains.TrainAxle;
import lintfordpickle.mailtrain.data.world.scenes.GameScene;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.Vector2f;

public class TrackController extends BaseController implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Track Controller";

	private static final List<Integer> TempList = new ArrayList<>();

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrainController mTrainController;
	private GameScene mActiveGameScene;

	private int mTrackBuildLogicalCounter = 0;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int trackBuildLogicalCounter() {
		return mActiveGameScene.track().trackLogicalCounter();
	}

	public void updateTrackBuildLogicalCounter() {
		mTrackBuildLogicalCounter++;
		mActiveGameScene.track().trackLogicalCounter(mTrackBuildLogicalCounter);

		Debug.debugManager().logger().i(getClass().getSimpleName(), "Track logical build counter (" + mTrackBuildLogicalCounter + ")");
	}

	@Override
	public boolean isInitialized() {
		return mActiveGameScene != null;
	}

	public Track track() {
		return mActiveGameScene.track();
	}

	public float worldToGrid(final float pWorldCoord) {
		return Track.worldToGrid(pWorldCoord, track().gridSizeInPixels);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackController(ControllerManager pControllerManager, GameScene pGameWorld, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mActiveGameScene = pGameWorld;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {

		mTrainController = (TrainController) pCore.controllerManager().getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupUid());

		mActiveGameScene.track().areSignalsDirty = true;
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);
		if (track().areSignalsDirty) {
			rebuildTrackSignalBlocks(pCore, track());
		} else {
			updateSignals(pCore, track());
		}
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		// TODO: Restore this later to save from within game (needs the world + scene)

//		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_F4, this)) {
//			final var lTrackFilename = mGameWorld.gameWorldHeader().trackFilename();
//			Debug.debugManager().logger().i(getClass().getSimpleName(), "Saving track to " + lTrackFilename);
//			saveTrack(lTrackFilename);
//			return true;
//		}

		return super.handleInput(pCore);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void loadTrackFromFile(String fileName) {
		final var lLoadedTrack = TrackIOController.loadTrackFromFile(fileName);
		mActiveGameScene.track(lLoadedTrack);
	}

	public void saveTrack(String pFilename) {
		FileWriter lWriter = null;
		Gson gson = new Gson();
		try {
			lWriter = new FileWriter(pFilename);
			gson.toJson(mActiveGameScene.track(), lWriter);

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lWriter.flush();
				lWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ---------------------------------------------

	public void clearAllTracks() {
		// TODO:
	}

	private void updateSignals(LintfordCore pCore, Track pTrack) {
		// open all signals
		pTrack.trackSignalBlocks.openSignalSegmentStates();

		// iterate over the trains and set the newly occupied blocks
		final var lTrainManager = mTrainController.trainManager();
		final int lNumTrains = lTrainManager.numInstances();
		for (int i = 0; i < lNumTrains; i++) {
			final var lTrain = lTrainManager.activeTrains().get(i);
			for (int j = 0; j < lTrain.getNumberOfCarsInTrain(); j++) {
				final var lCar = lTrain.getCarByIndex(j);

				updateSignalSegment(pCore, pTrack, lCar.frontAxle);
				updateSignalSegment(pCore, pTrack, lCar.rearAxle);

			}
		}
		// iterate over the signal blocks and update the warning and danger segments
		final int lNumSegments = pTrack.getNumberTrackEdges();
		for (int i = 0; i < lNumSegments; i++) {
			// iterate the whole track and update the signal blocks
			// TODO:

		}
	}

	private void updateSignalSegment(LintfordCore pCore, Track pTrack, TrainAxle pAxle) {
		final var lAxleTrackSegment = pAxle.currentEdge;
		final var lDestinationNodeUid = pAxle.destinationNodeUid;
		final var lDistanceIntoSegment = pAxle.normalizedDistanceAlongEdge;
		if (lAxleTrackSegment != null) {
			lAxleTrackSegment.setBothSignalStates(lDestinationNodeUid, lDistanceIntoSegment, SignalState.Occupied);

		}
	}

	// ---------------------------------------------

	public float getDistanceToNextSignalBlock(TrackSegment pTrackSegment, float pDist, int pDestNodeUid) {
		final var lTrack = track();

		float lDistanceAccumalated = 0.f;

		var lDestinationNodeUid = pDestNodeUid;
		var lCurrentTrackSegment = pTrackSegment;
		var lSignalSegments = lCurrentTrackSegment.getSignalsList(pDestNodeUid);
		var lOurSignalSegment = lSignalSegments.getSignal(pDist);
		final var lOurSignalBlock = lOurSignalSegment.signalBlock;
		if (lOurSignalBlock == null) {
			return 0.f;
		}
		final var lOrignalSignalBlockUid = lOurSignalBlock.poolUid;

		final float lOffsetDistanceIntoFirstSegment = (pDist - lOurSignalSegment.startDistance) / (1.f / lCurrentTrackSegment.edgeLengthInMeters);

		// Iterate the current path, until we reach a track segment with a different signal block on it
		while (lOurSignalSegment != null) {
			lDistanceAccumalated += lOurSignalSegment.length / (1.f / lCurrentTrackSegment.edgeLengthInMeters);

			// get next signal
			lOurSignalSegment = lSignalSegments.getNextSignal(lOurSignalSegment);
			if (lOurSignalSegment == null) {
				// Get the first signal from the next track segment
				lCurrentTrackSegment = lTrack.getNextEdge(lCurrentTrackSegment, lDestinationNodeUid);
				if (lCurrentTrackSegment != null) {
					lDestinationNodeUid = lCurrentTrackSegment.getOtherNodeUid(lDestinationNodeUid);
					lSignalSegments = lCurrentTrackSegment.getSignalsList(lDestinationNodeUid);
					lOurSignalSegment = lSignalSegments.getSignal(0.f);

				} else {
					lOurSignalSegment = null; // end
				}
			}
			// signal null, get first signal next track segment
			if (lOurSignalSegment != null && lOurSignalSegment.signalBlock.poolUid != lOrignalSignalBlockUid) {
				return lDistanceAccumalated - lOffsetDistanceIntoFirstSegment;

			}
		}
		return lDistanceAccumalated - lOffsetDistanceIntoFirstSegment;
	}

	public TrackSignalBlock getNextSignalBlock(TrackSegment pTrackSegment, float pDist, int pDestNodeUid) {
		final var lTrack = track();

		var lDestinationNodeUid = pDestNodeUid;
		var lCurrentTrackSegment = pTrackSegment;
		var lSignalSegments = lCurrentTrackSegment.getSignalsList(pDestNodeUid);
		var lOurSignalSegment = lSignalSegments.getSignal(pDist);
		final var lOurSignalBlock = lOurSignalSegment.signalBlock;
		if (lOurSignalBlock == null) {
			return null;

		}
		final var lOrignalSignalBlockUid = lOurSignalBlock.poolUid;

		TempList.clear();
		TempList.add(lOurSignalSegment.poolUid);

		// Iterate the current path, until we reach a track segment with a different signal block on it
		while (lOurSignalSegment != null) {
			// get next signal
			lOurSignalSegment = lSignalSegments.getNextSignal(lOurSignalSegment);
			if (lOurSignalSegment == null) {
				// Get the first signal from the next track segment
				lCurrentTrackSegment = lTrack.getNextEdge(lCurrentTrackSegment, lDestinationNodeUid);
				if (lCurrentTrackSegment != null) {
					lDestinationNodeUid = lCurrentTrackSegment.getOtherNodeUid(lDestinationNodeUid);
					lSignalSegments = lCurrentTrackSegment.getSignalsList(lDestinationNodeUid);
					lOurSignalSegment = lSignalSegments.getSignal(0.f);

				} else {
					lOurSignalSegment = null; // end
				}
			}
			if (lOurSignalSegment != null && lOurSignalSegment.signalBlock.poolUid != lOrignalSignalBlockUid) {
				return lOurSignalSegment.signalBlock;

			}
			if (TempList.contains(lOurSignalSegment.poolUid))
				return null;

		}
		return null;
	}

	// ---------------------------------------------

	public static float getEdgeLength(Track pTrack, TrackSegment pEdge) {
		final var lNodeA = pTrack.getNodeByUid(pEdge.nodeAUid);
		final var lNodeB = pTrack.getNodeByUid(pEdge.nodeBUid);

		// TODO: Edge length needs to consider curves
		return Vector2f.dst(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y);
	}

	public void rebuildTrackSignalBlocks(LintfordCore pCore, Track pTrack) {
		mTrackBuildLogicalCounter++;

		// unassign all blocks
		pTrack.trackSignalBlocks.resetSignalSegments();

		// Rebuild the track signal blocks.
		final var lTrackSegments = pTrack.edges();
		final int lNumTrackSegments = lTrackSegments.size();
		for (int i = 0; i < lNumTrackSegments; i++) {
			final var lTrackSegment = lTrackSegments.get(i);

			if (lTrackSegment.logicalUpdateCounter >= mTrackBuildLogicalCounter)
				continue;

			// We visit each TrackSegment only once
			lTrackSegment.logicalUpdateCounter = mTrackBuildLogicalCounter;

			// Update TrackSegment A Signals
			if (lTrackSegment.signalsA.logigalUpdateCounter < mTrackBuildLogicalCounter) {
				lTrackSegment.signalsA.logigalUpdateCounter = mTrackBuildLogicalCounter;

				updateBuildSignalBlock(pCore, pTrack, lTrackSegment, lTrackSegment.signalsA, mTrackBuildLogicalCounter);

			}
			// Update TrackSegmentB Signals
			if (lTrackSegment.signalsB.logigalUpdateCounter < mTrackBuildLogicalCounter) {
				lTrackSegment.signalsB.logigalUpdateCounter = mTrackBuildLogicalCounter;

				updateBuildSignalBlock(pCore, pTrack, lTrackSegment, lTrackSegment.signalsB, mTrackBuildLogicalCounter);

			}
		}
		pTrack.areSignalsDirty = false;

		final var lBlockInstances = pTrack.trackSignalBlocks.instances();
		final int lNumBlocks = lBlockInstances.size();
		for (int i = 0; i < lNumBlocks; i++) {
			final var lSignalBlock = lBlockInstances.get(i);

			System.out.println("Track Signal Block: " + lSignalBlock.poolUid);

			final var lSignalSegments = lSignalBlock.signalSegments();

			if (lSignalSegments == null)
				return;
			final int lNumSignalSegments = lSignalSegments.size();
			for (int j = 0; j < lNumSignalSegments; j++) {
				final var lSignal = lSignalSegments.get(j);
				System.out.println("   signalsegment: " + lSignal.poolUid + "(" + lSignal.trackSegmentUid + ")");

			}
		}
	}

	private void updateBuildSignalBlock(LintfordCore pCore, Track pTrack, TrackSegment pTrackSegment, SegmentSignals pSegmentSignals, int pLUCounter) {
		// -------- Get the ball rolling

		var lCurrentSignal = pSegmentSignals.getSignal(0.0f);
		var lCurrentSignalBlock = pTrack.trackSignalBlocks.getFreePooledItem();

		lCurrentSignalBlock.signalSegments().add(lCurrentSignal);
		lCurrentSignal.signalBlock = lCurrentSignalBlock;

		// -------- Link to prev segment(s)

		final var lSourceNodeUid = pTrackSegment.getOtherNodeUid(pSegmentSignals.destinationUid);
		final var lSourceNode = pTrack.getNodeByUid(lSourceNodeUid);

		final int lNumSourceSideEdges = lSourceNode.numberConnectedEdges();
		for (int i = 0; i < lNumSourceSideEdges; i++) {
			final var lOtherSourceEdge = lSourceNode.connectedEdges().get(i);
			if (lOtherSourceEdge == pTrackSegment)
				continue;

			// Can we merge with a signal block from a source node?
			final var lSignalsOtherNode = lOtherSourceEdge.getSignalsList(lSourceNodeUid);
			final var lLastSignalOtherNode = lSignalsOtherNode.getSignal(1.f);
			if (lLastSignalOtherNode != null && lLastSignalOtherNode.signalBlock != null) {
				// merge *their* signals into our list. Finish by recycling the signal block.
				final var lOtherSignalBlock = lLastSignalOtherNode.signalBlock;
				if (lOtherSignalBlock != lCurrentSignalBlock) {
					lCurrentSignalBlock.acquireOtherBlock(lOtherSignalBlock);
					lOtherSignalBlock.reset();

					pTrack.trackSignalBlocks.returnPooledItem(lOtherSignalBlock);

				}
			}
		}
		// -------- Iterate this track segment

		final var lNextSignal = pSegmentSignals.getNextSignal(lCurrentSignal);
		if (lNextSignal != null) {
			if (lNextSignal.isSignalHead) {
				lCurrentSignalBlock = pTrack.trackSignalBlocks.getFreePooledItem();

			}
			lCurrentSignalBlock.signalSegments().add(lNextSignal);
			lNextSignal.signalBlock = lCurrentSignalBlock;

		}
		// -------- Link to next segment(s)

		int pDestinationUid = pSegmentSignals.destinationUid;
		final var lDestinationNode = pTrack.getNodeByUid(pDestinationUid);

		final int lNumDestinationSideEdges = lDestinationNode.numberConnectedEdges();
		for (int i = 0; i < lNumDestinationSideEdges; i++) {
			final var lOtherSourceEdge = lDestinationNode.connectedEdges().get(i);
			if (lOtherSourceEdge == pTrackSegment)
				continue;

			// Can we merge with a signal block from a destination node?
			final var lSignalsOtherNode = lOtherSourceEdge.getSignalsList(lOtherSourceEdge.getOtherNodeUid(pDestinationUid));
			final var lLastSignalOtherNode = lSignalsOtherNode.getSignal(0.f);
			if (lLastSignalOtherNode != null && lLastSignalOtherNode.signalBlock != null) {
				// merge *their* signals into our list and recycle them
				final var lOtherSignalBlock = lLastSignalOtherNode.signalBlock;
				if (lOtherSignalBlock != lCurrentSignalBlock) {
					lCurrentSignalBlock.acquireOtherBlock(lOtherSignalBlock);
					lOtherSignalBlock.reset();

					pTrack.trackSignalBlocks.returnPooledItem(lOtherSignalBlock);

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
