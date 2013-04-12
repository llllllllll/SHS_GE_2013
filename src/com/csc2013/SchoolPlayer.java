package com.csc2013;

import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.MoveType;

import java.text.NumberFormat;

import java.util.ArrayList;

/**
 * 
 * @brief SchoolPlayer was programmed entirely by white cis males
 * @author Jack Pugmire
 * 
 */
public class SchoolPlayer {

	// These constants are the priorities of the different types of tiles.
	// Lower number means higher priority.
	private static final int EXIT = 0;
	private static final int KEY = 1;
	private static final int NEW_TILE = 2;
	private static final int TRAVERSED_TILE = 3;
	private static final int DEAD_END = 4;
	private static final int BLOCKED = 5;
	private static final int MAX_PRIORITY = 6;
	
	// The hard limit on the number of Tiles a path can contain.
	private static final int MAX_PATH_LENGTH = 30;

	/**
	 * The Tile class is only used for deciding on a path and storing
	 * coordinates
	 *
	 */
	private static class Tile {
		int x, y;

		public Tile(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Tile)
				return ((Tile) obj).x == this.x && ((Tile) obj).y == this.y;
			return super.equals(obj);
		}

		@Override
		public String toString() {
			return "[ " + x + ", " + y + " ]";
		}
	}

	// Stores the map.
	// It is ordered so that the row number is the y value (bottom to top), and
	// the column number is the x value (left to right).
	private ArrayList<ArrayList<BoxType>> map;

	// The coordinates of tiles that have been traversed.
	private ArrayList<Tile> traversed_tiles;

	// Offset of the origin from the bottom left corner of the array.
	// Effectively, we can plug these coordinates into map and get the origin
	private int offset_x = 0;
	private int offset_y = 0;

	// The dimensions of the map.
	private int map_width = 0;
	private int map_height = 0;

	// The player's location relative to the origin.
	private int player_x = 0;
	private int player_y = 0;

	// The number of keys we have.
	private int key_count = 0;
	
	/**
	 * Constructor.
	 * 
	 * @throws SlickException
	 */
	public SchoolPlayer() throws SlickException {
		map = new ArrayList<ArrayList<BoxType>>();
		traversed_tiles = new ArrayList<Tile>();
	}

	/**
	 * @brief Add a tile to the map.
	 * @param x
	 *            The x-coordinate of the tile to add.
	 * @param y
	 *            The y-coordinate of the tile to add.
	 * @param t
	 *            The type of the tile.
	 */
	private void add_tile(int x, int y, BoxType t) {
		// Ensure that the array is big enough.
		if (y < 0) {
			while (y < -offset_y) {
				// Make it easier to track down tricky indexing bugs
				assert map.size() == map_height;

				// Add rows at the beginning until the array is big enough to
				// hold y.
				map.add(0, new ArrayList<BoxType>());
				// We will fill up the new row with null tiles for now.
				for (int m = 0; m < map_width; ++m) {
					map.get(0).add(null);
				}

				++map_height;

				// The offset has to be increased because we have effectively
				// moved the origin up in the map.
				++offset_y;
			}
		} else {
			while (y + offset_y >= map_height) {
				assert map.size() == map_height;

				// Add rows until the array is big enough to hold y
				map.add(new ArrayList<BoxType>());
				// We will fill up the new row with null tiles for now.
				for (int m = 0; m < map_width; ++m) {
					map.get(map.size() - 1).add(null);
				}

				++map_height;
			}
		}
		if (x < 0) {
			while (x < -offset_x) {
				// Add an empty box at the beginning of each row
				for (ArrayList<BoxType> b : map) {
					assert b.size() == map_width;

					b.add(0, null);
				}

				++map_width;

				++offset_x;
			}
		} else {
			while (x + offset_x >= map_width) {
				// Add an empty box at the beginning of each row
				for (ArrayList<BoxType> b : map) {
					assert b.size() == map_width;

					b.add(null);
				}

				++map_width;
			}
		}

		// Finally, add our tile.
		(map.get(y + offset_y)).set(x + offset_x, t);
	}
	

	/**
	 * Check the type of a specified tile
	 * @param x The x coordinate of the tile
	 * @param y The y coordinate of the tile
	 * @return The type of the tile
	 */
	private BoxType get_box_type(int x, int y) {
		if (x + offset_x < 0 || x + offset_x >= map_width || y + offset_y < 0
				|| y + offset_y >= map_height)
			return null;

		return (map.get(y + offset_y)).get(x + offset_x);
	}

	/**
	 * Query whether keys are available or not.
	 * @return true if there are one or more keys in the player's posession
	 */
	private boolean has_key() {
		return key_count != 0;
	}

	/**
	 * Build the map with more vision.
	 * @param vision The PlayerVision object to use.
	 */
	private void update_map(final PlayerVision vision) {
		for (int i = 0; i < vision.North.length; ++i) {
			this.add_tile(this.player_x, this.player_y + i + 2,
					vision.North[i].North);
			this.add_tile(this.player_x, this.player_y + i,
					vision.North[i].South);
			this.add_tile(this.player_x + 1, this.player_y + i + 1,
					vision.North[i].East);
			this.add_tile(this.player_x - 1, this.player_y + i + 1,
					vision.North[i].West);
		}

		for (int i = 0; i < vision.South.length; ++i) {
			this.add_tile(this.player_x, this.player_y - i,
					vision.South[i].North);
			this.add_tile(this.player_x, this.player_y - i - 2,
					vision.South[i].South);
			this.add_tile(this.player_x + 1, this.player_y - i - 1,
					vision.South[i].East);
			this.add_tile(this.player_x - 1, this.player_y - i - 1,
					vision.South[i].West);
		}

		for (int i = 0; i < vision.East.length; ++i) {
			this.add_tile(this.player_x + i + 1, this.player_y + 1,
					vision.East[i].North);
			this.add_tile(this.player_x + i + 1, this.player_y - 1,
					vision.East[i].South);
			this.add_tile(this.player_x + i + 2, this.player_y,
					vision.East[i].East);
			this.add_tile(this.player_x + i, this.player_y, vision.East[i].West);
		}

		for (int i = 0; i < vision.West.length; ++i) {
			this.add_tile(this.player_x - i - 1, this.player_y + 1,
					vision.West[i].North);
			this.add_tile(this.player_x - i - 1, this.player_y - 1,
					vision.West[i].South);
			this.add_tile(this.player_x - i, this.player_y, vision.West[i].East);
			this.add_tile(this.player_x - i - 2, this.player_y,
					vision.West[i].West);
		}

		// If we're in a corner, we may get some null values without this.
		this.add_tile(this.player_x, this.player_y + 1,
				vision.CurrentPoint.North);
		this.add_tile(this.player_x, this.player_y - 1,
				vision.CurrentPoint.South);
		this.add_tile(this.player_x + 1, this.player_y,
				vision.CurrentPoint.East);
		this.add_tile(this.player_x - 1, this.player_y,
				vision.CurrentPoint.West);

		// Finally, add our current point to the list of traversed tiles.
		Tile current = new Tile(player_x, player_y);
		boolean add_traversed = true;
		for (Tile tmp : traversed_tiles) {
			if (current.equals(tmp)) {
				add_traversed = false;
			}
		}
		if (add_traversed)
			traversed_tiles.add(current);
	}

	/**
	 * Prints the map in text form. Useful for debugging.
	 */
	private void print_map() {
		NumberFormat tmp = NumberFormat.getInstance();
		tmp.setMinimumIntegerDigits(3);
		System.out.println("Map: " + map_width + "x" + map_height);
		System.out.println("Offset: " + offset_x + ", " + offset_y);
		for (int y = map_height - 1; y >= 0; --y) {
			System.out.print(y >= offset_y ? " " : "");
			System.out.print(tmp.format(y - offset_y) + ": ");
			for (int x = 0; x < map_width; ++x) {
				if (map.get(y).get(x) != null) {
					switch (map.get(y).get(x)) {
					case Blocked:
						System.out.print("X ");
						break;
					case Open:
						System.out.print("- ");
						break;
					default:
						System.out.print("O");
						break;
					}
				} else
					System.out.print("? ");
			}
			System.out.println();
		}
		System.out.println("\n");
	}

	/**
	 * Get a list of adjacent tiles.
	 * @param t The tile to look for adjacent tiles against.
	 * @return A list of adjacent tiles.
	 */
	private ArrayList<Tile> get_adjacent_open_tiles(Tile t) {
		ArrayList<Tile> adjacent = new ArrayList<Tile>();

		if (get_box_type(t.x, t.y + 1) == BoxType.Open
				|| get_box_type(t.x, t.y + 1) == BoxType.Key
				|| get_box_type(t.x, t.y + 1) == BoxType.Exit
				|| get_box_type(t.x, t.y + 1) == BoxType.Door && this.has_key()) {
			adjacent.add(new Tile(t.x, t.y + 1));
		}
		if (get_box_type(t.x, t.y - 1) == BoxType.Open
				|| get_box_type(t.x, t.y - 1) == BoxType.Key
				|| get_box_type(t.x, t.y - 1) == BoxType.Exit
				|| get_box_type(t.x, t.y - 1) == BoxType.Door && this.has_key()) {
			adjacent.add(new Tile(t.x, t.y - 1));
		}
		if (get_box_type(t.x + 1, t.y) == BoxType.Open
				|| get_box_type(t.x + 1, t.y) == BoxType.Key
				|| get_box_type(t.x + 1, t.y) == BoxType.Exit
				|| get_box_type(t.x + 1, t.y) == BoxType.Door && this.has_key()) {
			adjacent.add(new Tile(t.x + 1, t.y));
		}
		if (get_box_type(t.x - 1, t.y) == BoxType.Open
				|| get_box_type(t.x - 1, t.y) == BoxType.Key
				|| get_box_type(t.x - 1, t.y) == BoxType.Exit
				|| get_box_type(t.x - 1, t.y) == BoxType.Door && this.has_key()) {
			adjacent.add(new Tile(t.x - 1, t.y));
		}

		return adjacent;
	}

	/**
	 * Check whether a tile has any new vision to provide.
	 * @param t the tile to check
	 * @return true if this tile can offer more vision, false otherwise
	 */
	private boolean is_explored(Tile t) {
		return get_box_type(t.x, t.y + 1) == null
				|| get_box_type(t.x, t.y - 1) == null
				|| get_box_type(t.x + 1, t.y) == null
				|| get_box_type(t.x - 1, t.y) == null;
	}

	/**
	 * Check the priority of a tile.
	 * @param target The tile to check
	 * @return The priority this tile has (lower is better)
	 */
	private int determine_priority(Tile target) {
		int ret_val;

		if (get_box_type(target.x, target.y) == null)
			return SchoolPlayer.MAX_PRIORITY;

		switch (get_box_type(target.x, target.y)) {
		case Exit:
			ret_val = SchoolPlayer.EXIT;
			break;
		case Key:
			ret_val = SchoolPlayer.KEY;
			break;
		case Door:
			if (this.has_key())
				ret_val = SchoolPlayer.NEW_TILE;
			else
				ret_val = SchoolPlayer.BLOCKED;
		case Open:
			ret_val = SchoolPlayer.NEW_TILE;

			for (Tile tmp : this.traversed_tiles) {
				if (target.equals(tmp)) {
					ret_val = SchoolPlayer.TRAVERSED_TILE;
				}
			}

			if (is_explored(target)) {
				ret_val = SchoolPlayer.DEAD_END;
			}

			if ((get_adjacent_open_tiles(target).size() <= 1))
				ret_val = SchoolPlayer.DEAD_END;

			break;

		default:
			ret_val = SchoolPlayer.BLOCKED;
			break;
		}

		return ret_val;
	}
	
	/**
	 * Get a list of potential targets for path finding.
	 * @return A list of the best priority targets.
	 */
	private ArrayList<Tile> possible_targets() {
		ArrayList<Tile> possible_targets = null;

		int min_priority = SchoolPlayer.MAX_PRIORITY;

		Tile tmp;
		int curr_priority;
		for (int y = 0; y < map_height; ++y) {
			for (int x = 0; x < map_width; ++x) {
				tmp = new Tile(x - offset_x, y - offset_y);
				curr_priority = this.determine_priority(tmp);
				if (curr_priority < min_priority) {
					possible_targets = new ArrayList<Tile>();
					possible_targets.add(tmp);
					min_priority = curr_priority;
				} else if (curr_priority == min_priority
						&& min_priority != SchoolPlayer.MAX_PRIORITY) {
					possible_targets.add(tmp);
				}
			}
		}

		return possible_targets;
	}

	/**
	 * Recursively finds the shortest path between two tiles.
	 * @param src The starting tile.
	 * @param dest The destination tile.
	 * @return A list of Tiles describing the path to the target.
	 */
	private ArrayList<Tile> path_to_target(Tile src, Tile dest) {
		ArrayList<Tile> initial = new ArrayList<Tile>();
		return path_to_target_recurs(src, dest, initial);
	}

	/**
	 * The recursive method of path_to_target
	 * @param src The starting tile
	 * @param dest The destination tile
	 * @param traversed A list of tiles that are already in the path
	 * @return A path from src to dest.
	 */
	private ArrayList<Tile> path_to_target_recurs(Tile src, Tile dest,
			ArrayList<Tile> traversed) {
		ArrayList<Tile> new_traversed = (ArrayList) traversed.clone();
		new_traversed.add(src);

		if (src.equals(dest)) {
			return new_traversed;
		}

		ArrayList<Tile> adjacent_paths = get_adjacent_open_tiles(src);

		// First remove squares that have already been traversed
		for (int i = 0; i < adjacent_paths.size(); ++i) {
			for (Tile tmp : new_traversed) {
				if (adjacent_paths.get(i).equals(tmp)) {
					adjacent_paths.remove(i);
					--i;
					break;
				}
			}
		}

		// We can leave early if we're already adjacent to the lucky square
		for (Tile tmp : adjacent_paths) {
			if (tmp.equals(dest))
				return path_to_target_recurs(dest, dest, new_traversed);
		}

		if (adjacent_paths.size() == 0) {
			return null;
		}

		ArrayList<Tile> options[] = new ArrayList[adjacent_paths.size()];
		for (int i = 0; i < adjacent_paths.size(); ++i) {
			options[i] = path_to_target_recurs(adjacent_paths.get(i), dest,
					new_traversed);
		}

		int best_index = -1;
		int shortest_path = Integer.MAX_VALUE;
		for (int i = 0; i < options.length; ++i) {
			if (options[i] != null && options[i].size() < shortest_path) {
				best_index = i;
				shortest_path = options[i].size();
			}
		}

		if (best_index == -1) {
			return null;
		}

		return options[best_index];
	}

	/**
	 * Compares possible targets and selects the best one.
	 * @param possible A list of possible targets.
	 * @return A path to the best target.
	 */
	private ArrayList<Tile> get_path_to_best_target(ArrayList<Tile> possible) {
		ArrayList<ArrayList<Tile>> all_targets = new ArrayList<ArrayList<Tile>>();
		int closest = Integer.MAX_VALUE;
		for (Tile t : possible) {
			all_targets.add(path_to_target(new Tile(player_x, player_y), t));
			if (all_targets.get(all_targets.size() - 1) == null) {
				all_targets.remove(all_targets.size() - 1);
			} else if (all_targets.get(all_targets.size() - 1).size() < closest)
				closest = all_targets.get(all_targets.size() - 1).size();
		}

		ArrayList<ArrayList<Tile>> top_targets = new ArrayList<ArrayList<Tile>>();
		for (ArrayList<Tile> p : all_targets) {
			if (p.size() == closest)
				top_targets.add(p);
		}

		if (top_targets.size() == 1) {
			return top_targets.get(0);
		}

		return top_targets.get(0);
	}
	
	/**
	 * Gets the first action in a path of tiles.
	 * @param path The path for which to find the first action
	 * @return The first action necessary to follow the path
	 */
	private Action get_first_action(ArrayList<Tile> path) {

		for (Tile t : get_adjacent_open_tiles(path.get(0)))
			if (get_box_type(t.x, t.y) == BoxType.Door && has_key())
				return Action.Use;
		if (get_box_type(player_x, player_y) == BoxType.Key)
			return Action.Pickup;

		if (path.get(1).x == player_x + 1) {
			return Action.East;
		} else if (path.get(1).x == player_x - 1) {
			return Action.West;
		}

		if (path.get(1).y == player_y + 1) {
			return Action.North;
		} else if (path.get(1).y == player_y - 1) {
			return Action.South;
		}

		return null;
	}

	/**
	 * Update data based on the action that was selected for this turn.
	 * @param d The action that was selected this turn.
	 */
	private void process_decision(Action d) {
		if (d == null)
			return;
		switch (d) {
		case North:
			++player_y;
			break;
		case South:
			--player_y;
			break;
		case East:
			++player_x;
			break;
		case West:
			--player_x;
			break;

		default:
			break;
		}
	}

	/**
	 * @brief Get the next move that that has been decided on.
	 * @param vision
	 *            The current field of vision for the player.
	 * @param keyCount
	 *            The number of keys that the player is holding.
	 * @param lastAction
	 *            The Action that the player previously took.
	 * @return The next move to make.
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount,
			final boolean lastAction) {
		Action decision = null;

		this.key_count = keyCount;
		update_map(vision);

		ArrayList<Tile> poss = possible_targets();
		
		ArrayList<Tile> path = get_path_to_best_target(poss);
		decision = get_first_action(path);

		process_decision(decision);
		
		return decision;
	}
}