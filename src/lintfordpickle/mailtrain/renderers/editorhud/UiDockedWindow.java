package lintfordpickle.mailtrain.renderers.editorhud;

import java.util.List;

import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UiWindow;

public class UiDockedWindow extends UiWindow {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final int DOCKED_WINDOW_WIDTH = 260;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private List<UiPanel> mEditorPanels;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<UiPanel> editorPanels() {
		return mEditorPanels;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UiDockedWindow(RendererManager rendererManager, String rendererName, int entityGroupUid) {
		super(rendererManager, rendererName, entityGroupUid);
		// TODO Auto-generated constructor stub
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	// --------------------------------------
	// Methods
	// --------------------------------------

}
