package lintfordpickle.mailtrain.data.track.signals;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.entity.instances.IndexedPooledBaseData;

public class TrackSignalBlock extends IndexedPooledBaseData {

	// ---------------------------------------------
	// Constants & Enums
	// ---------------------------------------------

	private static final long serialVersionUID = 8199316996442219079L;

	public enum SignalState {
		Open, // free
		Warning, // next signal block occupied
		Occupied, // train in front
		Danger, // crash
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SignalState mSignalState = SignalState.Open;

	// TODO: this shouldn't be transient - but I introduced a stack overflow when saving
	private transient final List<TrackSignalSegment> mSignalSegments = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public List<TrackSignalSegment> signalSegments() {
		return mSignalSegments;
	}

	public SignalState signalState() {
		return mSignalState;
	}

	public void signalState(SignalState pNewState) {
		mSignalState = pNewState;
		if (mSignalState == null)
			mSignalState = SignalState.Occupied;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrackSignalBlock(final int pUid) {
		super(pUid);
	}

	public void reset() {
		mSignalState = SignalState.Open;
		if (mSignalSegments != null)
			mSignalSegments.clear();
	}

	public void acquireOtherBlock(TrackSignalBlock pOtherBlock) {
		mSignalSegments.addAll(pOtherBlock.mSignalSegments);

		// update the references of the newly acquired signals
		final int lNumSignals = mSignalSegments.size();
		for (int i = 0; i < lNumSignals; i++) {
			mSignalSegments.get(i).signalBlock = this;

		}
	}
}
