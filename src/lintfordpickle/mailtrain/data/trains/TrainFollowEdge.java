package lintfordpickle.mailtrain.data.trains;

import java.io.Serializable;

import lintfordpickle.mailtrain.data.track.TrackSegment;

public class TrainFollowEdge implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -2338419397179933367L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public TrackSegment edge;
	public int targetNodeUid;
	public int logicalCounter;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrainFollowEdge() {

	}

	public TrainFollowEdge(TrackSegment pEdge, int pDestNodeUid, int pTicker) {
		edge = pEdge;
		targetNodeUid = pDestNodeUid;

		logicalCounter = pTicker;

	}

}
