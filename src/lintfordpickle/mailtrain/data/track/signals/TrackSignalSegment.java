package lintfordpickle.mailtrain.data.track.signals;

import lintfordpickle.mailtrain.data.track.TrackSegment;
import net.lintford.library.core.entity.instances.IndexedPooledBaseData;

public class TrackSignalSegment extends IndexedPooledBaseData implements Comparable<TrackSignalSegment> {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 6539871720735626541L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public int trackSegmentUid; // track segment to which this signal segment belongs
	public transient TrackSegment trackSegment;
	public TrackSignalBlock signalBlock; // member of

	public boolean isSignalHead; //

	public int destinationNodeUid; // so we know which direction
	public float startDistance; // The start location of this signal (in node space)
	public float length; // the length is updated on each signal insertion into the same node

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrackSignalSegment(int pPoolUid) {
		super(pPoolUid);

		isSignalHead = false;
	}

	// ---------------------------------------------
	// Comparable
	// ---------------------------------------------

	@Override
	public int compareTo(TrackSignalSegment o) {
		return startDistance < o.startDistance ? -1 : 1;
	}
}