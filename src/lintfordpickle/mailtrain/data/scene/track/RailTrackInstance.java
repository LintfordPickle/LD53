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

	private List<RailTrackSegment> mEdges;

	private int mTrackLogicalCounter;

	private int nodeUidCounter = 0;

	private int edgeUidCounter = 0;

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

	public int getNumberTrackEdges() {
		return mEdges.size();
	}

	public List<RailTrackSegment> edges() {
		return mEdges;
	}

	public void setNodeUidCounter(int nodeUidCounter) {
		this.nodeUidCounter = nodeUidCounter;
	}

	public void setEdgeUidCounter(int edgeUidCounter) {
		this.edgeUidCounter = edgeUidCounter;
	}

	public int getNodeUidCounter() {
		return nodeUidCounter;
	}

	public int getEdgeUidCounter() {
		return edgeUidCounter;
	}

	public int getNewNodeUid() {
		return nodeUidCounter++;
	}

	public int getNewEdgeUid() {
		return edgeUidCounter++;
	}

	public RailTrackSegment getEdgeByUid(final int pUid) {
		final int edgeCount = mEdges.size();
		for (int i = 0; i < edgeCount; i++) {
			if (mEdges.get(i).uid == pUid)
				return mEdges.get(i);

		}
		return null;
	}

	public RailTrackSegment getEdgeByName(final String segmentName) {
		final int edgeCount = mEdges.size();
		for (int i = 0; i < edgeCount; i++) {
			if (mEdges.get(i).segmentName == null)
				continue;

			if (mEdges.get(i).segmentName.equals(segmentName))
				return mEdges.get(i);

		}
		return null;
	}

	public RailTrackNode getNodeByUid(final int pUid) {
		final int nodeCount = mNodes.size();
		for (int i = 0; i < nodeCount; i++) {
			if (mNodes.get(i).uid == pUid)
				return mNodes.get(i);

		}
		return null;
	}

	public boolean edgeExistsBetween(int pUidA, int pUidB) {
		return getEdgeBetweenNodes(pUidA, pUidB) != null;
	}

	public RailTrackSegment getEdgeBetweenNodes(int pUidA, int pUidB) {
		if (pUidA == pUidB)
			return null;

		final int edgeCount = mEdges.size();
		for (int i = 0; i < edgeCount; i++) {
			final int lEdgeNodeA = mEdges.get(i).nodeAUid;
			final int lEdgeNodeB = mEdges.get(i).nodeBUid;
			if ((lEdgeNodeA == pUidA || lEdgeNodeA == pUidB)) {
				if ((lEdgeNodeB == pUidA || lEdgeNodeB == pUidB)) {
					return mEdges.get(i);
				}
			}
		}
		return null;
	}

	public static float worldToGrid(final float pWorldCoord, final float pGridSizeInPixels) {
		return pWorldCoord - (pWorldCoord % pGridSizeInPixels) - (pWorldCoord < 0.f ? pGridSizeInPixels : 0) + pGridSizeInPixels * .5f;
	}

	public float getEdgeLength(RailTrackSegment pEdge) {
		var lNodeA = getNodeByUid(pEdge.nodeAUid);
		var lNodeB = getNodeByUid(pEdge.nodeBUid);
		if (pEdge.edgeType == RailTrackSegment.EDGE_TYPE_CURVE) {
			float lDist = 0.f;

			float lLastPointX = MathHelper.bezier4CurveTo(0, lNodeA.x, pEdge.control0X, pEdge.control1X, lNodeB.x);
			float lLastPointY = MathHelper.bezier4CurveTo(0, lNodeA.y, pEdge.control0Y, pEdge.control1Y, lNodeB.y);

			// Same code as in TrackEditorRenderer - consider moving to a helper class I guess
			final float lStepSize = 0.01f;
			for (float t = lStepSize; t <= 1.f; t += lStepSize) {
				final float lNewPointX = MathHelper.bezier4CurveTo(t, lNodeA.x, pEdge.control0X, pEdge.control1X, lNodeB.x);
				final float lNewPointY = MathHelper.bezier4CurveTo(t, lNodeA.y, pEdge.control0Y, pEdge.control1Y, lNodeB.y);

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
		mEdges = new ArrayList<>();
	}

	public void reset() {
		nodeUidCounter = 0;
		edgeUidCounter = 0;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void finalizeAfterLoading(Object pParent) {
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
		final int lSegmentCount = mEdges.size();
		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = mEdges.get(i);
			lSegment.finalizeAfterLoading(this);

			if (lSegment.uid > edgeUidCounter)
				edgeUidCounter = lSegment.uid;
		}

		trackSignalBlocks.finalizeAfterLoading();
		trackSignalSegments.finalizeAfterLoading();

		nodeUidCounter++;
		edgeUidCounter++;
	}

	public RailTrackSegment getNextEdge(RailTrackSegment currentEdge, int destinationNodeUid) {
		RailTrackSegment lReturnEdge = null;

		final var lCurrentNode = getNodeByUid(destinationNodeUid);

		// If this is a signal node, then we need to take into consideration which path is currently active.
		if (currentEdge.trackJunction != null && currentEdge.trackJunction.isSignalActive && lCurrentNode.uid == currentEdge.trackJunction.signalNodeUid) {
			final var lActiveEdgeUid = currentEdge.trackJunction.leftEnabled ? currentEdge.trackJunction.leftEdgeUid : currentEdge.trackJunction.rightEdgeUid;
			return getEdgeByUid(lActiveEdgeUid);

		}
		lReturnEdge = lCurrentNode.getRandomWhitelistedEdgeApartFrom(currentEdge, destinationNodeUid);

		if (lReturnEdge == null) {
			// If there are no whitelisted edges, then just pick a 'valid' track ...
			// TODO: this is commented out to simulate a de-railment at the next arrvied at node.
			lReturnEdge = lCurrentNode.getRandomEdgeApartFrom(currentEdge, destinationNodeUid);
		}

		return lReturnEdge;
	}

	public float getPositionAlongEdgeX(RailTrackSegment segment, int fromUid, float normalizedDist) {
		var lNodeA = getNodeByUid(segment.nodeAUid);
		var lNodeB = getNodeByUid(segment.nodeBUid);
		switch (segment.edgeType) {
		case RailTrackSegment.EDGE_TYPE_STRAIGHT:
			if (segment.nodeAUid != fromUid)
				normalizedDist = 1.f - normalizedDist;

			final float lLength = lNodeB.x - lNodeA.x;
			return lNodeA.x + lLength * normalizedDist;
		case RailTrackSegment.EDGE_TYPE_CURVE:
			if (segment.nodeAUid != fromUid) {
				normalizedDist = 1.f - normalizedDist;
			}

			return MathHelper.bezier4CurveTo(normalizedDist, lNodeA.x, segment.control0X, segment.control1X, lNodeB.x);

		}
		return 0.f;
	}

	public float getPositionAlongEdgeY(RailTrackSegment segment, int fromUid, float normalizedDist) {
		var lNodeA = getNodeByUid(segment.nodeAUid);
		var lNodeB = getNodeByUid(segment.nodeBUid);
		switch (segment.edgeType) {
		case RailTrackSegment.EDGE_TYPE_STRAIGHT:
			if (segment.nodeAUid != fromUid)
				normalizedDist = 1.f - normalizedDist;

			final float lLength = lNodeB.y - lNodeA.y;
			return lNodeA.y + lLength * normalizedDist;
		case RailTrackSegment.EDGE_TYPE_CURVE:
			if (segment.nodeAUid != fromUid) {
				normalizedDist = 1.f - normalizedDist;
			}

			return MathHelper.bezier4CurveTo(normalizedDist, lNodeA.y, segment.control0Y, segment.control1Y, lNodeB.y);

		}
		return 0.f;
	}
}
