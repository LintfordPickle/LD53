package lintfordpickle.mailtrain.data.scene.track;

import lintfordpickle.mailtrain.data.scene.BaseInstanceManager;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.savedefinitions.GameSceneSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalSegment;

public class TrackManager extends BaseInstanceManager {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private RailTrackInstance mTrack;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public RailTrackInstance track() {
		return mTrack;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackManager() {
		mTrack = new RailTrackInstance();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initializeManager() {
		// TODO Auto-generated method stub

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	public void storeInSceneDefinition(GameSceneSaveDefinition sceneDefinition) {
		sceneDefinition.trackSaveManager().storeTrackDefinitions(mTrack);
	}

	@Override
	public void loadFromSceneDefinition(GameSceneSaveDefinition sceneDefinition) {
		final var lTrackSaveManager = sceneDefinition.trackSaveManager();
		final var lRailTrackSaveDefinition = lTrackSaveManager.railTrackSaveDefinition();

		final var lSignalBlocksToLoad = lRailTrackSaveDefinition.railSignalBlocks;
		final var lNumSignalBlocks = lSignalBlocksToLoad.size();
		for (int i = 0; i < lNumSignalBlocks; i++) {
			final var lSignalBlockToLoad = lSignalBlocksToLoad.get(i);
			final var lNewSignalBlock = new RailTrackSignalBlock(lSignalBlockToLoad);
			mTrack.trackSignalBlocks.addSignalBlock(lNewSignalBlock);
		}

		final var lSignalSegmentsToLoad = lRailTrackSaveDefinition.railSignalSegments;
		final var lNumSignalSegments = lSignalSegmentsToLoad.size();
		for (int i = 0; i < lNumSignalSegments; i++) {
			final var lSignalSegmentToLoad = lSignalSegmentsToLoad.get(i);
			final var lNewSignalSegment = new RailTrackSignalSegment(lSignalSegmentToLoad);
			mTrack.trackSignalSegments.addSignalSegment(lNewSignalSegment);
		}

		final var lSegmentsToLoad = lRailTrackSaveDefinition.railTrackSegments;
		final var lNumSegmentsToLoad = lSegmentsToLoad.size();
		for (int i = 0; i < lNumSegmentsToLoad; i++) {
			final var lSegmentToLoad = lSegmentsToLoad.get(i);
			final var lNewSegment = new RailTrackSegment(lSegmentToLoad);
			mTrack.segments().add(lNewSegment);
		}

		final var lNodesToLoad = lRailTrackSaveDefinition.railTrackNodes;
		final var lNumNodesToLoad = lNodesToLoad.size();
		for (int i = 0; i < lNumNodesToLoad; i++) {
			final var lNodeToLoad = lNodesToLoad.get(i);
			final var lNewNode = new RailTrackNode(lNodeToLoad);

			mTrack.nodes().add(lNewNode);
		}
	}

	@Override
	public void finalizeAfterLoading(GameSceneInstance sceneInstance) {
		final var lTrack = sceneInstance.trackManager().track();
		lTrack.finalizeAfterLoading(this);
	}

}
