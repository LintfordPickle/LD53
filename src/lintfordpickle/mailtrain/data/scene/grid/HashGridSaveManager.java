package lintfordpickle.mailtrain.data.scene.grid;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import net.lintford.library.core.geometry.partitioning.GridEntity;
import net.lintford.library.core.geometry.partitioning.SpatialHashGrid;

public class HashGridSaveManager implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -7527658396808701982L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	@SerializedName(value = "Width")
	public int hashGridWidth;

	@SerializedName(value = "Height")
	public int hashGridHeight;

	@SerializedName(value = "TilesWide")
	public int hashGridTilesWide;

	@SerializedName(value = "TilesHigh")
	public int hashGridTilesHigh;

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void updateSettings(SpatialHashGrid<GridEntity> hashgrid) {
		hashGridWidth = 32;// hashgrid.boundaryWidth();
		hashGridHeight = 32;// hashgrid.boundaryHeight();

		hashGridTilesWide = 2;// hashgrid.numTilesWide();
		hashGridTilesHigh = 2;// hashgrid.numTilesHigh();
	}

}
