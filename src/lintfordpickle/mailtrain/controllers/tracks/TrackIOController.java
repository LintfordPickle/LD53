package lintfordpickle.mailtrain.controllers.tracks;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.data.track.Track;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class TrackIOController {

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public static Track loadTrackFromFile(String pFilename) {
		final var lGson = new GsonBuilder().create();
		String lTrackRawFileContents = null;
		Track lTrack = null;
		try {
			lTrackRawFileContents = FileUtils.loadString(pFilename);
			lTrack = lGson.fromJson(lTrackRawFileContents, Track.class);

			resolveEdges(lTrack);
			resolveSignals(lTrack);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(TrackIOController.class.getSimpleName(), ex);
		}

		if (lTrack == null)
			lTrack = new Track();

		lTrack.finializeLoading();

		return lTrack;
	}

	private static void resolveSignals(final Track pTrack) {
		final int lNumTrackSegments = pTrack.edges().size();
		for (int i = 0; i < lNumTrackSegments; i++) {
			final var lTrackSegment = pTrack.edges().get(i);
			lTrackSegment.afterDeserialization(pTrack);

		}
	}

	private static void resolveEdges(final Track pTrack) {
		final var lTrackEdges = pTrack.edges();
		final int lEdgeCount = lTrackEdges.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lTrackEdges.get(i);

			if (lEdge.specialEdgeType == 0)
				continue;

		}
	}
}
