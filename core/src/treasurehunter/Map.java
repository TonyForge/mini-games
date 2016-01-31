package treasurehunter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

public class Map extends GameObject {

	public DungeonBSP dungeonBSP = null;
	public IntArray tiles = null;
	public IntArray waveMap = null;
	
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
	
	public int GetRandomFloor()
	{
		float rnd = (float) Math.random();
		
		if (rnd > 0.6f) return 1;
		else
		{
			rnd = (float) Math.random();
		}
		
		return 2+(int)Math.floor((10f*rnd));
	}
	
	public void Generate(int xSize, int ySize)
	{
		tilesCount = xSize*ySize;
		tiles_xSize = xSize;
		tiles_ySize = ySize;
		
		if (tiles == null)
		{
			tiles = new IntArray(true,tilesCount);
			waveMap = new IntArray(true,tilesCount);
		}
		else
		{
			if (tiles.size < tilesCount)
			{
				tiles.ensureCapacity(tilesCount-tiles.size);
				waveMap.ensureCapacity(tilesCount-tiles.size);
			}
		}
		
		tiles.size = tilesCount;
		waveMap.size = tilesCount;
		
		int x = 0;
		int y = 0;
		
		for (y = 0; y < tiles_ySize; y++)
		for (x = 0; x < tiles_xSize; x++)
		{
			tiles.set(x+y*tiles_xSize,0);
			waveMap.set(x+y*tiles_xSize,0);
		}
		
		//generate dungeon
		dungeonBSP = new DungeonBSP();
		dungeonBSP.GenerateDungeon(0, 0, xSize, ySize);
		
		//set halls
		dungeonBSP.ParseRooms_Begin();
		Rectangle room = null;
		LinkedList<Vector2> halls = null;
		
		while (dungeonBSP.ParseRooms_Next())
		{
			room = dungeonBSP.parseRooms_room;
			halls = dungeonBSP.parseRooms_halls;
			
			if (room == null)
			if (halls != null)
			{
				Iterator<Vector2> it = halls.iterator();
				if (it.hasNext())
				{
					Vector2 startPoint = it.next();
					Vector2 endPoint;
					Vector2 tmpVector = new Vector2();
					
					int len;
					while (it.hasNext())
					{
						endPoint = it.next();

						tmpVector.x = endPoint.x - startPoint.x;
						tmpVector.y = endPoint.y - startPoint.y;
						len = (int) Math.ceil(tmpVector.len());
						
						tmpVector.setLength(1);
						
						for (int i = 0; i < len; i++)
						{
							tiles.set((int)(startPoint.x + tmpVector.x*i) + ((int)(startPoint.y + tmpVector.y*i))*tiles_xSize,GetRandomFloor());
						}
						
						tiles.set((int)startPoint.x+(int)startPoint.y*tiles_xSize,GetRandomFloor());
						tiles.set((int)endPoint.x+(int)endPoint.y*tiles_xSize,GetRandomFloor());
						
						startPoint = endPoint;
					}
				}
			}
		}
		
		
		//set rooms
		dungeonBSP.ParseRooms_Begin();
		
		while (dungeonBSP.ParseRooms_Next())
		{
			room = dungeonBSP.parseRooms_room;
			
			if (room != null)
			{
				for (y = (int) room.y; y < (int)(room.y + room.height); y++)
				for (x = (int) room.x; x < (int)(room.x + room.width); x++)
				{
					tiles.set(x+y*tiles_xSize,1);
				}
			}
		}
		
		
		//select start room
		Rectangle startRoom = dungeonBSP.GetDeepestRoom(dungeonBSP.root.leftChild);
		
		tiles.set(((int)startRoom.x + (int)startRoom.width/2) + ((int)startRoom.y + (int)startRoom.height/2)*tiles_xSize, 12);
		
		
		//use wave algorithm to create map progress and find farthest point where will be exit
		class WaveElement
		{
			int waveIndex;
			int tileX, tileY;
		}
		
		int maxWaveIndex = 0;
		WaveElement maxWaveElement = null;

		Stack<WaveElement> stack = new Stack<WaveElement>();
		
		IntArray waves = waveMap;
		for (x = 0; x < tilesCount; x++)
		{
			if (tiles.get(x) != 0)
			waves.set(x,0);
			else
			waves.set(x,-1);
		}
		
		WaveElement wave = new WaveElement();
		wave.waveIndex = 1;
		wave.tileX = (int)startRoom.x + (int)(startRoom.width-1)/2;
		wave.tileY = (int)startRoom.y + (int)(startRoom.height-1)/2;
		
		waves.set((wave.tileX-1) + wave.tileY*tiles_xSize,1);
		
		stack.push(wave);

		while (stack.size() != 0)
		{
			wave = stack.pop();
			
			if (wave.waveIndex > maxWaveIndex)
			{
				maxWaveIndex = wave.waveIndex;
				maxWaveElement = wave;
			}
			
			if (wave.tileX > 0 && wave.tileX < tiles_xSize-1 &&
				wave.tileY > 0 && wave.tileY < tiles_ySize-1)
			{
				WaveElement nextWave = null;
				
				if (waves.get((wave.tileX-1) + wave.tileY*tiles_xSize)==0)
				{
					waves.set((wave.tileX-1) + wave.tileY*tiles_xSize,wave.waveIndex+1);
					nextWave = new WaveElement();
					nextWave.waveIndex = wave.waveIndex+1;
					nextWave.tileX = wave.tileX-1;
					nextWave.tileY = wave.tileY;
					stack.push(nextWave);
				}
				
				if (waves.get((wave.tileX+1) + wave.tileY*tiles_xSize)==0)
				{
					waves.set((wave.tileX+1) + wave.tileY*tiles_xSize,wave.waveIndex+1);
					nextWave = new WaveElement();
					nextWave.waveIndex = wave.waveIndex+1;
					nextWave.tileX = wave.tileX+1;
					nextWave.tileY = wave.tileY;
					stack.push(nextWave);
				}
				
				if (waves.get(wave.tileX + (wave.tileY+1)*tiles_xSize)==0)
				{
					waves.set(wave.tileX + (wave.tileY+1)*tiles_xSize,wave.waveIndex+1);
					nextWave = new WaveElement();
					nextWave.waveIndex = wave.waveIndex+1;
					nextWave.tileX = wave.tileX;
					nextWave.tileY = wave.tileY+1;
					stack.push(nextWave);
				}
				
				if (waves.get(wave.tileX + (wave.tileY-1)*tiles_xSize)==0)
				{
					waves.set(wave.tileX + (wave.tileY-1)*tiles_xSize,wave.waveIndex+1);
					nextWave = new WaveElement();
					nextWave.waveIndex = wave.waveIndex+1;
					nextWave.tileX = wave.tileX;
					nextWave.tileY = wave.tileY-1;
					stack.push(nextWave);
				}
			}
		}
		
		Rectangle finalRoom = dungeonBSP.GetRoomThatContains(maxWaveElement.tileX, maxWaveElement.tileY);
		tiles.set(((int)finalRoom.x + (int)(finalRoom.width-1) / 2) + ((int)finalRoom.y + (int)(finalRoom.height-1) / 2)*tiles_xSize, 12);
		
		stack.clear();
		
		//place items and creatures
		PlaceItemsAndCreatures(startRoom, finalRoom);

		System.gc();
	}
	
	private void PlaceItemsAndCreatures(Rectangle startRoom, Rectangle finalRoom)
	{
		int wayStartX = (int)startRoom.x + (int)startRoom.width/2;
		int wayStartY = (int)startRoom.y + (int)startRoom.height/2;
		
		int wayEndX = (int)finalRoom.x + (int)finalRoom.width/2;
		int wayEndY = (int)finalRoom.y + (int)finalRoom.height/2;
		
		int wayX, wayY;
		
		float wayLength = (float)waveMap.get(wayEndX + wayEndY*tiles_xSize);
		float wayProgress = 100f;
		//moving from end to start (because of wave enumeration)
		wayX = wayEndX; wayY = wayEndY;
		int waveN, waveS, waveE, waveW, waveC;

		for (;;)
		{
			waveC = waveMap.get(wayX + wayY*tiles_xSize);
			if (waveC == 1) break; //end of moving
	
			waveN = waveMap.get(wayX + (wayY+1)*tiles_xSize);
			waveS = waveMap.get(wayX + (wayY-1)*tiles_xSize);
			waveE = waveMap.get((wayX+1) + wayY*tiles_xSize);
			waveW = waveMap.get((wayX-1) + wayY*tiles_xSize);
			
			wayProgress = ((float)waveC / wayLength)*100;
			
			if (waveS == -1 && waveN == -1)
			{
				//horizontal hall
			}
			else
			if (waveE == -1 && waveW == -1)
			{
				//vertical hall
			} else
			{
				//inside room or cross-roads
			}
			
			if (waveN > 1 && waveN < waveC)
			{
				wayY = (wayY+1);
			} else if (waveS > 1 && waveS < waveC)
			{
				wayY = (wayY-1);
			} else if (waveE > 1 && waveE < waveC)
			{
				wayX = (wayX+1);
			} else if (waveW > 1 && waveW < waveC)
			{
				wayX = (wayX-1);
			} else break; //halt! can't move anymore (unusual situation)
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
			
			tileWorldRect.set(x*80, y*80, 80, 80);
			
			if (view.overlaps(tileWorldRect))
			//if (view.contains(tileWorldRect.x + tileWorldRect.width/2,tileWorldRect.y+tileWorldRect.height /2))
			{
				tileRegion.setRegion(tileRegionX+tileIndexX*40, tileRegionY+tileIndexY*40, 40, 40);
				batch.draw(tileRegion, tileWorldRect.x, tileWorldRect.y);
				batch.draw(tileRegion, tileWorldRect.x+40, tileWorldRect.y);
				batch.draw(tileRegion, tileWorldRect.x+40, tileWorldRect.y+40);
				batch.draw(tileRegion, tileWorldRect.x, tileWorldRect.y+40);
			}
		}
	}
}
