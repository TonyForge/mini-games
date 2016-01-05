package treasurehunter;

import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class DungeonBSP {
	
	public int MIN_LEAF_SIZE = 6;
	public int MAX_LEAF_SIZE = 20;
	public float MAX_HALL_SECTION_LENGTH = 2f;
	
	class Leaf
	{
		public int x, y, w, h;
		public Leaf leftChild;
		public Leaf rightChild;
		
		public Rectangle room;
		public LinkedList<Vector2> hallPath;
		
		public Leaf(int x, int y, int w, int h)
		{
			this.x = x; this.y = y; this.w = w; this.h = h;
			leftChild = null; rightChild = null;
			room = null;
			hallPath = null;
		}
		
		public boolean Split()
		{
			if (leftChild != null || rightChild != null) return false;
			
			boolean splitH = Math.random() > 0.5f;
			
			if (w > h && (w / h) >= 1.25f)
				splitH = false;
			else 
			if (h > w && (h / w) >= 1.25f)
	        	splitH = true;
			
			int max = (splitH ? h : w) - MIN_LEAF_SIZE;
			
			if (max <= MIN_LEAF_SIZE)
				return false;
			
			int split = MIN_LEAF_SIZE + (int)((float)(max-MIN_LEAF_SIZE) * Math.random());
			
			if (splitH)
			{
				leftChild = new Leaf(x, y, w, split);
	            rightChild = new Leaf(x, y + split, w, h - split);
	        }
			else
	        {
	        	leftChild = new Leaf(x, y, split, h);
	        	rightChild = new Leaf(x + split, y, w - split, h);
	        }
			
	        return true;
		}
		
		public void createRooms()
		{
			if (leftChild != null || rightChild != null)
			{
				if (leftChild != null)
				{
					leftChild.createRooms();
				}
				if (rightChild != null)
				{
					rightChild.createRooms();
				}
				
				if (leftChild != null && rightChild != null)
				{
					createHall(leftChild.getRoom(),rightChild.getRoom());
				}
			}
			else
			{
				float roomSizeX, roomSizeY;
				float roomPosX, roomPosY;

				// the room can be between 3 x 3 tiles to the size of the leaf - 2.
				roomSizeX = 3f + ((float)w - 2f - 3f)*(float)Math.random();
				roomSizeY = 3f + ((float)h - 2f - 3f)*(float)Math.random();
				// place the room within the Leaf, but don't put it right 
				// against the side of the Leaf (that would merge rooms together)
				roomPosX = 1f + ((float)w - roomSizeX - 1f - 1f)*(float)Math.random();
				roomPosY = 1f + ((float)h - roomSizeY - 1f - 1f)*(float)Math.random();

				room = new Rectangle(x + roomPosX, y + roomPosY, roomSizeX, roomSizeY);
			}
		}
		
		public Rectangle getRoom()
		{
			if (room != null)
				return room;
			else
			{
				Rectangle lRoom = null;
				Rectangle rRoom = null;
				if (leftChild != null)
				{
					lRoom = leftChild.getRoom();
				}
				if (rightChild != null)
				{
					rRoom = rightChild.getRoom();
				}
				if (lRoom == null && rRoom == null)
					return null;
				else if (rRoom == null)
					return lRoom;
				else if (lRoom == null)
					return rRoom;
				else if (Math.random() > 0.5f)
					return lRoom;
				else
					return rRoom;
			}
		}
		
		public void createHall(Rectangle l, Rectangle r)
		{
			hallPath = new LinkedList<Vector2>();
			
			float beginX, beginY, endX, endY;
			
			beginX = l.x + l.width*(float)Math.random();
			beginY = l.y + l.height*(float)Math.random();
			
			endX = r.x + r.width*(float)Math.random();
			endY = r.y + r.height*(float)Math.random();
			
			Vector2 v = new Vector2(endX - beginX, endY - beginY);
			v.setLength(1);
			
			Vector2 pathPoint = null;
			
			float len = (float) Math.sqrt((endX - beginX)*(endX - beginX) + (endY - beginY)*(endY - beginY));
			int lenSteps = (int) (len / MAX_HALL_SECTION_LENGTH);
			
			hallPath.push(new Vector2(beginX, beginY));
			
			hallPath.push(new Vector2(endX, beginY));
			
			/*for (int i = 0; i < lenSteps; i++)
			{
				if (pathPoint == null)
				pathPoint = new Vector2(beginX, beginY);
				else
				pathPoint = new Vector2(pathPoint);
				
				pathPoint.x += v.x * MAX_HALL_SECTION_LENGTH;
				hallPath.push(pathPoint);
				
				pathPoint = new Vector2(pathPoint);
				pathPoint.y += v.y * MAX_HALL_SECTION_LENGTH;
				hallPath.push(pathPoint);
			}*/
			
			hallPath.push(new Vector2(endX, endY));
		}
	}
	
	Leaf root;
	LinkedList<Leaf> leafsCollection;
	
	public DungeonBSP() {
		leafsCollection = new LinkedList<Leaf>();
		root = null;
	}
	
	public void GenerateDungeon(int left, int top, int width, int height)
	{
		leafsCollection.clear();
		root = new Leaf(left, top, width, height);
		leafsCollection.push(root);
		
		boolean didSplit = true;
		
		while (didSplit)
		{
			didSplit = false;

			for (int i = 0; i < leafsCollection.size(); i++)
			{
				Leaf leaf = leafsCollection.get(i);
				
				if (leaf.leftChild == null && leaf.rightChild == null)
				{
					if (leaf.w > MAX_LEAF_SIZE || leaf.h > MAX_LEAF_SIZE || Math.random() > 0.75f)
					{
						if (leaf.Split())
						{
							leafsCollection.push(leaf.leftChild);
							leafsCollection.push(leaf.rightChild);
							didSplit = true;
						}
					}
				}
			}
		}
		
		root.createRooms();
	}
	
	public Iterator<Leaf> parseRooms_iterator;
	public Rectangle parseRooms_room;
	LinkedList<Vector2> parseRooms_halls;
	
	public void ParseRooms_Begin()
	{
		parseRooms_iterator = leafsCollection.iterator();
		parseRooms_room = null;
		parseRooms_halls = null;
	}
	
	public boolean ParseRooms_Next()
	{
		Leaf tmp = null;
		if (parseRooms_iterator.hasNext())
			tmp = parseRooms_iterator.next();
		
		if (tmp != null) {
			parseRooms_room = tmp.room;
			parseRooms_halls = tmp.hallPath;
			return true;
		}
		
		return false;
	}

}
