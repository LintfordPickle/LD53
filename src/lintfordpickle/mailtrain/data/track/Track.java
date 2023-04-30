package lintfordpickle.mailtrain.data.track;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.track.signals.TrackSignalBlock;
import lintfordpickle.mailtrain.data.track.signals.TrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.track.signals.TrackSignalSegment;
import net.lintford.library.core.entity.BaseInstanceData;
import net.lintford.library.core.entity.instances.IndexedPoolInstanceManager;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;

public class Track extends BaseInstanceData {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -2259371698144835441L;

	public class TrackSignalBlockManager extends IndexedPoolInstanceManager<TrackSignalBlock> {

		// ---------------------------------------------
		// Constants
		// ---------------------------------------------

		private static final long serialVersionUID = 5292390238656274462L;

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

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
		protected TrackSignalBlock createPoolObjectInstance() {
			return new TrackSignalBlock(getNewInstanceUID());
		}
	}

	public class TrackSignalSegmentManager extends IndexedPoolInstanceManager<TrackSignalSegment> {

		// ---------------------------------------------
		// Constants
		// ---------------------------------------------

		private static final long serialVersionUID = -4528366524067066723L;

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

		@Override
		protected TrackSignalSegment createPoolObjectInstance() {
			return new TrackSignalSegment(getNewInstanceUID());
		}
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final TrackSignalBlockManager trackSignalBlocks = new TrackSignalBlockManager();
	public final TrackSignalSegmentManager trackSignalSegments = new TrackSignalSegmentManager();

	public final float gridSizeInPixels = 32.f;
	public final int maxGridWidth = 256;
	public final int maxGridHeight = 256;

	private int nodeUidCounter = 0;
	private int edgeUidCounter = 0;

	private List<TrackNode> mNodes;
	private List<TrackSegment> mEdges;

	private int mTrackLogicalCounter;

	public boolean areSignalsDirty;

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

	public List<TrackNode> nodes() {
		return mNodes;
	}

	public int getNumberTrackEdges() {
		return mEdges.size();
	}

	public List<TrackSegment> edges() {
		return mEdges;
	}

	public int getNewNodeUid() {
		return nodeUidCounter++;
	}

	public int getNewEdgeUid() {
		return edgeUidCounter++;
	}

	public TrackSegment getEdgeByUid(final int pUid) {
		final int edgeCount = mEdges.size();
		for (int i = 0; i < edgeCount; i++) {
			if (mEdges.get(i).uid == pUid)
				return mEdges.get(i);

		}
		return null;
	}

	public TrackSegment getEdgeByName(final String segmentName) {
		final int edgeCount = mEdges.size();
		for (int i = 0; i < edgeCount; i++) {
			if (mEdges.get(i).segmentName == null)
				continue;

			if (mEdges.get(i).segmentName.equals(segmentName))
				return mEdges.get(i);

		}
		return null;
	}

	public TrackNode getNodeByUid(final int pUid) {
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

	public TrackSegment getEdgeBetweenNodes(int pUidA, int pUidB) {
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

	public float getEdgeLength(TrackSegment pEdge) {
		var lNodeA = getNodeByUid(pEdge.nodeAUid);
		var lNodeB = getNodeByUid(pEdge.nodeBUid);
		if (pEdge.edgeType == TrackSegment.EDGE_TYPE_CURVE) {
			float lDist = 0.f;

			float lLastPointX = MathHelper.bezier4CurveTo(0, lNodeA.x, pEdge.lControl0X, pEdge.lControl1X, lNodeB.x);
			float lLastPointY = MathHelper.bezier4CurveTo(0, lNodeA.y, pEdge.lControl0Y, pEdge.lControl1Y, lNodeB.y);

			// Same code as in TrackEditorRenderer - consider moving to a helper class I guess
			final float lStepSize = 0.01f;
			for (float t = lStepSize; t <= 1.f; t += lStepSize) {
				final float lNewPointX = MathHelper.bezier4CurveTo(t, lNodeA.x, pEdge.lControl0X, pEdge.lControl1X, lNodeB.x);
				final float lNewPointY = MathHelper.bezier4CurveTo(t, lNodeA.y, pEdge.lControl0Y, pEdge.lControl1Y, lNodeB.y);

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

	public Track() {
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

	public void finializeLoading() {
		resolveTrackEdges();
	}

	private void resolveTrackEdges() {
		final int lNodeCount = getNumberTrackNodes();
		for (int i = 0; i < lNodeCount; i++) {
			mNodes.get(i).resolveEdges(this);

		}
	}

	public int worldToGrid(float pWorldCoord) {
		return (int) (pWorldCoord / gridSizeInPixels);
	}

	public float gridToWorld(int pGridCoord) {
		return (pGridCoord * gridSizeInPixels);
	}

	public int getGridIndex(int pGridX, int pGridY) {
		return pGridY * maxGridWidth + pGridX;
	}

	public boolean getIsGridCoordinateInBounds(int pGridX, int pGridY) {
		final int lResult = getGridIndex(pGridX, pGridY);
		return lResult >= 0 && lResult < maxGridWidth * maxGridHeight - 1;
	}

	public TrackSegment getNextEdge(TrackSegment pCurrentEdge, int pDestinationNodeUid) {
		TrackSegment lReturnEdge = null;

		final var lCurrentNode = getNodeByUid(pDestinationNodeUid);

		// If this is a signal node, then we need to take into consideration which path is currently active.
		if (pCurrentEdge.trackJunction != null && pCurrentEdge.trackJunction.isSignalActive && lCurrentNode.uid == pCurrentEdge.trackJunction.signalNodeUid) {
			final var lActiveEdgeUid = pCurrentEdge.trackJunction.leftEnabled ? pCurrentEdge.trackJunction.leftEdgeUid : pCurrentEdge.trackJunction.rightEdgeUid;
			return getEdgeByUid(lActiveEdgeUid);

		}
		lReturnEdge = lCurrentNode.getRandomEdgeApartFrom(pCurrentEdge, pDestinationNodeUid);
		return lReturnEdge;
	}

	public float getPositionAlongEdgeX(TrackSegment pEdge, int pFromUid, float pNormalizedDist) {
		var lNodeA = getNodeByUid(pEdge.nodeAUid);
		var lNodeB = getNodeByUid(pEdge.nodeBUid);
		switch (pEdge.edgeType) {
		case TrackSegment.EDGE_TYPE_STRAIGHT:
			if (pEdge.nodeAUid != pFromUid)
				pNormalizedDist = 1.f - pNormalizedDist;

			final float lLength = lNodeB.x - lNodeA.x;
			return lNodeA.x + lLength * pNormalizedDist;
		case TrackSegment.EDGE_TYPE_CURVE:
			if (pEdge.nodeAUid != pFromUid) {
				pNormalizedDist = 1.f - pNormalizedDist;
			}

			boolean lFlipControlPoints = false;
			if (lNodeA.x > lNodeB.x) {
				if (lNodeA.y > lNodeB.y) {
					lFlipControlPoints = true;
				} else {
					lFlipControlPoints = false;
				}
			} else {
				if (lNodeA.y > lNodeB.y) {
					lFlipControlPoints = true;
				} else {
					lFlipControlPoints = false;
				}
			}

			return MathHelper.bezier4CurveTo(pNormalizedDist, lNodeA.x, lFlipControlPoints ? pEdge.lControl1X : pEdge.lControl0X, lFlipControlPoints ? pEdge.lControl0X : pEdge.lControl1X, lNodeB.x);

		}
		return 0.f;
	}

	public float getPositionAlongEdgeY(TrackSegment pEdge, int pFromUid, float pNormalizedDist) {
		var lNodeA = getNodeByUid(pEdge.nodeAUid);
		var lNodeB = getNodeByUid(pEdge.nodeBUid);
		switch (pEdge.edgeType) {
		case TrackSegment.EDGE_TYPE_STRAIGHT:
			if (pEdge.nodeAUid != pFromUid)
				pNormalizedDist = 1.f - pNormalizedDist;

			final float lLength = lNodeB.y - lNodeA.y;
			return lNodeA.y + lLength * pNormalizedDist;
		case TrackSegment.EDGE_TYPE_CURVE:
			if (pEdge.nodeAUid != pFromUid) {
				pNormalizedDist = 1.f - pNormalizedDist;
			}

			boolean lFlipControlPoints = false;
			if (lNodeA.x > lNodeB.x) {
				if (lNodeA.y > lNodeB.y) {
					lFlipControlPoints = true;
				} else {
					lFlipControlPoints = false;
				}
			} else {
				if (lNodeA.y > lNodeB.y) {
					lFlipControlPoints = true;
				} else {
					lFlipControlPoints = false;
				}
			}

			return MathHelper.bezier4CurveTo(pNormalizedDist, lNodeA.y, lFlipControlPoints ? pEdge.lControl1Y : pEdge.lControl0Y, lFlipControlPoints ? pEdge.lControl0Y : pEdge.lControl1Y, lNodeB.y);

		}
		return 0.f;
	}
}
