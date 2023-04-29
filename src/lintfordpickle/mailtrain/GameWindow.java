package lintfordpickle.mailtrain;

import net.lintford.library.GameInfo;
import net.lintford.library.GameVersion;
import net.lintford.library.core.debug.Debug.DebugLogLevel;

public class GameWindow extends MailtrainGame {

	private final int APP_VERSION_MAJ = 0;
	private final int APP_VERSION_MIN = 1;
	private final int APP_VERSION_BUILD = 1;

	private final String APP_POSTFIX = "29042023";

	// ---------------------------------------------
	// Entry Point
	// ---------------------------------------------

	public static void main(String[] args) {

		final var lGameInfo = new GameInfo() {
			@Override
			public DebugLogLevel debugLogLevel() {
				return DebugLogLevel.info;
			}

			@Override
			public String applicationName() {
				return ConstantsGame.APPLICATION_NAME;
			}

			@Override
			public String windowTitle() {
				return ConstantsGame.WINDOW_TITLE;
			}

			@Override
			public int minimumWindowWidth() {
				return ConstantsGame.GAME_CANVAS_WIDTH;
			}

			@Override
			public int minimumWindowHeight() {
				return ConstantsGame.GAME_CANVAS_HEIGHT;
			}

			@Override
			public int gameCanvasResolutionWidth() {
				return ConstantsGame.GAME_CANVAS_WIDTH;
			}

			@Override
			public int gameCanvasResolutionHeight() {
				return ConstantsGame.GAME_CANVAS_HEIGHT;
			}

			@Override
			public boolean stretchGameResolution() {
				return true;
			}

			@Override
			public boolean windowResizeable() {
				return true;
			}
		};

		final var lClient = new GameWindow(lGameInfo, args);
		lClient.createWindow();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWindow(GameInfo gameInfo, String[] args) {
		super(gameInfo, args);

		setGameVersion();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void setGameVersion() {
		GameVersion.setGameVersion(APP_VERSION_MAJ, APP_VERSION_MIN, APP_VERSION_BUILD, APP_POSTFIX);
	}

}
