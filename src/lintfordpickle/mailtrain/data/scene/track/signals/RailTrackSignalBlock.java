package lintfordpickle.mailtrain.data.scene.track.signals;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSignalBlockSaveDefinition;
import net.lintford.library.core.entities.instances.ClosedPooledBaseData;

public class RailTrackSignalBlock extends ClosedPooledBaseData {

	// ---------------------------------------------
	// Constants & Enums
	// ---------------------------------------------

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

	private transient final List<RailTrackSignalSegment> mSignalSegments = new ArrayList<>();
	private final List<Integer> mSignalSegmentIndices = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public List<RailTrackSignalSegment> signalSegments() {
		return mSignalSegments;
	}

	public List<Integer> signalSegmentIndices() {
		return mSignalSegmentIndices;
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

	public RailTrackSignalBlock(final int pUid) {
		super(pUid);
	}

	public RailTrackSignalBlock(RailTrackSignalBlockSaveDefinition saveDef) {
		super(saveDef.uid);
	}

	// ---------------------------------------------
	// IO Methods
	// ---------------------------------------------

	public void loadFromDef(RailTrackSignalBlockSaveDefinition saveDef) {
		// TODO: Need to move away from poolUid

		saveDef.signalState = mSignalState;
		saveDef.signalSegmentIndices.addAll(mSignalSegmentIndices);
	}

	public void saveIntoDef(RailTrackSignalBlockSaveDefinition saveDef) {
		saveDef.uid = uid;

		saveDef.signalState = mSignalState;
		saveDef.signalSegmentIndices.addAll(mSignalSegmentIndices);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void reset() {
		mSignalState = SignalState.Open;
		if (mSignalSegments != null) {
//			final int lSignalSegmentCount = mSignalSegments.size();
//			for (int i = 0; i < lSignalSegmentCount; i++) {
//				mSignalSegments.get(i).resetSignalBlock();
//			}
			mSignalSegments.clear();
		}

		if (mSignalSegmentIndices != null)
			mSignalSegmentIndices.clear();
	}

	public void acquireOtherBlock(RailTrackSignalBlock pOtherBlock) {
		mSignalSegments.addAll(pOtherBlock.mSignalSegments);

		// update the references of the newly acquired signals
		final int lNumSignals = mSignalSegments.size();
		for (int i = 0; i < lNumSignals; i++) {
			mSignalSegments.get(i).signalBlock = this;

		}
	}
}
