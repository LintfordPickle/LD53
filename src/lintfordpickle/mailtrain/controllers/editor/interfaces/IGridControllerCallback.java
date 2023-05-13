package lintfordpickle.mailtrain.controllers.editor.interfaces;

import net.lintford.library.core.geometry.partitioning.GridEntity;
import net.lintford.library.core.geometry.partitioning.SpatialHashGrid;

public interface IGridControllerCallback {

	void gridCreated(SpatialHashGrid<GridEntity> grid);

	void gridDeleted(SpatialHashGrid<GridEntity> grid);

}
