package lintfordpickle.mailtrain.data.scene.track;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalSegment;
import net.lintford.library.core.entities.instances.ClosedPoolInstanceManager;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;

public class RailTrackInstance {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public class RailTrackSignalBlockManager extends ClosedPoolInstanceManager<RailTrackSignalBlock> {

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

		public void finalizeAfterLoading() {
			refreshInstanceUidCounter();
		}

		public void openSignalSegmentStates() {
			final int lNumSignalSegments = numInstances();
			for (int i = 0; i < lNumSignalSegments; i++) {
				mInstances.get(i).signalState(SignalState.Open);
			}
		}

		public void resetSignalSegments() {
			final int lNumSignalSegments = numInstances();
			for (int i = 0; i < lNumSignalSegments; i++) {
				mInstances.get(i).reset();
			}
		}

		@Override
		protected RailTrackSignalBlock createPoolObjectInstance() {
			return new RailTrackSignalBlock(getNewInstanceUID());
		}

		public void addSignalBlock(RailTrackSignalBlock lNewSignalBlock) {
			if (mInstances.contains(lNewSignalBlock) == false)
				mInstances.add(lNewSignalBlock);
		}

	}

	public class RailTrackSignalSegmentManager extends ClosedPoolInstanceManager<RailTrackSignalSegment> {

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

		public void finalizeAfterLoading() {
			refreshInstanceUidCounter();
		}

		@Override
		protected RailTrackSignalSegment createPoolObjectInstance() {
			return new RailTrackSignalSegment(getNewInstanceUID());
		}

		public void addSignalSegment(RailTrackSignalSegment newSignalSegment) {
			if (mInstances.contains(newSignalSegment) == false)
				mInstances.add(newSignalSegment);
		}
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final RailTrackSignalBlockManager trackSignalBlocks = new RailTrackSignalBlockManager();

	public final RailTrackSignalSegmentManager trackSignalSegments = new RailTrackSignalSegmentManager();

	private List<RailTrackNode> mNodes;

	private List<RailTrackSegment> mSegments;

	private int mTrackLogicalCounter;

	private int nodeUidCounter = 0;

	private int segmentUidCounter = 0;

	public transient boolean areSignalsDirty; // signals are initially dirty (after deserialization) or signal insertion

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int trackLogicalCounter() {
		return mTrackLogicalCounter;
	}

	public void trackLogicalCounter(int pLogicalCounter) {
		mTrackLogicalCounter = pLogicalCounter;
	}

	public int getNumberTrackNodes() {
		return mNodes.size();
	}

	public List<RailTrackNode> nodes() {
		return mNodes;
	}

	public int getNumberTrackSegments() {
		return mSegments.size();
	}

	public List<RailTrackSegment> segments() {
		return mSegments;
	}

	public void setNodeUidCounter(int nodeUidCounter) {
		this.nodeUidCounter = nodeUidCounter;
	}

	public void setSegmentUidCounter(int segmentUidCounter) {
		this.segmentUidCounter = segmentUidCounter;
	}

	public int getNodeUidCounter() {
		return nodeUidCounter;
	}

	public int getSegmentUidCounter() {
		return segmentUidCounter;
	}

	public int getNewNodeUid() {
		return nodeUidCounter++;
	}

	public int getNewSegmentUid() {
		return segmentUidCounter++;
	}

	public RailTrackSegment getSegmentByUid(final int pUid) {
		final int lSegmentCount = mSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			if (mSegments.get(i).uid == pUid)
				return mSegments.get(i);

		}
		return null;
	}

	public RailTrackSegment getSegmentByName(final String segmentName) {
		final int lSegmentCount = mSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			if (mSegments.get(i).segmentName == null)
				continue;

			if (mSegments.get(i).segmentName.equals(segmentName))
				return mSegments.get(i);

		}
		return null;
	}

	public RailTrackSegment getSegmentByType(int segmentType) {
		final int lSegmentCount = mSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = mSegments.get(i);
			if (lSegment.segmentName == null)
				continue;

			if (lSegment.isSegmentOfType(segmentType))
				return lSegment;

		}
		return null;
	}

	public RailTrackNode getNodeByUid(final int nodeUid) {
		final int nodeCount = mNodes.size();
		for (int i = 0; i < nodeCount; i++) {
			if (mNodes.get(i).uid == nodeUid)
				return mNodes.get(i);

		}
		return null;
	}

	public boolean doesSegmentExistsBetween(int nodeUidA, int nodeUidB) {
		return getSegmentBetweenNodes(nodeUidA, nodeUidB) != null;
	}

	public RailTrackSegment getSegmentBetweenNodes(int nodeUidA, int nodeUidB) {
		if (nodeUidA == nodeUidB)
			return null;

		final int lSegmentCount = mSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final int lSegmentNodeA = mSegments.get(i).nodeAUid;
			final int lSegmentNodeB = mSegments.get(i).nodeBUid;
			if ((lSegmentNodeA == nodeUidA || lSegmentNodeA == nodeUidB)) {
				if ((lSegmentNodeB == nodeUidA || lSegmentNodeB == nodeUidB)) {
					return mSegments.get(i);
				}
			}
		}
		return null;
	}

	public static float worldToGrid(final float worldCoord, final float gridSizeInPixels) {
		return worldCoord - (worldCoord % gridSizeInPixels) - (worldCoord < 0.f ? gridSizeInPixels : 0) + gridSizeInPixels * .5f;
	}

	public float getSegmentLength(RailTrackSegment segment) {
		var lNodeA = getNodeByUid(segment.nodeAUid);
		var lNodeB = getNodeByUid(segment.nodeBUid);
		if (segment.segmentType == RailTrackSegment.SEGMENT_TYPE_CURVE) {
			float lDist = 0.f;

			float lLastPointX = MathHelper.bezier4CurveTo(0, lNodeA.x, segment.control0X, segment.control1X, lNodeB.x);
			float lLastPointY = MathHelper.bezier4CurveTo(0, lNodeA.y, segment.control0Y, segment.control1Y, lNodeB.y);

			// Same code as in TrackEditorRenderer - consider moving to a helper class I guess
			final float lStepSize = 0.01f;
			for (float t = lStepSize; t <= 1.f; t += lStepSize) {
				final float lNewPointX = MathHelper.bezier4CurveTo(t, lNodeA.x, segment.control0X, segment.control1X, lNodeB.x);
				final float lNewPointY = MathHelper.bezier4CurveTo(t, lNodeA.y, segment.control0Y, segment.control1Y, lNodeB.y);

				lDist += Vector2f.dst(lLastPointX, lLastPointY, lNewPointX, lNewPointY);

				lLastPointX = lNewPointX;
				lLastPointY = lNewPointY;

			}
			return lDist;
		} else { // straight
			return Vector2f.dst(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y);
		}
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public RailTrackInstance() {
		mNodes = new ArrayList<>();
		mSegments = new ArrayList<>();
	}

	public void reset() {
		nodeUidCounter = 0;
		segmentUidCounter = 0;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void finalizeAfterLoading(Object parent) {
		resolveTrackComponents();
	}

	private void resolveTrackComponents() {
		// Resolve segments
		final int lNodeCount = getNumberTrackNodes();
		for (int i = 0; i < lNodeCount; i++) {
			final var lNode = mNodes.get(i);
			lNode.finalizeAfterLoading(this);
			if (lNode.uid > nodeUidCounter)
				nodeUidCounter = lNode.uid;
		}

		// Resolve signal segments
		final int lSegmentCount = mSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = mSegments.get(i);
			lSegment.finalizeAfterLoading(this);

			if (lSegment.uid > segmentUidCounter)
				segmentUidCounter = lSegment.uid;
		}

		trackSignalBlocks.finalizeAfterLoading();
		trackSignalSegments.finalizeAfterLoading();

		nodeUidCounter++;
		segmentUidCounter++;
	}

	public RailTrackSegment getNextSegment(RailTrackSegment currentSegment, int destinationNodeUid) {
		final var lDestinationNode = getNodeByUid(destinationNodeUid);

		final var lDestinationSegmentUid = lDestinationNode.trackSwitch.getOutSegmentUid(currentSegment.uid);
		if (lDestinationSegmentUid == -1) {
			return null; // no available connecting lines
		}

		return getSegmentByUid(lDestinationSegmentUid);
	}

	public float getPositionAlongSegmentX(RailTrackSegment segment, int fromUid, float normalizedDist) {
		var lNodeA = getNodeByUid(segment.nodeAUid);
		var lNodeB = getNodeByUid(segment.nodeBUid);
		switch (segment.segmentType) {
		case RailTrackSegment.SEGMENT_TYPE_STRAIGHT:
			if (segment.nodeAUid != fromUid)
				normalizedDist = 1.f - normalizedDist;

			final float lLength = lNodeB.x - lNodeA.x;
			return lNodeA.x + lLength * normalizedDist;
		case RailTrackSegment.SEGMENT_TYPE_CURVE:
			if (segment.nodeAUid != fromUid) {
				normalizedDist = 1.f - normalizedDist;
			}

			return MathHelper.bezier4CurveTo(normalizedDist, lNodeA.x, segment.control0X, segment.control1X, lNodeB.x);

		}
		return 0.f;
	}

	public float getPositionAlongSegmentY(RailTrackSegment segment, int fromUid, float normalizedDist) {
		var lNodeA = getNodeByUid(segment.nodeAUid);
		var lNodeB = getNodeByUid(segment.nodeBUid);
		switch (segment.segmentType) {
		case RailTrackSegment.SEGMENT_TYPE_STRAIGHT:
			if (segment.nodeAUid != fromUid)
				normalizedDist = 1.f - normalizedDist;

			final float lLength = lNodeB.y - lNodeA.y;
			return lNodeA.y + lLength * normalizedDist;
		case RailTrackSegment.SEGMENT_TYPE_CURVE:
			if (segment.nodeAUid != fromUid) {
				normalizedDist = 1.f - normalizedDist;
			}

			return MathHelper.bezier4CurveTo(normalizedDist, lNodeA.y, segment.control0Y, segment.control1Y, lNodeB.y);

		}
		return 0.f;
	}
}
