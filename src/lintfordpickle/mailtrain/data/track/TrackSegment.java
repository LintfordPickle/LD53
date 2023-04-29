package lintfordpickle.mailtrain.data.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lintfordpickle.mailtrain.data.track.signals.TrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.track.signals.TrackSignalSegment;
import net.lintford.library.core.entity.BaseInstanceData;

public class TrackSegment extends TrackEdge {

	// |---------- Track Segment --------------|
	// |-SS-||-----Signal Segment -----||--SS--|

	public class SegmentSignals extends BaseInstanceData {

		private static final long serialVersionUID = -7109916601842567422L;

		// ---------------------------------------------
		// Variables
		// ---------------------------------------------

		// We can't keep a reference to TrackSignalBlock here, because
		// each SegmentSignals contains multiple TrackSignalSegments, some of which may
		// belong to difference signal blocks.

		transient TrackSegment trackSegment;
		public final List<Integer> signalSegmentUids = new ArrayList<>();
		public transient List<TrackSignalSegment> signals;
		public int logigalUpdateCounter;
		public int destinationUid;

		// ---------------------------------------------
		// Constructor
		// ---------------------------------------------

		public SegmentSignals(TrackSegment pTrackSegment) {
			trackSegment = pTrackSegment;
			signals = new ArrayList<>();
			logigalUpdateCounter = 0;
		}

		// ---------------------------------------------
		// Core-Methods
		// ---------------------------------------------

		public void afterDeserialization(Track pTrack) {
			signals = new ArrayList<>();

			// Need to resolve the trackSignalSegment uid references
			final int lNumSignalSegmentReferences = signalSegmentUids.size();
			for (int i = 0; i < lNumSignalSegmentReferences; i++) {
				Integer lNextUid = signalSegmentUids.get(i);
				final var lSignalSegment = pTrack.trackSignalSegments.getInstanceByUid(lNextUid);
				lSignalSegment.trackSegment = trackSegment;

				signals.add(lSignalSegment);

			}
		}

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

		public void addTrackSignalSegment(Track pTrack, float pDist) {
			final var lNewTrackSignal = pTrack.trackSignalSegments.getFreePooledItem();
			signalSegmentUids.add(lNewTrackSignal.poolUid);
			addSignalSegment(lNewTrackSignal, pDist);
		}

		public void addTrackSignal(Track pTrack, float pDist) {
			final var lNewTrackSignal = pTrack.trackSignalSegments.getFreePooledItem();
			lNewTrackSignal.isSignalHead = true;

			signalSegmentUids.add(lNewTrackSignal.poolUid);
			addSignalSegment(lNewTrackSignal, pDist);
		}

		private void addSignalSegment(TrackSignalSegment pNewSegment, float pDist) {
			pNewSegment.trackSegmentUid = trackSegment.uid;
			pNewSegment.trackSegment = trackSegment;
			pNewSegment.startDistance = pDist;
			pNewSegment.destinationNodeUid = destinationUid;

			signals.add(pNewSegment);

			Collections.sort(signals);

			// update the segment lengths

			final int lNumSegments = signals.size();
			float lRemainingDist = 1.f;
			for (int i = lNumSegments - 1; i >= 0; i--) {
				final var lSignal = signals.get(i);
				float lDist = lRemainingDist - lSignal.startDistance;
				lSignal.length = lDist;
				lRemainingDist = lSignal.startDistance;
			}
			// update the parentage ...
		}

		// ---------------------------------------------

		public TrackSignalSegment getSignal(float pDistIntoSegment) {
			final int lNumSignals = signals.size();
			if (lNumSignals == 1)
				return signals.get(0);

			var lSignalSegment = signals.get(0);
			for (int i = 0; i < lNumSignals; i++) {
				final var lSignalToCheck = signals.get(i);
				if (lSignalToCheck.startDistance <= pDistIntoSegment && pDistIntoSegment <= lSignalToCheck.startDistance + lSignalToCheck.length)
					return lSignalToCheck; // return last valid find
				lSignalSegment = lSignalToCheck;

			}
			return lSignalSegment;
		}

		public TrackSignalSegment getNextSignal(TrackSignalSegment pSignalSegment) {
			final int lNumSignals = signals.size();
			if (lNumSignals == 1)
				return null;

			final float lDistAfter = pSignalSegment.startDistance + pSignalSegment.length;
			if (lDistAfter > 1)
				return null;
			for (int i = 0; i < lNumSignals; i++) {
				final var lSignal = signals.get(i);
				if (lSignal.startDistance >= lDistAfter) {
					return lSignal;

				}
			}
			return null;
		}

	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 8253048654834320875L;

	public static final int EDGE_TYPE_STRAIGHT = 0;
	public static final int EDGE_TYPE_CURVE = 1;

	public static final int EDGE_SPECIAL_TYPE_UNASSIGNED = 0; // nothing
	public static final int EDGE_SPECIAL_TYPE_MAP_SPAWN = 1; // player map spawn (edge needs name)
	public static final int EDGE_SPECIAL_TYPE_MAP_EXIT = 2; // player map exit point (edge needs special name)
	public static final int EDGE_SPECIAL_TYPE_MAP_EDGE = 4; // edge of map (enemy spawn / leave)
	public static final int EDGE_SPECIAL_TYPE_STATION = 8; // station / town for trading
	public static final int EDGE_SPECIAL_TYPE_ENEMY_SPAWN = 16; // station / town for trading

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public float edgeLengthInMeters;

	public int specialEdgeType = EDGE_SPECIAL_TYPE_UNASSIGNED;
	public int edgeType = EDGE_TYPE_STRAIGHT;

	public float edgeAngle;
	public transient boolean isSelected;

	public String segmentName;
	public String specialName;

	public final TrackJunction trackJunction = new TrackJunction();

	public final SegmentSignals signalsA = new SegmentSignals(this);
	public final SegmentSignals signalsB = new SegmentSignals(this);
	public int logigalUpdateCounter;

	public final List<Integer> allowedEdgeConections = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void setEdgeBitFlag(int pTypeToSet) {
		specialEdgeType = pTypeToSet;
	}

	public void setEdgeWithType(int pTypeToSet) {
		specialEdgeType |= pTypeToSet;
	}

	public boolean isEdgeOfType(int pTypeToCheck) {
		return (specialEdgeType & pTypeToCheck) == pTypeToCheck;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrackSegment(Track pTrack, final int pUid, int pNodeAUid, int pNodeBUid, float pEdgeAngle) {
		super(pUid, pNodeAUid, pNodeBUid);

		edgeAngle = pEdgeAngle;

		signalsA.destinationUid = nodeAUid;
		signalsA.addTrackSignalSegment(pTrack, 0);
		signalsB.destinationUid = nodeBUid;
		signalsB.addTrackSignalSegment(pTrack, 0);
	}

	// ---------------------------------------------

	public void afterDeserialization(Track pTrack) {
		signalsA.trackSegment = this;
		signalsA.afterDeserialization(pTrack);

		signalsB.trackSegment = this;
		signalsB.afterDeserialization(pTrack);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public int getOtherNodeUid(int pNodeUid) {
		if (nodeAUid == pNodeUid) {
			return nodeBUid;

		} else {
			return nodeAUid;

		}
	}

	public int getOtherAllowedEdgeConnectionUids() {
		final int allowedEdgeCount = allowedEdgeConections.size();
		for (int i = 0; i < allowedEdgeCount; i++) {
			if (allowedEdgeConections.get(i) != uid)
				return allowedEdgeConections.get(i);
		}
		return -1;
	}

	public int getOtherAllowedEdgeConnectionUids2() {
		boolean lFoundOne = false;
		final int allowedEdgeCount = allowedEdgeConections.size();
		for (int i = 0; i < allowedEdgeCount; i++) {
			if (allowedEdgeConections.get(i) != uid) {
				if (!lFoundOne)
					lFoundOne = true;
				else
					return allowedEdgeConections.get(i);
			}
		}
		return -1;
	}

	public static int getCommonNodeUid(TrackSegment pEdgeA, TrackSegment pEdgeB) {
		if (pEdgeA.nodeAUid == pEdgeB.nodeAUid)
			return pEdgeB.nodeAUid;
		else if (pEdgeA.nodeBUid == pEdgeB.nodeBUid)
			return pEdgeB.nodeBUid;
		else if (pEdgeA.nodeAUid == pEdgeB.nodeBUid)
			return pEdgeB.nodeBUid;
		else if (pEdgeA.nodeBUid == pEdgeB.nodeAUid)
			return pEdgeB.nodeAUid;
		return -1;
	}

	// ---------------------------------------------

	public void setBothSignalStates(int pDestinationNodeUid, float pDistIntoSegment, SignalState pNewState) {
		final var lSignalSetA = signalsA.getSignal(pDestinationNodeUid == signalsA.destinationUid ? pDistIntoSegment : 1.f - pDistIntoSegment);
		if (lSignalSetA != null && lSignalSetA.signalBlock != null) {
			lSignalSetA.signalBlock.signalState(pNewState);

		}
		final var lSignalSetB = signalsB.getSignal(pDestinationNodeUid == signalsB.destinationUid ? pDistIntoSegment : 1.f - pDistIntoSegment);
		if (lSignalSetB != null && lSignalSetB.signalBlock != null) {
			lSignalSetB.signalBlock.signalState(pNewState);

		}
	}

	public SegmentSignals getSignalsList(int pDestNodeUid) {
		if (signalsA.destinationUid == pDestNodeUid)
			return signalsA;
		if (signalsB.destinationUid == pDestNodeUid)
			return signalsB;
		return null;
	}

	public void addTrackSignal(Track pTrack, float lDist, int pDestNodeUid) {
		final var lSegmentSignals = getSignalsList(pDestNodeUid);
		if (lSegmentSignals == null)
			return;

		lSegmentSignals.trackSegment = this;
		lSegmentSignals.addTrackSignal(pTrack, lDist);
	}
}
