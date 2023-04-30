package lintfordpickle.mailtrain.screens.editor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.data.world.scenes.SceneHeader;

public class TrackList {

	public static List<File> getListOfSceneFiles(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);
		final var lTrackList = lDirectory.listFiles(SceneHeader.sceneFileFilter);

		if (lTrackList == null) {
			return new ArrayList<File>();
		}

		return Arrays.asList(lTrackList);

	}

	public static List<File> getListWithTrackFilesSortedModified(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);
		final File[] lTrackList = lDirectory.listFiles(GameWorldHeader.gameWorldHeaderFileFilter);

		if (lTrackList == null || lTrackList.length == 0)
			return new ArrayList<>();

		// Sort files based on date modified (easier for testing if nothing else)
		Arrays.sort(lTrackList, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});

		return Arrays.asList(lTrackList);
	}

	public static List<File> getListOfWorldFoldersSortedModified(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);

		var directories = lDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				final var lFile = new File(current, name);
				if (lFile.isDirectory()) {
					final var lFilesInWorldFolder = lFile.listFiles();
					final int lNumFilesInFolder = lFilesInWorldFolder.length;
					for (int i = 0; i < lNumFilesInFolder; i++) {
						final var f = lFilesInWorldFolder[i];

						if (f.getName().equals(GameWorldHeader.WORLD_HEADER_FILE_NAME)) {
							return true;
						}
					}
				}

				return false;
			}
		});

		return Arrays.asList(directories);

	}
}
