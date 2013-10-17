package com.artemis;

import java.util.HashMap;

import com.artemis.utils.Bag;


/**
 * Identifies components in artemis without having to use classes.
 * <p>
 * This class keeps a list of all generated component types for fast
 * retrieval.
 * </p>
 *
 * @author Arni Arent
 */
public class ComponentType {
	enum Taxonomy {
		BASIC, POOLED, PACKED;
	}
	
	
	/** Amount of generated component types. */
	private static int INDEX = 0;
	/** Index of this component type in componentTypes. */
	private final int index;
	/** The class type of the component type. */
	private final Class<? extends Component> type;
	/** True if component type is a {@link PackedComponent} */
//	private final boolean packedComponentType;
	private final Taxonomy taxonomy;
	private static final Bag<ComponentType> types = new Bag<ComponentType>();


	/**
	 * Creates a new {@link ComponentType} instance of given component class.
	 *
	 * @param type
	 *			the components class
	 */
	private ComponentType(Class<? extends Component> type) {
		types.set(INDEX, this);
		index = INDEX++;
		this.type = type;
		if (PackedComponent.class.isAssignableFrom(type)) {
			taxonomy = Taxonomy.PACKED;
		} else if (PooledComponent.class.isAssignableFrom(type)) {
			taxonomy = Taxonomy.POOLED;
		} else {
			taxonomy = Taxonomy.BASIC;
		}
	}


	/**
	 * Get the component type's index.
	 *
	 * @return the component types index
	 */
	public int getIndex() {
		return index;
	}


	@Override
	public String toString() {
		return "ComponentType["+type.getSimpleName()+"] ("+index+")";
	}
	
	protected Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	protected static Taxonomy getTaxonomy(int index) {
		ComponentType type = types.get(index);
		return type != null ? type.getTaxonomy() : Taxonomy.BASIC;
	}
	
	public boolean isPackedComponent() {
		return taxonomy == Taxonomy.PACKED;
	}
	
	protected static boolean isPackedComponent(int index) {
		ComponentType type = types.get(index);
		return type != null ? type.isPackedComponent() : false;
	}
	
	protected Class<? extends Component> getType() {
		return type;
	}
	
	/**
	 * Contains all generated component types, newly generated component types
	 * will be stored here.
	 */
	private static final HashMap<Class<? extends Component>, ComponentType> componentTypes
			= new HashMap<Class<? extends Component>, ComponentType>();

	/**
	 * Gets the component type for the given component class.
	 * <p>
	 * If no component type exists yet, a new one will be created and stored
	 * for later retrieval.
	 * </p>
	 *
	 * @param c
	 *			the component's class to get the type for
	 *
	 * @return the component's {@link ComponentType}
	 */
	public static ComponentType getTypeFor(Class<? extends Component> c) {
		ComponentType type = componentTypes.get(c);

		if (type == null) {
			type = new ComponentType(c);
			componentTypes.put(c, type);
		}

		return type;
	}
	
	/**
	 * Gets the component type for the given component class.
	 * <p>
	 *
	 * @param c
	 *			the component's class to get the type for
	 *
	 * @return the component's {@link ComponentType}
	 */
	public static ComponentType getTypeFor(int index) {
		return types.get(index);
	}

	/**
	 * Get the index of the component type of given component class.
	 *
	 * @param c
	 *			the component class to get the type index for
	 *
	 * @return the component type's index
	 */
	public static int getIndexFor(Class<? extends Component> c) {
		return getTypeFor(c).getIndex();
	}
	
}
