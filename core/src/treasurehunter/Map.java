package treasurehunter;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntArray;

public class Map extends GameObject {

	public IntArray tiles = null;
	public int tilesCount;
	public int tiles_xSize;
	public int tiles_ySize;
	
	public TextureRegion tileRegion;
	public int tileRegionX, tileRegionY, tileRegionW, tileRegionH;
	
	private Rectangle view;
	private Rectangle tileWorldRect;
	
	public Map(TreasureHunter game) {
		super(game);

		tileRegionX = 2; tileRegionY = 2;
		tileRegionW = 480; tileRegionH = 160;
		tileRegion = new TextureRegion(game.assets.get("treasure-hunter-tiles.png",Texture.class));
		
		view = new Rectangle();
		tileWorldRect = new Rectangle();
	}

	public void Generate(int xSize, int ySize)
	{
		tilesCount = xSize*ySize;
		tiles_xSize = xSize;
		tiles_ySize = ySize;
		
		if (tiles == null)
		{
			tiles = new IntArray(true,tilesCount);
		}
		else
		{
			if (tiles.size < tilesCount)
			tiles.ensureCapacity(tilesCount-tiles.size);
		}
		
		tiles.size = tilesCount;
		
		for (int y = 0; y < tiles_ySize; y++)
		for (int x = 0; x < tiles_xSize; x++)
		{
			tiles.set(x+y*tiles_xSize,1);
		}
	}
	
	public void draw (Batch batch, float parentAlpha) {
		
		view.set(game.camera2d.position.x-game.camera2d.viewportWidth / 2,
				 game.camera2d.position.y-game.camera2d.viewportHeight / 2,
				 game.camera2d.viewportWidth, game.camera2d.viewportHeight);
		
		int tileIndex = 0;
		int tileIndexY = 0;
		int tileIndexX = 0;
		
		for (int y = 0; y < tiles_ySize; y++)
		for (int x = 0; x < tiles_xSize; x++)
		{
			tileIndex = tiles.get(x+y*tiles_xSize)-1;
			
			if (tileIndex < 0) continue;
				
			tileIndexY = tileIndex / 12;
			tileIndexX = tileIndex-tileIndexY*12;
			
			tileWorldRect.set(x*40, y*40, 40, 40);
			
			if (view.overlaps(tileWorldRect))
			//if (view.contains(tileWorldRect.x + tileWorldRect.width/2,tileWorldRect.y+tileWorldRect.height /2))
			{
				tileRegion.setRegion(tileRegionX+tileIndexX*40, tileRegionY+tileIndexY*40, 40, 40);
				batch.draw(tileRegion, tileWorldRect.x, tileWorldRect.y);
			}
		}
	}
}
