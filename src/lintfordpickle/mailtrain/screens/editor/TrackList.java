package lintfordpickle.mailtrain.screens.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lintfordpickle.mailtrain.data.GameWorldHeader;

public class TrackList {
	
	public static List<String> getListWithTrackFilenames(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);
		final String[] lTrackList = lDirectory.list(GameWorldHeader.filter);

		return Arrays.asList(lTrackList);

	}

	public static List<File> getListWithTrackFilesSortedModified(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);
		final File[] lTrackList = lDirectory.listFiles(GameWorldHeader.filter);

		if(lTrackList == null || lTrackList.length == 0)
			return new ArrayList<>();
		
		// Sort files based on date modified (easier for testing if nothing else)
		Arrays.sort(lTrackList, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});

		return Arrays.asList(lTrackList);

	}
}
