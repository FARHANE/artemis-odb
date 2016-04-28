package com.artemis.link;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntitySubscription;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;

import static com.artemis.Aspect.all;

class EntityBagFieldMutator implements MultiFieldMutator<Bag<Entity>> {
	private final Bag<Entity> empty = new Bag<Entity>();
	private final EntitySubscription all;

	public EntityBagFieldMutator(World world) {
		all = world.getAspectSubscriptionManager().get(all());
	}

	@Override
	public void validate(int sourceId, Bag<Entity> collection, LinkListener listener) {
		for (int i = 0; collection.size() > i; i++) {
			Entity e = collection.get(i);
			if (!all.getActiveEntityIds().get(e.getId())) {
				collection.remove(i--);
				if (listener != null)
					listener.onTargetDead(sourceId, e.getId());
			}
		}
	}

	@Override
	public Bag<Entity> read(Component c, Field f) {
		try {
			Bag<Entity> e = (Bag<Entity>) f.get(c);
			return (e != null) ? e : empty;
		} catch (ReflectionException exc) {
			throw new RuntimeException(exc);
		}
	}
}
