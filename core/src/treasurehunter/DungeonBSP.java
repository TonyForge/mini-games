package treasurehunter;

import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class DungeonBSP {
	
	public int MIN_LEAF_SIZE = 6;
	public int MAX_LEAF_SIZE = 20;
	//public float MAX_HALL_SECTION_LENGTH = 2f;
	
	class Leaf
	{
		public int x, y, w, h, depth;
		public Leaf leftChild;
		public Leaf rightChild;
		
		public Rectangle room;
		public LinkedList<Vector2> hallPath;
		
		public Leaf(int x, int y, int w, int h, int depth)
		{
			this.x = x; this.y = y; this.w = w; this.h = h; this.depth = depth;
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
				leftChild = new Leaf(x, y, w, split, depth + 1);
	            rightChild = new Leaf(x, y + split, w, h - split, depth + 1);
	        }
			else
	        {
	        	leftChild = new Leaf(x, y, split, h, depth + 1);
	        	rightChild = new Leaf(x + split, y, w - split, h, depth + 1);
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
					LinkedList<Rectangle> lRooms = leftChild.getRoomsList();
					LinkedList<Rectangle> rRooms = rightChild.getRoomsList();
					LinkedList<Rectangle> twoClosestRooms = new LinkedList<Rectangle>();
					
					findTwoClosestRooms(lRooms, rRooms, twoClosestRooms);
					
					createHall(twoClosestRooms.get(0),twoClosestRooms.get(1));
					
					lRooms.clear();
					rRooms.clear();
					twoClosestRooms.clear();
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
		
		public void findTwoClosestRooms(LinkedList<Rectangle> leftRooms, LinkedList<Rectangle> rightRooms, LinkedList<Rectangle> out)
		{
			Iterator<Rectangle> itLeft = leftRooms.iterator();
			Iterator<Rectangle> itRight = null;
			Rectangle leftRoom, rightRoom;
			
			Rectangle minPairA = null, minPairB = null;
			float minDistance = -1;
			
			float cx1,cy1,cx2,cy2,dist;
			
			while (itLeft.hasNext())
			{
				leftRoom = itLeft.next();
				
				cx1 = leftRoom.x + leftRoom.width/2f;
				cy1 = leftRoom.y + leftRoom.height/2f;
				
				itRight = rightRooms.iterator();
				while (itRight.hasNext())
				{
					rightRoom = itRight.next();

					cx2 = rightRoom.x + rightRoom.width/2f;
					cy2 = rightRoom.y + rightRoom.height/2f;
					
					if (minDistance == -1)
					{
						minDistance = (cx2 - cx1)*(cx2 - cx1) + (cy2 - cy1)*(cy2 - cy1);
						minPairA = leftRoom;
						minPairB = rightRoom;
					}
					else
					{
						dist = (cx2 - cx1)*(cx2 - cx1) + (cy2 - cy1)*(cy2 - cy1);
						if (dist < minDistance)
						{
							minDistance = dist;
							minPairA = leftRoom;
							minPairB = rightRoom;
						}
					}
				}
			}
			
			out.push(minPairA);
			out.push(minPairB);
		}
		
		public LinkedList<Rectangle> getRoomsList()
		{
			LinkedList<Rectangle> result = new LinkedList<Rectangle>();
			collectRooms(result);
			
			return result;
		}
		
		public void collectRooms(LinkedList<Rectangle> collection)
		{
			if (room != null) collection.push(room);
			if (leftChild != null) leftChild.collectRooms(collection);
			if (rightChild != null) rightChild.collectRooms(collection);
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
			
			beginX = l.x + (l.width-1)*(float)Math.random();
			beginY = l.y + (l.height-1)*(float)Math.random();
			
			endX = r.x + (r.width-1)*(float)Math.random();
			endY = r.y + (r.height-1)*(float)Math.random();
			
			//Vector2 v = new Vector2(endX - beginX, endY - beginY);
			//v.setLength(1);
			
			//Vector2 pathPoint = null;
			
			//float len = (float) Math.sqrt((endX - beginX)*(endX - beginX) + (endY - beginY)*(endY - beginY));
			//int lenSteps = (int) (len / MAX_HALL_SECTION_LENGTH);
			
			hallPath.push(new Vector2(beginX, beginY));
			hallPath.push(new Vector2(endX, beginY));
			hallPath.push(new Vector2(endX, endY));
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
			
			
		}
		
		
		private void FindDeepest()
		{
			if (leftChild != null) leftChild.FindDeepest();
			if (rightChild != null) rightChild.FindDeepest();
			
			if (room != null && depth > currentDeepest)
			{
				currentDeepest = depth;
				currentDeepestRectangle = room;
			}
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
		root = new Leaf(left, top, width, height, 0);
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
	
	public Rectangle GetRoomThatContains(int tileX, int tileY)
	{
		Iterator<Leaf> it = leafsCollection.iterator();
		Rectangle result = null;
		while (it.hasNext())
		{
			result = it.next().room;
			if (result != null && result.contains(tileX, tileY)) 
				return result;
		}
		return null;
	}
	
	public int currentDeepest;
	public Rectangle currentDeepestRectangle;
	
	public Rectangle GetDeepestRoom(Leaf subTreeRoot)
	{
		currentDeepest = subTreeRoot.depth;
		currentDeepestRectangle = subTreeRoot.room;
		subTreeRoot.FindDeepest();
		
		return currentDeepestRectangle;
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
