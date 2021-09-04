package io.github.lasteamlab.mcpythonmod;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import io.github.lasteamlab.mcpythonmod.cmd.CmdEntity;
import io.github.lasteamlab.mcpythonmod.cmd.CmdEvent;
import io.github.lasteamlab.mcpythonmod.cmd.CmdPlayer;
import io.github.lasteamlab.mcpythonmod.cmd.CmdWorld;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Arrays; 
import java.util.Iterator;


public class RemoteSession {

    private final LocationType locationType;

    public Location origin;

    private Socket socket;

    private BufferedReader in;

    private BufferedWriter out;

    private Thread inThread;

    private Thread outThread;

    private ArrayDeque<String> inQueue = new ArrayDeque<String>();

    private ArrayDeque<String> outQueue = new ArrayDeque<String>();

    public boolean running = true;

    public boolean pendingRemoval = false;

    public MCPythonMod plugin;

    public ArrayDeque<PlayerInteractEvent> interactEventQueue = new ArrayDeque<PlayerInteractEvent>();

    public ArrayDeque<AsyncPlayerChatEvent> chatPostedQueue = new ArrayDeque<AsyncPlayerChatEvent>();
    
    protected ArrayDeque<ProjectileHitEvent> projectileHitQueue = new ArrayDeque<ProjectileHitEvent>();

    private int maxCommandsPerTick = 9000;

    private boolean closed = false;

    private Player attachedPlayer = null;

    private CmdEntity cmdEntity;
    private CmdEvent cmdEvent;
    private CmdPlayer cmdPlayer;
    private CmdWorld cmdWorld;

    public RemoteSession(MCPythonMod plugin, Socket socket) throws IOException {
        this.socket = socket;
        this.plugin = plugin;
        this.locationType = plugin.getLocationType();
        init();
        createCmdObject();
    }

    public void init() throws IOException {
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setTrafficClass(0x10);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
        startThreads();
        plugin.getLogger().info("Opened connection to" + socket.getRemoteSocketAddress() + ".");
    }

    public void createCmdObject(){
        cmdEntity = new CmdEntity(this);
        cmdEvent = new CmdEvent(this);
        cmdPlayer = new CmdPlayer(this);
        cmdWorld = new CmdWorld(this);

    }

    protected void startThreads() {
        inThread = new Thread(new InputThread());
        inThread.start();
        outThread = new Thread(new OutputThread());
        outThread.start();
    }


    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Socket getSocket() {
        return socket;
    }

    public void queuePlayerInteractEvent(PlayerInteractEvent event) {
        //plugin.getLogger().info(event.toString());
        interactEventQueue.add(event);
    }

    public void queueChatPostedEvent(AsyncPlayerChatEvent event) {
        //plugin.getLogger().info(event.toString());
        chatPostedQueue.add(event);
    }

    public void queueProjectileHitEvent(ProjectileHitEvent event) {
    	//plugin.getLogger().info(event.toString());

    	if (event.getEntityType() == EntityType.ARROW) {
    		Arrow arrow = (Arrow) event.getEntity();
    		if (arrow.getShooter() instanceof Player) {
    			projectileHitQueue.add(event);
    		}
    	}
    }


    	/**
     * called from the server main thread
     */
    public void tick() {
        if (origin == null) {
            switch (locationType) {
                case ABSOLUTE:
                    this.origin = new Location(plugin.getServer().getWorlds().get(0), 0, 0, 0);
                    break;
                case RELATIVE:
                    this.origin = plugin.getServer().getWorlds().get(0).getSpawnLocation();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown location type " + locationType);
            }
        }
        int processedCount = 0;
        String message;
        while ((message = inQueue.poll()) != null) {
            handleLine(message);
            processedCount++;
            if (processedCount >= maxCommandsPerTick) {
                plugin.getLogger().warning("Over " + maxCommandsPerTick +
                        " commands were queued - deferring " + inQueue.size() + " to next tick");
                break;
            }
        }

        if (!running && inQueue.size() <= 0) {
            pendingRemoval = true;
        }
    }

    protected void handleLine(String line) {
        //System.out.println(line);
        String methodName = line.substring(0, line.indexOf("("));
        //split string into args, handles , inside " i.e. ","
        String[] args = line.substring(line.indexOf("(") + 1, line.length() - 1).split(",");
        //System.out.println(methodName + ":" + Arrays.toString(args));
        handleCommand(methodName, args);
    }

    protected void handleCommand(String c, String[] args) {

        try {
            // get the server
            Server server = plugin.getServer();

            // get the world
            World world = origin.getWorld();

            // 分割命令
            String[] cmd = c.split("[.]", 2);

            if (cmd[0].equals("player")) {
                cmdPlayer.execute(cmd[1], args);

            } else if (cmd[0].equals("multiplayer")) {
                String id = args[0];
                String[] newargs = Arrays.copyOfRange(args, 1, args.length);
            	cmdPlayer.execute(cmd[1], newargs, id);

            } else if (cmd[0].equals("entity")) {
                cmdEntity.execute(cmd[1], args);

            } else if (cmd[0].equals("world")) {
//                new CmdWorld(this).execute(world, cmd[1], args);
                cmdWorld.execute(world, cmd[1], args);

            } else if (cmd[0].equals("events")) {
                cmdEvent.execute(cmd[1], args);

                // chat.post
            } else if (c.equals("chat.post")) {
                //create chat message from args as it was split by ,
                String chatMessage = "";
                int count;
                for (count = 0; count < args.length; count++) {
                    chatMessage = chatMessage + args[count] + " ";
                }
                chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                server.broadcastMessage(chatMessage);

                // not a command which is supported
            } else {
                plugin.getLogger().warning(c + " is not supported.");
                send("Fail," + c + " is not supported.");
            }
        } catch (Exception e) {

            plugin.getLogger().warning("Error occured handling command");
            e.printStackTrace();
            send("Fail,Please check out minecraft server console");

        }
    }

    // updates a block
    // gets the current player
    public Player getCurrentPlayer() {
        if (!serverHasPlayer()) {
            send("Fail,There are no players in the server.");
            return null;
        }
        Player player = attachedPlayer;
        // if the player hasnt already been retreived for this session, go and get it.
        if (player == null) {
            player = plugin.getHostPlayer();
            attachedPlayer = player;
        }
        return player;
    }

	public Player getCurrentPlayer(String name) {
		// if a named player is returned use that
		Player player = plugin.getPlayer(name);
		// otherwise if there is an attached player for this session use that
		if (player == null) {
			player = attachedPlayer;
			// otherwise go and get the host player and make that the attached player
			if (player == null) {
				player = plugin.getHostPlayer();
				attachedPlayer = player;
			}
		}
		return player;
	}

    private boolean serverHasPlayer() {
        return !Bukkit.getOnlinePlayers().isEmpty();
    }

    public Location parseRelativeBlockLocation(String xstr, String ystr, String zstr) {
        int x = (int) Double.parseDouble(xstr);
        int y = (int) Double.parseDouble(ystr);
        int z = (int) Double.parseDouble(zstr);
        return parseLocation(origin.getWorld(), x, y, z, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }

    public Location parseRelativeLocation(String xstr, String ystr, String zstr) {
        double x = Double.parseDouble(xstr);
        double y = Double.parseDouble(ystr);
        double z = Double.parseDouble(zstr);
        return parseLocation(origin.getWorld(), x, y, z, origin.getX(), origin.getY(), origin.getZ());
    }

    public Location parseRelativeBlockLocation(String xstr, String ystr, String zstr, float pitch, float yaw) {
        Location loc = parseRelativeBlockLocation(xstr, ystr, zstr);
        loc.setPitch(pitch);
        loc.setYaw(yaw);
        return loc;
    }

    public Location parseRelativeLocation(String xstr, String ystr, String zstr, float pitch, float yaw) {
        Location loc = parseRelativeLocation(xstr, ystr, zstr);
        loc.setPitch(pitch);
        loc.setYaw(yaw);
        return loc;
    }

    public String blockLocationToRelative(Location loc) {
        return parseLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }

    public String locationToRelative(Location loc) {
        return parseLocation(loc.getX(), loc.getY(), loc.getZ(), origin.getX(), origin.getY(), origin.getZ());
    }

    private String parseLocation(double x, double y, double z, double originX, double originY, double originZ) {
        return (x - originX) + "," + (y - originY) + "," + (z - originZ);
    }

    private Location parseLocation(World world, double x, double y, double z, double originX, double originY, double originZ) {
        return new Location(world, originX + x, originY + y, originZ + z);
    }

    private String parseLocation(int x, int y, int z, int originX, int originY, int originZ) {
        return (x - originX) + "," + (y - originY) + "," + (z - originZ);
    }

    private Location parseLocation(World world, int x, int y, int z, int originX, int originY, int originZ) {
        return new Location(world, originX + x, originY + y, originZ + z);
    }

	public double getDistance(Entity ent1, Entity ent2) {
		if (ent1 == null || ent2 == null)
			return -1;
		double dx = ent2.getLocation().getX() - ent1.getLocation().getX();
		double dy = ent2.getLocation().getY() - ent1.getLocation().getY();
		double dz = ent2.getLocation().getZ() - ent1.getLocation().getZ();
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	public String getEntities(World world, String typeEntity) {
		return getEntities(world, null, -1, typeEntity); 
	}
	
	public String getEntities(World world, int entityid, int distance, String typeEntity) {
		Entity entity = plugin.getEntity(entityid);
		return getEntities(world, entity, distance, typeEntity);
	}

	public String getEntities(World world, Entity entity, int distance, String typeEntity) {
		StringBuilder bdr = new StringBuilder();

		if ("".equals(typeEntity)) {  // chaine vide on recherche toutes les entités à proximité de entityId
			for (org.bukkit.entity.Entity e : world.getEntities()) {   // on recherche toutes les entités dans le monde
				if ( e.getType().isSpawnable()  && 	(entity == null || distance < 0 || getDistance(entity, e) <= distance) && entity != e) {
					bdr.append(getEntityMsg(e));
				}			
			} 
		} else {  // on ne recherche que les entités du type demandé
			org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.valueOf(typeEntity);
			for (org.bukkit.entity.Entity e : world.getEntities()) {   // on ne recherche que l'entité du type demandé
				if ( ( e.getType() == entityType) && e.getType().isSpawnable()  && 	(entity == null || distance < 0 || getDistance(entity, e) <= distance) && entity != e) {
					bdr.append(getEntityMsg(e));
				}
			}			
		}		
		return bdr.toString();
	}	

	public String getEntityMsg(Entity entity) {
		StringBuilder bdr = new StringBuilder();
		bdr.append(entity.getEntityId());
		bdr.append(",");
		bdr.append(entity.getType().toString());
		bdr.append(",");
		bdr.append(entity.getLocation().getX());
		bdr.append(",");
		bdr.append(entity.getLocation().getY());
		bdr.append(",");
		bdr.append(entity.getLocation().getZ());
		bdr.append("|");
		return bdr.toString();
	}

	public int removeEntities(World world, Entity entity, int distance) {
		return removeEntities(world, entity, distance, ""); 
	}
	
	public int removeEntities(World world, int entityid, int distance) {
		Entity entity = plugin.getEntity(entityid);
		return removeEntities(world, entity, distance, ""); 
	}
	
	public int removeEntities(World world, String typeEntity) {
		return removeEntities(world, null, -1, typeEntity); 
	}

	public int removeEntities(World world, int entityid, int distance, String typeEntity) {
		Entity entity = plugin.getEntity(entityid);
		return removeEntities(world, entity, distance, typeEntity); 
	}
	
	public int removeEntities(World world, Entity entity, int distance, String typeEntity) {
		int removedEntitiesCount = 0;

		if ("".equals(typeEntity)) {  // chaine vide on recherche toutes les entités à proximité de entityId
			for (org.bukkit.entity.Entity e : world.getEntities()) {   // on recherche toutes les entités dans le monde
				if ( e.getType().isSpawnable()  && 	(entity == null || distance < 0 || getDistance(entity, e) <= distance) && entity != e) {
					e.remove();
					removedEntitiesCount++;
				}			
			} 
		} else {  // on ne recherche que les entités du type demandé
			org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.valueOf(typeEntity);
			for (org.bukkit.entity.Entity e : world.getEntities()) {   // on ne recherche que l'entité du type demandé
				if ( ( e.getType() == entityType) && e.getType().isSpawnable()  && 	(entity == null || distance < 0 || getDistance(entity, e) <= distance) && entity != e) {
					e.remove();
					removedEntitiesCount++;
				}
			}			
		}		

		return removedEntitiesCount;
	}

	public String getChatPosts() {
		return getChatPosts(-1);
	}

	public String getChatPosts(int entityId) {
		StringBuilder b = new StringBuilder();
		for (Iterator<AsyncPlayerChatEvent> iter = chatPostedQueue.iterator(); iter.hasNext(); ) {
			AsyncPlayerChatEvent event = iter.next();
			if (entityId == -1 || event.getPlayer().getEntityId() == entityId) {
				b.append(event.getPlayer().getEntityId());
				b.append(",");
				b.append(event.getMessage());
				b.append("|");
				iter.remove();
			}
		}
		if (b.length() > 0)
			b.deleteCharAt(b.length() - 1);
		 return b.toString();
	}

	public String getBlockHits() {
		return getBlockHits(-1);
	}

	public String getBlockHits(int entityId) {
		StringBuilder b = new StringBuilder();
		for (Iterator<PlayerInteractEvent> iter = interactEventQueue.iterator(); iter.hasNext(); ) {
			PlayerInteractEvent event = iter.next();
			if (entityId == -1 || event.getPlayer().getEntityId() == entityId) {
				Block block = event.getClickedBlock();
				//plugin.getLogger().info("bloc touche avec epee");
				Location loc = block.getLocation();
				b.append(blockLocationToRelative(loc));
				b.append(",");
				// face du block touché 
				b.append(blockFaceToNotch(event.getBlockFace())); // face convertie en entier
				b.append(",");
				b.append(event.getPlayer().getEntityId());
				b.append("|");
				iter.remove();					

			}
		}
		
		
		if (b.length() > 0)
			b.deleteCharAt(b.length() - 1);

		return b.toString();
	}

	public String getProjectileHits() {
		return getProjectileHits(-1);
	}

	public String getProjectileHits(int entityId) {
		StringBuilder b = new StringBuilder();
		for (Iterator<ProjectileHitEvent> iter = projectileHitQueue.iterator(); iter.hasNext(); ) {
			ProjectileHitEvent event = iter.next();
			Arrow arrow = (Arrow) event.getEntity();
			LivingEntity shooter = (LivingEntity)arrow.getShooter();
			if (entityId == -1 || shooter.getEntityId() == entityId) {
				if (shooter instanceof Player) {
					Player player = (Player)shooter;
					Block block = arrow.getAttachedBlock(); 
					if (block == null)
						block = arrow.getLocation().getBlock();
					Location loc = block.getLocation();
					b.append(blockLocationToRelative(loc));
					b.append(",");
					//b.append(1); //blockFaceToNotch(event.getBlockFace()), but don't really care
					//b.append(",");
					b.append(player.getPlayerListName());   // nom du joueur
					
					Entity hitEntity = event.getHitEntity();
					if(hitEntity!=null){
						if(hitEntity instanceof Player){	
							b.append(",");
							Player hitPlayer = (Player)hitEntity;
							b.append(hitPlayer.getEntityId());
							b.append(",");
							b.append(hitPlayer.getPlayerListName());
						}else{
							b.append(",");
							b.append(hitEntity.getEntityId());
							b.append(",");
							b.append(hitEntity.getType().toString());
							//plugin.getLogger().info("Entité touchée : " + b.toString());
						}
					} else {  // on ajoute une information par défaut : identifiacteur 0 et "" nom de l'entité 
						b.append(",");
						b.append(0);
						b.append(",");
						b.append("");
					}
				}
				b.append("|");
				arrow.remove();
				iter.remove();
			}						
		}
		if (b.length() > 0)
			b.deleteCharAt(b.length() - 1); 
		//plugin.getLogger().info("Entité touchée : " + b.toString());
		return b.toString();
	
	}
	
	public void clearEntityEvents(int entityId) {
		for (Iterator<PlayerInteractEvent> iter = interactEventQueue.iterator(); iter.hasNext(); ) {
			PlayerInteractEvent event = iter.next();
			if (event.getPlayer().getEntityId() == entityId)
				iter.remove();
		}
		for (Iterator<AsyncPlayerChatEvent> iter = chatPostedQueue.iterator(); iter.hasNext(); ) {
			AsyncPlayerChatEvent event = iter.next();
			if (event.getPlayer().getEntityId() == entityId)
				iter.remove();
		}
		for (Iterator<ProjectileHitEvent> iter = projectileHitQueue.iterator(); iter.hasNext(); ) {
			ProjectileHitEvent event = iter.next();
			Arrow arrow = (Arrow) event.getEntity();
			LivingEntity shooter = (LivingEntity)arrow.getShooter();
			if (shooter.getEntityId() == entityId)
				iter.remove();
		}
	}
	
	public void send(Object a) {
        send(a.toString());
    }

    public void send(String a) {
        if (pendingRemoval) return;
        synchronized (outQueue) {
            outQueue.add(a);
        }
    }

    public void close() {
        if (closed) return;
        running = false;
        pendingRemoval = true;

        //wait for threads to stop
        try {
            inThread.join(2000);
            outThread.join(2000);
        } catch (InterruptedException e) {
            plugin.getLogger().warning("Failed to stop in/out thread");
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("Closed connection to" + socket.getRemoteSocketAddress() + ".");
    }

    public void kick(String reason) {
        try {
            out.write(reason);
            out.flush();
        } catch (Exception e) {
        }
        close();
    }

    /**
     * socket listening thread
     */
    private class InputThread implements Runnable {
        public void run() {
            plugin.getLogger().info("Starting input thread");
            while (running) {
                try {
                    String newLine = in.readLine();
                    //System.out.println(newLine);
                    if (newLine == null) {
                        running = false;
                    } else {
                        inQueue.add(newLine);
                        //System.out.println("Added to in queue");
                    }
                } catch (Exception e) {
                    // if its running raise an error
                    if (running) {
                        if (e.getMessage().equals("Connection reset")) {
                            plugin.getLogger().info("Connection reset");
                        } else {
                            e.printStackTrace();
                        }
                        running = false;
                    }
                }
            }
            //close in buffer
            try {
                in.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to close in buffer");
                e.printStackTrace();
            }
        }
    }

    private class OutputThread implements Runnable {
        public void run() {
            plugin.getLogger().info("Starting output thread!");
            while (running) {
                try {
                    String line;
                    while ((line = outQueue.poll()) != null) {
                        out.write(line);
                        out.write('\n');
                    }
                    out.flush();
                    Thread.yield();
                    Thread.sleep(1L);
                } catch (Exception e) {
                    // if its running raise an error
                    if (running) {
                        e.printStackTrace();
                        running = false;
                    }
                }
            }
            //close out buffer
            try {
                out.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to close out buffer");
                e.printStackTrace();
            }
        }
    }

    /**
     * from CraftBukkit's org.bukkit.craftbukkit.block.CraftBlock.blockFactToNotch
     */
    public int blockFaceToNotch(BlockFace face) {
        switch (face) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                return 7; // Good as anything here, but technically invalid
        }
    }

}
