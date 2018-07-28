package thebetweenlands.api.rune;

import java.util.Collection;
import java.util.List;

public interface INodeCompositionBlueprint<E> {
	/**
	 * This class represents a link to a node's output
	 */
	public static interface ILink {
		/**
		 * Returns the output node's index
		 * @return the output node's index
		 */
		public int getNode();
		
		/**
		 * Returns the output node's output index
		 * @return the output node's output index
		 */
		public int getOutput();
	}
	
	/**
	 * Returns the number of node blueprints in this composition blueprint.
	 * @return the number of node blueprints in this composition blueprint
	 */
	public int getNodeBlueprints();

	/**
	 * Returns the node blueprint at the specified node index
	 * @param node - index of the node
	 * @return the node blueprint at the specified node index
	 * @throws IndexOutOfBoundsException if the node index is out of range (node < 0 || node >= {@link #getNodeBlueprints()})
	 */
	public INodeBlueprint<?, E> getNodeBlueprint(int node);
	
	/**
	 * Returns the link at the specified node index and input index.
	 * @param node - index of the node
	 * @param input - index of the input
	 * @return the link at the specified node index and input index.
	 * If the specified input does not exist or no link is available <b><i>null</b></i> is returned
	 * @throws IndexOutOfBoundsException if the node index is out of range (node < 0 || node >= {@link #getNodeBlueprints()})
	 */
	public ILink getLink(int node, int input);
	
	/**
	 * Returns a collection of input indices that have links.
	 * @param node - index of the node
	 * @return a collection containing all input indices that have links. If no links are available at any slot an empty collection is returned
	 * @throws IndexOutOfBoundsException if the node index is out of range (node < 0 || node >= {@link #getNodeBlueprints()})
	 */
	public Collection<Integer> getLinkedSlots(int node);
	
	/**
	 * Creates a node composition instance from this blueprint.
	 * @return a node composition instance created from this blueprint
	 */
	public INodeComposition<E> create();
}
