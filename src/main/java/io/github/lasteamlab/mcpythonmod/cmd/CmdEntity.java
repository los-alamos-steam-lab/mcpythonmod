package io.github.lasteamlab.mcpythonmod.cmd;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.lasteamlab.mcpythonmod.MCPythonMod;
import io.github.lasteamlab.mcpythonmod.RemoteSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class CmdEntity {
	private final String preFix = "entity.";
	private RemoteSession session;
	private MCPythonMod plugin;

	public CmdEntity(RemoteSession session) {
		this.session = session;
		this.plugin = session.plugin;
	}
	
	public void setBaby(int id) {
		Entity entity = plugin.getEntity(id);
		setBaby(entity);
	}

	public void setBaby(Entity entity) {
		// verify that it is ageable 				
		if ( entity instanceof org.bukkit.entity.Ageable) {
			// retype the entity to be an ageable entity
			org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
			ageable.setBaby();
		} else {
			session.send("Fail, entity is not ageable");
		}
		session.send(entity.getEntityId());
		return;
	}

	public void setAdult(int id) {
		Entity entity = plugin.getEntity(id);
		setAdult(entity);
		return;
	}

	public void setAdult(Entity entity) {
		// verify that it is ageable 				
		if ( entity instanceof org.bukkit.entity.Ageable) {
			// retype the entity to be an ageable entity
			org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
			ageable.setAdult();
		} else {
			session.send("Fail, entity is not ageable");
		}
		session.send(entity.getEntityId());
		return;
	}

	public void setAge(int id, int age) {
		Entity entity = plugin.getEntity(id);
		setAge(entity, age);
		return;
	}

	public void setAge(Entity entity, int age) {
		// verify that it is ageable 				
		if ( entity instanceof org.bukkit.entity.Ageable) {
			// retype the entity to be an ageable entity
			org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
			ageable.setAge(age);
		} else {
			session.send("Fail, entity is not ageable");
		}
		session.send(entity.getEntityId());
		return;
	}

	public void setTamed(int id, boolean tamed) {
		Entity entity = plugin.getEntity(id);
		setTamed(entity, tamed);
		return;
	}

	public void setTamed(Entity entity, boolean tamed) {
		// verify that it is tameable 				
		if ( entity instanceof org.bukkit.entity.Tameable) {
			// retype the entity to be an ageable entity
			org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
			tameable.setTamed(tamed);
		} else {
			session.send("Fail, entity is not tameable");
		}
		session.send(entity.getEntityId());
		return;
	}

	public void setOwner(int id, int ownerid) {
		Entity entity = plugin.getEntity(id);
		Entity owner = plugin.getEntity(ownerid);		
		setOwner(entity, owner);
		return;
	}

	public void setOwner(int id, Entity owner) {
		Entity entity = plugin.getEntity(id);
		setOwner(entity, owner);
		return;
	}

	public void setOwner(Entity entity, int ownerid) {
		Entity owner = plugin.getEntity(ownerid);		
		setOwner(entity, owner);
		return;
	}

	public void setOwner(Entity entity, Entity owner) {
		// verify that it is tameable 				
		if ( entity instanceof org.bukkit.entity.Tameable) {
			if ( (owner instanceof org.bukkit.entity.AnimalTamer)) {
				// retype the entity to be a tameable entity
				org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
				// retype the owner to be an animal tamer 
				org.bukkit.entity.AnimalTamer tamer = (org.bukkit.entity.AnimalTamer) owner;
				tameable.setOwner(tamer);
			} else {
				session.send("Fail, owner is not animal tamer");
			}
		} else {
			session.send("Fail, entity is not tameable");
		}
		session.send(owner.getEntityId());
		return;
	}
	
	public void callMethod(int id, String method, String[] args) {
		Entity entity = plugin.getEntity(id);
		callMethod(entity, method, args);
		return;
	}

	public void callMethod(Entity entity, String method, String[] args) {
		Object result = null;
		Method m = null;

		org.bukkit.entity.EntityType entityType = entity.getType();
		Class<? extends Entity> entityClass = entityType.getEntityClass();
		Object typedEntity = entityClass.cast(entity);			
				

		StringBuilder b = new StringBuilder();	
		
		String[] noargslist = new String[] {"isAngry", "getCollarColor", "getVariant", "isPlayingDead", "isAwake", "getAnger", "getCannotEnterHiveTicks", "getFlower", "getHive", "hasNectar", "hasStung", "getCatType", "getRabbitType", "getFoxType", "getColor", "getInventory", "getStyle", "isScreaming", "getStrength", "getFirstTrustedPlayer", "getSecondTrustedPlayer", "isCrouching"};
		String[] booleanlist = new String[] {"setCrouching", "setAngry", "setPlayingDead", "setAwake", "setHasNectar", "setHasStung", "setScreaming", "setSleeping"};


		try {
			
			// no Args
			if (Arrays.asList(noargslist).contains(method)) {
				m = typedEntity.getClass().getMethod(method);
				result = m.invoke(typedEntity);
			// Boolean arg
			} else if (Arrays.asList(booleanlist).contains(method)) {
				m = typedEntity.getClass().getMethod(method, new Class[] {boolean.class});
				if (Boolean.parseBoolean(args[0])){
					result = m.invoke(typedEntity, new Object[] {true});
					b.append(" - made it to true - ");
				} else {
					result = m.invoke(typedEntity, new Object[] {false});
					b.append(" - made it to false - ");
					b.append(args[0].toLowerCase());
				}			
			// int arg
			} else if (Arrays.asList(new String[] {"setAge", "setAnger", "setCannotEnterHiveTicks", "setStrength"}).contains(method)){
				m = typedEntity.getClass().getMethod(method, new Class[] {int.class});
				result = m.invoke(typedEntity, new Object[] { Integer.parseInt(args[0])});
			// dyecolor arg
			} else if (Arrays.asList(new String[] {"setCollarColor"}).contains(method)){
				org.bukkit.DyeColor color = org.bukkit.DyeColor.valueOf(args[0].toUpperCase());
				m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.DyeColor.class});
				result = m.invoke(typedEntity, new Object[] { color });
			// location arg
			} else if (Arrays.asList(new String[] {"setFlower", "setHive"}).contains(method)){
				org.bukkit.Location location = new org.bukkit.Location( session.origin.getWorld(),  Integer.parseInt(args[0]),  Integer.parseInt(args[1]), Integer.parseInt( args[2]));
				m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.Location.class});
				result = m.invoke(typedEntity, new Object[] { location });
			// setVariant
			} else if (Arrays.asList(new String[] {"setVariant"}).contains(method)){
				Object variant = null;
				if (typedEntity instanceof org.bukkit.entity.Axolotl) {
					variant = org.bukkit.entity.Axolotl.Variant.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Axolotl.Variant.class});
				}
				if (typedEntity instanceof org.bukkit.entity.Parrot) {
					variant = org.bukkit.entity.Parrot.Variant.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Parrot.Variant.class});
				}
				result = m.invoke(typedEntity, new Object[] { variant });
			} else if (Arrays.asList(new String[] {"setCatType"}).contains(method)){
				Object type = null;
				if (typedEntity instanceof org.bukkit.entity.Cat) {
					type = org.bukkit.entity.Cat.Type.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Cat.Type.class});
				}
				result = m.invoke(typedEntity, new Object[] { type });
			} else if (Arrays.asList(new String[] {"setRabbitType"}).contains(method)){
				Object type = null;
				if (typedEntity instanceof org.bukkit.entity.Rabbit) {
					type = org.bukkit.entity.Rabbit.Type.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Rabbit.Type.class});
				}
				result = m.invoke(typedEntity, new Object[] { type });
			} else if (Arrays.asList(new String[] {"setFoxType"}).contains(method)){
				Object type = null;
				if (typedEntity instanceof org.bukkit.entity.Fox) {
					type = org.bukkit.entity.Fox.Type.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Fox.Type.class});
				}
				result = m.invoke(typedEntity, new Object[] { type });
			} else if (Arrays.asList(new String[] {"setColor"}).contains(method)){
				Object color = null;
				if (typedEntity instanceof org.bukkit.entity.Horse) {
					color = org.bukkit.entity.Horse.Color.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Horse.Color.class});
				} else if (typedEntity instanceof org.bukkit.entity.Llama) {
					color = org.bukkit.entity.Llama.Color.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Llama.Color.class});
				}
				result = m.invoke(typedEntity, new Object[] { color });
			} else if (Arrays.asList(new String[] {"setStyle"}).contains(method)){
				Object style = null;
				if (typedEntity instanceof org.bukkit.entity.Horse) {
					style = org.bukkit.entity.Horse.Style.valueOf(args[0].toUpperCase());
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Horse.Style.class});
				}
				result = m.invoke(typedEntity, new Object[] { style });
			} else if (Arrays.asList(new String[] {"setFirstTrustedPlayer", "setSecondTrustedPlayer"}).contains(method)){
				Entity player = plugin.getEntity(Integer.parseInt(args[0]));
				org.bukkit.entity.AnimalTamer tamer = (org.bukkit.entity.AnimalTamer) player;

				if (typedEntity instanceof org.bukkit.entity.Fox) {
					m = typedEntity.getClass().getMethod(method, new Class[] {org.bukkit.entity.Horse.Style.class});
				}				
				result = m.invoke(typedEntity, new Object[] { tamer });
			}
			
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
		b.append(result);
		
		for (Object o : args) {
			b.append(" ");
			b.append((String) o);	
		}
		
		session.send(b);
		
	}
	
	public void execute(String command, String[] args) {

		//get entity based on id
		Entity entity = plugin.getEntity(Integer.parseInt(args[0]));

		if (entity == null) {
			plugin.getLogger().info("Entity [" + args[0] + "] not found.");
			session.send("Fail,This entity identity not exist");
		}

		// entity.getType
		if (command.equals("getType")) {

			session.send(entity.getType());

			// entity.getTile
		} else if (command.equals("getTile")) {

				session.send(session.blockLocationToRelative(entity.getLocation()));

				// entity.setTile
		} else if (command.equals("setTile")) {
			String x = args[1], y = args[2], z = args[3];
			Location loc = entity.getLocation();

			entity.teleport(session.parseRelativeBlockLocation(x, y, z, loc.getPitch(), loc.getYaw()));

			// entity.getPos
		} else if (command.equals("getPos")) {

			session.send(session.locationToRelative(entity.getLocation()));

			// entity.setPos
		} else if (command.equals("setPos")) {
			String x = args[1], y = args[2], z = args[3];
			Location loc = entity.getLocation();

			entity.teleport(session.parseRelativeLocation(x, y, z, loc.getPitch(), loc.getYaw()));

			// entity.setDirection
		} else if (command.equals("setDirection")) {
			Double x = Double.parseDouble(args[1]);
			Double y = Double.parseDouble(args[2]);
			Double z = Double.parseDouble(args[3]);
			Location loc = entity.getLocation();

			loc.setDirection(new Vector(x, y, z));
			entity.teleport(loc);

			// entity.getDirection
		} else if (command.equals("getDirection")) {

			session.send(entity.getLocation().getDirection().toString());

			// entity.setRotation
		} else if (command.equals("setRotation")) {
			Float yaw = Float.parseFloat(args[1]);
			Location loc = entity.getLocation();

			loc.setYaw(yaw);
			entity.teleport(loc);

			// entity.getRotation
		} else if (command.equals("getRotation")) {

			session.send(entity.getLocation().getYaw());

			// entity.setPitch
		} else if (command.equals("setPitch")) {
			Float pitch = Float.parseFloat(args[1]);
			Location loc = entity.getLocation();

			loc.setPitch(pitch);
			entity.teleport(loc);

			// entity.getPitch
		} else if (command.equals("getPitch")) {
			session.send(entity.getLocation().getPitch());

			// entity.getListName
		} else if (command.equals("getName")) {
			if (entity instanceof Player) {
				Player p = (Player) entity;
				//sending list name because plugin.getNamedPlayer() uses list name
				session.send(p.getPlayerListName());
			} else {
				session.send(entity.getName());
			}
		// entity.getEntities
		} else if (command.equals("getEntities")) {
			int entityId = Integer.parseInt(args[0]);
			int distance = Integer.parseInt(args[1]);
			String entityType = "";

			if (args.length == 3) {
				entityType = args[2];
			}				

			session.send(session.getEntities(session.origin.getWorld(), entityId, distance, entityType));
				
		// entity.removeEntities
		} else if (command.equals("removeEntities")) {
			int entityId = Integer.parseInt(args[0]);
			int distance = Integer.parseInt(args[1]);
			String entityType = "";

			if (args.length == 3) {
				entityType = args[2];
			}				

			session.send(session.removeEntities(session.origin.getWorld(), entityId, distance, entityType));
		
		// ageable.getAge()
		} else if (command.equals("getAge")) {
			if ( entity instanceof org.bukkit.entity.Ageable) {
				// retype the entity to be an ageable entity
				org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
				session.send(ageable.getAge());
			} else {
				session.send("Fail, entity is not ageable");
			}
		
		// ageable.setAge()
		} else if (command.equals("setAge")) {
			int age = Integer.parseInt(args[1]);
			setAge(entity, age);
		
		// ageable.getAgeLock()
		} else if (command.equals("getAgeLock")) {
			if ( entity instanceof org.bukkit.entity.Ageable) {
				// retype the entity to be an ageable entity
				org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
				session.send(ageable.getAgeLock());
			} else {
				session.send("Fail, entity is not ageable");
			}
			
		// ageable.setAgeLock()
		} else if (command.equals("setAgeLock")) {
			boolean lock = Boolean.parseBoolean(args[1]);
			if ( entity instanceof org.bukkit.entity.Ageable) {
				// retype the entity to be an ageable entity
				org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
				ageable.setAgeLock(lock);
				session.send(ageable.getAgeLock());
			} else {
				session.send("Fail, entity is not ageable");
			}
			
		// ageable.setBaby()
		} else if (command.equals("setBaby")) {
			setBaby(entity);
		
		// ageable.setAdult()
		} else if (command.equals("setAdult")) {
			setAdult(entity);
		
		// ageable.isAdult()
		} else if (command.equals("isAdult")) {
			if ( entity instanceof org.bukkit.entity.Ageable) {
				// retype the entity to be an ageable entity
				org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
				session.send(ageable.isAdult());
			} else {
				session.send("Fail, entity is not ageable");
			}
		
		//entity.tameable.isTamed
		} else if (command.equals("isTamed")) {
			if ( entity instanceof org.bukkit.entity.Tameable) {
				// retype the entity to be an ageable entity
				org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
				session.send(tameable.isTamed());
			} else {
				session.send("Fail, entity is not tameable");
			}

		//entity.tameable.setTamed
		} else if (command.equals("setTamed")) {
			boolean tamed = Boolean.parseBoolean(args[1]);
			setTamed(entity, tamed);
			
		//entity.Tameable.getOwner
		} else if (command.equals("getOwner")) {
			if ( entity instanceof org.bukkit.entity.Tameable) {
				// retype the entity to be an ageable entity
				org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
				Entity owner = (Entity) tameable.getOwner();
				if ( owner == null) {
					session.send("");
				}
				session.send(owner.getEntityId());
			} else {
				session.send("Fail, entity is not tameable");
			}
			
		//entity.Tameable.setOwner
		} else if (command.equals("setOwner")) {
			int ownerid = Integer.parseInt(args[1]);
			setOwner(entity, ownerid);

		// entity.AbstractHorse.getDomestication()
		} else if (command.equals("getDomestication")) {
			if ( entity instanceof org.bukkit.entity.AbstractHorse) {
				// retype the entity to be an AbstractHorse
				org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) entity;
				session.send(horse.getDomestication());
			} else {
				session.send("Fail, entity is not AbstractHorse");
			}
				
		// entity.AbstractHorse.setDomestication()
		} else if (command.equals("setDomestication")) {
			int level = Integer.parseInt(args[1]);
			if ( entity instanceof org.bukkit.entity.AbstractHorse) {
				// retype the entity to be an AbstractHorse
				org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) entity;
				horse.setDomestication(level);
				session.send(horse.getDomestication());
			} else {
				session.send("Fail, entity is not AbstractHorse");
			}
				
			// entity.AbstractHorse.getMaxDomestication()
			} else if (command.equals("getMaxDomestication")) {
				if ( entity instanceof org.bukkit.entity.AbstractHorse) {
					// retype the entity to be an AbstractHorse
					org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) entity;
					session.send(horse.getMaxDomestication());
				} else {
					session.send("Fail, entity is not AbstractHorse");
				}
					
			// entity.AbstractHorse.setMaxDomestication()
			} else if (command.equals("setMaxDomestication")) {
				int level = Integer.parseInt(args[1]);
				if ( entity instanceof org.bukkit.entity.AbstractHorse) {
					// retype the entity to be an AbstractHorse
					org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) entity;
					horse.setMaxDomestication(level);
					session.send(horse.getMaxDomestication());
				} else {
					session.send("Fail, entity is not AbstractHorse");
				}
					
		// entity.AbstractHorse.getJumpStrength()
		} else if (command.equals("getJumpStrength")) {
			if ( entity instanceof org.bukkit.entity.AbstractHorse) {
				// retype the entity to be an AbstractHorse
				org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) entity;
				session.send(horse.getJumpStrength());
			} else {
				session.send("Fail, entity is not AbstractHorse");
			}
				
		// entity.AbstractHorse.setJumpStrength()
		} else if (command.equals("setJumpStrength")) {
			double strength = Double.parseDouble(args[1]);
			if ( entity instanceof org.bukkit.entity.AbstractHorse) {
				// retype the entity to be an AbstractHorse
				org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) entity;
				horse.setJumpStrength(strength);
				session.send(horse.getJumpStrength());
			} else {
				session.send("Fail, entity is not AbstractHorse");
			}
					
		// entity.events.clear
		} else if (command.equals("events.clear")) {
			int entityId = Integer.parseInt(args[0]);
			session.clearEntityEvents(entityId);
			
		// entity.events.block.hits
		} else if (command.equals("events.block.hits")) {
			int entityId = Integer.parseInt(args[0]);
			session.send(session.getBlockHits(entityId));

			// entity.events.chat.posts
		} else if (command.equals("events.chat.posts")) {
			int entityId = Integer.parseInt(args[0]);
			session.send(session.getChatPosts(entityId));

			// entity.events.projectile.hits
		} else if(command.equals("events.projectile.hits")) {
			int entityId = Integer.parseInt(args[0]);
			session.send(session.getProjectileHits(entityId));
		// call a method - this is so we don't have to set up individual calls for entity with special methods
		} else if(command.equals("callMethod")) {
			String method = args[1];
			String[] newArgs = (String[]) Arrays.copyOfRange(args, 2, args.length);
			callMethod(entity, method, newArgs);
		} else {
			session.plugin.getLogger().warning(preFix + command + " is not supported.");
			session.send("Fail," + preFix + command + " is not supported.");
		}

	}
}
