package lintfordpickle.mailtrain.data.scene.track.savedefinition;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.entities.savedefinitions.BaseSaveDefinition;

public class RailTrackSaveDefinition extends BaseSaveDefinition {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 5463585375966600606L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public List<RailTrackNodeSaveDefinition> railTrackNodes = new ArrayList<>();
	public List<RailTrackSegmentSaveDefinition> railTrackSegments = new ArrayList<>();

	public List<RailTrackSignalBlockSaveDefinition> railSignalBlocks = new ArrayList<>();
	public List<RailTrackSignalSegmentSaveDefinition> railSignalSegments = new ArrayList<>();

	public int trackInstanceLogicalCounter;

}
