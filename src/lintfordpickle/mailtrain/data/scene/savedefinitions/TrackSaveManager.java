package lintfordpickle.mailtrain.data.scene.savedefinitions;

import java.io.Serializable;

import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackNodeSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSegmentSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSignalBlockSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSignalSegmentSaveDefinition;

public class TrackSaveManager implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -8959955380123047555L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private RailTrackSaveDefinition mRailTrackSaveDefinition;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public RailTrackSaveDefinition railTrackSaveDefinition() {
		return mRailTrackSaveDefinition;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void storeTrackDefinitions(RailTrackInstance railTrackInstance) {
		mRailTrackSaveDefinition = new RailTrackSaveDefinition();

		final var lSignalBlocks = railTrackInstance.trackSignalBlocks.instances();
		final var lNumSignalBlocks = lSignalBlocks.size();
		for (int i = 0; i < lNumSignalBlocks; i++) {
			final var lSignalBlock = lSignalBlocks.get(i);
			final var lSignalBlockSaveDefinition = new RailTrackSignalBlockSaveDefinition();

			lSignalBlock.saveIntoDef(lSignalBlockSaveDefinition);
			mRailTrackSaveDefinition.railSignalBlocks.add(lSignalBlockSaveDefinition);
		}

		// Save Signal Segments
		final var lSignalSegments = railTrackInstance.trackSignalSegments.instances();
		final var lNumSignalSegments = lSignalSegments.size();
		for (int i = 0; i < lNumSignalSegments; i++) {
			final var lTrackSignalSegment = lSignalSegments.get(i);
			final var lSignalSegmentSaveDef = new RailTrackSignalSegmentSaveDefinition();

			lTrackSignalSegment.saveIntoDef(lSignalSegmentSaveDef);
			mRailTrackSaveDefinition.railSignalSegments.add(lSignalSegmentSaveDef);
		}

		// Save Track Nodes
		final var lTrackNodes = railTrackInstance.nodes();
		final int lNumNodes = lTrackNodes.size();
		for (int i = 0; i < lNumNodes; i++) {
			final var railTrackNode = lTrackNodes.get(i);
			final var lNewNodeDefinition = new RailTrackNodeSaveDefinition();

			railTrackNode.saveIntoDef(lNewNodeDefinition);
			mRailTrackSaveDefinition.railTrackNodes.add(lNewNodeDefinition);
		}

		// Save Track Segments
		final var lTrackSegments = railTrackInstance.edges();
		final int lNumSegments = lTrackSegments.size();
		for (int i = 0; i < lNumSegments; i++) {
			final var railTrackSegment = lTrackSegments.get(i);
			final var lNewSegmentDefinition = new RailTrackSegmentSaveDefinition();

			railTrackSegment.saveIntoDef(lNewSegmentDefinition);

			mRailTrackSaveDefinition.railTrackSegments.add(lNewSegmentDefinition);
		}
	}
}
