package lintfordpickle.mailtrain.data.scene.trains;

import java.io.Serializable;

import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;

public class TrainFollowSegment implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -2338419397179933367L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public RailTrackSegment Segment;
	public int targetNodeUid;
	public int logicalCounter;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrainFollowSegment() {

	}

	public TrainFollowSegment(RailTrackSegment pEdge, int pDestNodeUid, int pTicker) {
		Segment = pEdge;
		targetNodeUid = pDestNodeUid;

		logicalCounter = pTicker;

	}

}
