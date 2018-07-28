package thebetweenlands.api.rune.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import thebetweenlands.api.aspect.AspectContainer;
import thebetweenlands.api.aspect.IAspectType;
import thebetweenlands.api.rune.INode;
import thebetweenlands.api.rune.INodeBlueprint;
import thebetweenlands.api.rune.INodeBlueprint.INodeIO;
import thebetweenlands.api.rune.INodeBlueprint.INodeIO.ISchedulerTask;
import thebetweenlands.api.rune.INodeComposition;
import thebetweenlands.api.rune.INodeCompositionBlueprint;
import thebetweenlands.api.rune.INodeCompositionBlueprint.ILink;
import thebetweenlands.api.rune.INodeConfiguration;
import thebetweenlands.api.rune.INodeConfiguration.IConfigurationInput;
import thebetweenlands.api.rune.INodeConfiguration.IConfigurationOutput;
import thebetweenlands.api.rune.INodeConfiguration.IType;
import thebetweenlands.api.rune.IRuneUser;
import thebetweenlands.api.rune.impl.RuneChain.RuneExecutionContext;

public class RuneChain implements INodeComposition<RuneExecutionContext> {
	public static final class Blueprint implements INodeCompositionBlueprint<RuneExecutionContext> {
		private List<NodeSlot> slots = new ArrayList<>();

		private final class NodeSlot {
			private final INodeBlueprint<?, RuneExecutionContext> blueprint;
			private final SlotLink[] links;

			private int cachedIndex = -1;

			private NodeSlot(INodeBlueprint<?, RuneExecutionContext> blueprint) {
				this.blueprint = blueprint;
				int maxSlots = 0;
				for(INodeConfiguration configuration : blueprint.getConfigurations()) {
					maxSlots = Math.max(configuration.getInputs().size(), maxSlots);
				}
				this.links = new SlotLink[maxSlots];
			}

			private int getIndex() {
				if(this.cachedIndex >= 0 && this.cachedIndex < slots.size() && slots.get(this.cachedIndex) == this) {
					return this.cachedIndex;
				}
				return this.cachedIndex = slots.indexOf(this);
			}
		}

		private final class SlotLink implements ILink {
			private final NodeSlot slot;
			private final int output;

			private SlotLink(NodeSlot slot, int output) {
				this.slot = slot;
				this.output = output;
			}

			@Override
			public int getNode() {
				return this.slot.getIndex();
			}

			@Override
			public int getOutput() {
				return this.output;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + this.output;
				result = prime * result + ((this.slot == null) ? 0 : this.slot.getIndex());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				SlotLink other = (SlotLink) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (this.output != other.output)
					return false;
				if (this.slot == null) {
					if (other.slot != null)
						return false;
				} else if (this.slot.getIndex() != other.slot.getIndex())
					return false;
				return true;
			}

			private Blueprint getOuterType() {
				return Blueprint.this;
			}
		}

		@Override
		public int getNodeBlueprints() {
			return this.slots.size();
		}

		@Override
		public INodeBlueprint<?, RuneExecutionContext> getNodeBlueprint(int node) {
			return this.slots.get(node).blueprint;
		}

		@Override
		public Collection<Integer> getLinkedSlots(int node) {
			Set<Integer> linkedSlots = new HashSet<>();
			NodeSlot slot = this.slots.get(node);
			for(int i = 0; i < slot.links.length; i++) {
				if(slot.links[i] != null) {
					linkedSlots.add(i);
				}
			}
			return linkedSlots;
		}

		@Override
		public ILink getLink(int node, int input) {
			if(input >= 0) {
				NodeSlot slot = this.slots.get(node);
				if(input < slot.links.length) {
					return slot.links[input];
				}
			}
			return null;
		}

		@Override
		public RuneChain create() {
			return new RuneChain(this);
		}

		/**
		 * Adds a new node blueprint to the rune chain
		 * @param blueprint - blueprint to add to the rune chain
		 */
		public void addNodeBlueprint(INodeBlueprint<?, RuneExecutionContext> blueprint) {
			this.addNodeBlueprint(this.slots.size(), blueprint);
		}

		/**
		 * Adds a new node blueprint at the specified index to the rune chain
		 * @param index - index where the blueprint should be inserted
		 * @param blueprint - blueprint to add to the rune chain
		 */
		public void addNodeBlueprint(int index, INodeBlueprint<?, RuneExecutionContext> blueprint) {
			this.slots.add(index, new NodeSlot(blueprint));
		}

		/**
		 * Removes the node blueprint at the specified index
		 * @param index - index of the node blueprint to remove
		 * @return the removed node blueprint
		 */
		public INodeBlueprint<?, RuneExecutionContext> removeNodeBlueprint(int index) {
			return this.slots.remove(index).blueprint;
		}

		/**
		 * Tries to unlink the specified input
		 * @param inNodeIndex - index of the input node
		 * @param inputIndex - index of the input node's input
		 * @return <i>true</i> if the input was successfully unlinked, <i>false</i> otherwise
		 */
		public boolean unlink(int inNodeIndex, int inputIndex) {
			if(inputIndex < 0 || inNodeIndex < 0 || inNodeIndex >= this.getNodeBlueprints()) {
				return false;
			}

			NodeSlot inputSlot = this.slots.get(inNodeIndex);

			if(inputIndex >= inputSlot.links.length) {
				return false;
			}

			SlotLink link = inputSlot.links[inputIndex];

			if(link != null) {
				inputSlot.links[inputIndex] = null;
				return true;
			}

			return false;
		}

		/**
		 * Tries to link the specified input to the specified output
		 * @param inNodeIndex - index of the input node
		 * @param inputIndex - index of the input node's input
		 * @param outNodeIndex - index of the output node
		 * @param outputIndex - index of the output node's output
		 * @return <i>true</i> if the input was successfully linked, <i>false</i> otherwise
		 */
		public boolean link(int inNodeIndex, int inputIndex, int outNodeIndex, int outputIndex) {
			if(this.canLink(inNodeIndex, inputIndex, outNodeIndex, outputIndex)) {
				NodeSlot outputSlot = this.slots.get(outNodeIndex);
				NodeSlot inputSlot = this.slots.get(inNodeIndex);
				inputSlot.links[inputIndex] = new SlotLink(outputSlot, outputIndex);
				return true;
			}
			return false;
		}

		/**
		 * Returns whether the specified input can be linked to the specified output
		 * @param inNodeIndex - index of the input node
		 * @param inputIndex - index of the input node's input
		 * @param outNodeIndex - index of the output node
		 * @param outputIndex - index of the output node's output
		 * @return whether the specifeid input can be linked to the specified output
		 */
		public boolean canLink(int inNodeIndex, int inputIndex, int outNodeIndex, int outputIndex) {
			if(outNodeIndex < 0 || outNodeIndex >= this.getNodeBlueprints() || inNodeIndex < 0 || inNodeIndex >= this.getNodeBlueprints() || inNodeIndex <= outNodeIndex) {
				return false;
			}

			List<INodeConfiguration> outConfigurations = this.getValidConfigurations(outNodeIndex, false);
			List<INodeConfiguration> inConfigurations = this.getValidConfigurations(inNodeIndex, false);

			for(INodeConfiguration inConfiguration : inConfigurations) {
				List<IConfigurationInput> inputs = inConfiguration.getInputs();

				if(inputIndex < inputs.size()) {
					IConfigurationInput input = inputs.get(inputIndex);

					for(INodeConfiguration outConfiguration : outConfigurations) {
						List<IConfigurationOutput> outputs = outConfiguration.getOutputs();

						if(outputIndex < outputs.size()) {
							List<IType> validOutputTypes = this.getValidOutputTypes(outNodeIndex, outputIndex);

							for(IType outputType : validOutputTypes) {
								if(input.test(outputType)) {
									return true;
								}
							}
						}
					}
				}
			}

			return false;
		}

		private List<IType> getValidOutputTypes(int nodeIndex, int outputIndex) {
			List<IType> validOutputTypes = new ArrayList<>();
			List<INodeConfiguration> configurations = this.getValidConfigurations(nodeIndex, false);
			Collection<Integer> linkedSlots = this.getLinkedSlots(nodeIndex);

			for(INodeConfiguration configuration : configurations) {
				List<IConfigurationOutput> outputs = configuration.getOutputs();
				List<IConfigurationInput> inputs = configuration.getInputs();

				if(outputIndex < outputs.size()) {
					IConfigurationOutput output = outputs.get(outputIndex);

					ImmutableList.Builder<IType> inputTypesBuilder = ImmutableList.builder();
					for(int i = 0; i < inputs.size(); i++) {
						if(linkedSlots.contains(i)) {
							ILink link = this.getLink(nodeIndex, i);
							inputTypesBuilder.addAll(this.getValidOutputTypes(link.getNode(), link.getOutput()));
						} else {
							inputTypesBuilder.add((IType) null);
						}
					}

					List<IType> inputTypes = inputTypesBuilder.build();

					if(output.isEnabled(inputTypes)) {
						validOutputTypes.add(output.getType(inputTypes));
					}
				}
			}
			return validOutputTypes;
		}

		private List<INodeConfiguration> getValidConfigurations(int nodeIndex, boolean onlyFullyLinked) {
			if(nodeIndex < 0 || nodeIndex >= this.getNodeBlueprints()) {
				return Collections.emptyList();
			}

			INodeBlueprint<?, ?> node = this.getNodeBlueprint(nodeIndex);

			List<INodeConfiguration> validConfigurations = new ArrayList<>();

			configurations: for(INodeConfiguration configuration : node.getConfigurations()) {
				List<IConfigurationInput> inputs = configuration.getInputs();

				Collection<Integer> linkedSlots = this.getLinkedSlots(nodeIndex);

				if(onlyFullyLinked && linkedSlots.size() != inputs.size()) {
					continue configurations;
				}

				for(int i : linkedSlots) {
					if(i < inputs.size()) {
						IConfigurationInput input = inputs.get(i);

						ILink link = this.getLink(nodeIndex, i);

						boolean validOutputConfiguration = false;

						for(INodeConfiguration outputConfiguration : this.getValidConfigurations(link.getNode(), false)) {
							List<IConfigurationOutput> outputs = outputConfiguration.getOutputs();

							if(link.getOutput() < outputs.size()) {
								List<IType> validOutputTypes = this.getValidOutputTypes(link.getNode(), link.getOutput());
								for(IType outputType : validOutputTypes) {
									if(input.test(outputType)) {
										validOutputConfiguration = true;
										break;
									}
								}
							}
						}

						if(!validOutputConfiguration) {
							continue configurations;
						}
					} else {
						continue configurations;
					}
				}

				validConfigurations.add(configuration);
			}

			return validConfigurations;
		}
	}

	private static final class Scheduler implements INodeIO.IScheduler {
		private boolean terminated;
		private float delay;

		private Scheduler() {
			this.reset();
		}

		private void reset() {
			this.delay = 0.0F;
			this.terminated = false;
		}

		@Override
		public void sleep(float delay) {
			if(delay > 0) {
				this.delay += delay;
			}
		}

		@Override
		public void terminate() {
			this.terminated = true;
		}
	}

	private final class NodeIO implements INodeIO {
		private INode<?,RuneExecutionContext> node;
		private INodeBlueprint<INode<?, RuneExecutionContext>, RuneExecutionContext> blueprint;
		private Branch branch;

		private boolean branchingAllowed;
		private boolean branched;
		private boolean failed;
		private boolean terminated;
		private INodeIO.ISchedulerTask task;

		private NodeIO() {
			this.reset(null, null, null);
		}

		private void reset(Branch branch, INodeBlueprint<INode<?, RuneExecutionContext>, RuneExecutionContext> blueprint, INode<?, RuneExecutionContext> node) {
			this.branch = branch;
			this.node = node;
			this.blueprint = blueprint;
			this.branchingAllowed = true;
			this.branched = false;
			this.failed = false;
			this.terminated = false;
			this.task = null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void set(int output, Object obj) {
			this.branchingAllowed = false;
			// TODO Type check?
			if(obj instanceof Collection) {
				RuneChain.this.outputValues.set(output, (Collection<Object>) obj);
			} else {
				RuneChain.this.outputValues.set(output, Collections.singletonList(obj));
			}
		}

		@Override
		public void fail() {
			this.branchingAllowed = false;
			if(!this.failed) {
				this.failed = true;
				this.blueprint.fail(this.node, RuneChain.this.context);
			} else {
				this.failed = true;
			}
		}

		@Override
		public void branch() {
			if(!this.branchingAllowed) {
				throw new IllegalStateException("Cannot branch after calling set or fail");
			}
			if(!this.branched) {
				this.branch = new Branch(this.branch);
				this.branched = true;
			}
		}

		@Override
		public void terminate() {
			this.terminated = true;
		}

		@Override
		public void schedule(ISchedulerTask task) {
			this.task = task;
		}

		@Override
		public Object get(int input) {
			// TODO Type check?
			return RuneChain.this.combination[input];
		}

		@Override
		public boolean branched() {
			return this.branched;
		}

		@Override
		public boolean failed() {
			return this.failed;
		}

		@Override
		public ISchedulerTask scheduled() {
			return this.task;
		}

		@Override
		public boolean terminated() {
			return this.terminated;
		}
	}

	private final class Branch {
		private final Branch parent;
		private Int2ObjectMap<List<Collection<Object>>> outputValues;

		private Branch(Branch parent) {
			this.parent = parent;
		}

		private void addOverrideOutputValues(int node, List<Collection<Object>> values) {
			if(this.outputValues == null) {
				this.outputValues = new Int2ObjectOpenHashMap<>();
			}

			this.outputValues.put(node, values);
		}

		private void addOverrideOutputValue(int node, int slot, Collection<Object> values) {
			if(this.outputValues == null) {
				this.outputValues = new Int2ObjectOpenHashMap<>();
			}

			List<Collection<Object>> nodeValues = this.outputValues.get(node);

			List<Collection<Object>> parentValues = this.getOutputValues(node);
			if(parentValues != null) {
				nodeValues = new ArrayList<>(parentValues.size());
				nodeValues.addAll(parentValues);
				this.outputValues.put(node, nodeValues);
			} else {
				int slots = RuneChain.this.nodes.get(node).getConfiguration().getInputs().size();
				nodeValues = new ArrayList<>(slots);
				for(int i = 0; i < slots; i++) {
					nodeValues.add(Collections.emptyList());
				}
			}

			if(slot < nodeValues.size()) {
				nodeValues.set(slot, values);
			}
		}

		@Nullable
		private List<Collection<Object>> getOutputValues(int node) {
			Branch branch = this;
			do {
				if(branch.outputValues != null) {
					List<Collection<Object>> nodeValues = branch.outputValues.get(node);
					if(nodeValues != null) {
						return nodeValues;
					}
				}

				branch = branch.parent;
			} while(branch != null);

			return null;
		}
	}

	public final class RuneExecutionContext {
		private final IRuneUser user;
		private int parallelActivationCount;
		private int parallelActivation;
		private int branchCount;
		private int branch;

		private RuneExecutionContext(IRuneUser user) {
			this.user = user;
		}

		/**
		 * Returns the user that activated the rune chain
		 * @return the user that activated the rune chain
		 */
		public IRuneUser getUser() {
			return this.user;
		}

		/**
		 * Returns the aspect buffer that provides the runes with aspect.
		 * @return the aspect buffer that provides the runes with aspect
		 */
		public IAspectBuffer getAspectBuffer() {
			return RuneChain.this.aspectBuffer;
		}
		
		/**
		 * Returns the number of currently active branches.
		 * @return the number of currently active branches
		 */
		public int getBranchCount() {
			return this.branchCount;
		}

		/**
		 * Returns the current active branch starting at 0.
		 * @return the current active branch starting at 0
		 */
		public int getBranch() {
			return this.branch;
		}

		/**
		 * Returns the number of current parallel rune activations.
		 * @return the number of current parallel rune activations
		 */
		public int getParallelActivationCount() {
			return this.parallelActivationCount;
		}

		/**
		 * Returns the current parallel rune activation starting at 0.
		 * @return the current parallel rune activation starting at 0
		 */
		public int getParallelActivation() {
			return this.parallelActivation;
		}

		/**
		 * Returns the currently activating rune index.
		 * @return the currently activating rune index
		 */
		public int getRune() {
			return RuneChain.this.currentNode;
		}
	}

	@FunctionalInterface
	public static interface IAspectBuffer {
		/**
		 * Gets an aspect container for the specified aspect type that
		 * can be modified. The container may be empty if no aspect container
		 * with the requested aspect type is available. All runes require and
		 * drain aspects from this container. This container could be used
		 * to provide an aspect buffer that is continuously refilled to
		 * restrict how often or how quickly the runes can be activated before
		 * they fail. Also keep in mind that this aspect container may be drained
		 * many times very quickly so it should be optimized for that (e.g. don't save
		 * on change but instead only after updating the rune chain).
		 * @param type - the requested type of the aspect
		 */
		public AspectContainer get(IAspectType type);
	}
	
	private final Blueprint blueprint;
	private final List<INode<?, RuneExecutionContext>> nodes;

	private final Scheduler scheduler = new Scheduler();
	private final NodeIO nodeIO = new NodeIO();

	private IAspectBuffer aspectBuffer;
	
	private boolean running = false;

	private int nextNode = 0;
	private Queue<Branch> branches;
	private Queue<Branch> newBranches;
	private int currentNode;
	private boolean sourceBranchAdded = false;
	private Branch sourceBranch;
	private float delay;
	private RuneExecutionContext context;
	private List<List<Object>> inputValues;
	private int currentCombination;
	private int combinations;
	private int[] itemCounts;
	private int[] divs;
	private List<Collection<Object>> outputValues = new ArrayList<>();
	private Object[] combination;

	private ISchedulerTask scheduledTask;

	private RuneChain(Blueprint blueprint) {
		this.blueprint = blueprint;
		this.nodes = new ArrayList<>(this.blueprint.getNodeBlueprints());

		for(int i = 0; i < this.blueprint.getNodeBlueprints(); i++) {
			List<INodeConfiguration> validConfigurations = this.blueprint.getValidConfigurations(i, true);

			if(validConfigurations.isEmpty()) {
				// Add dummy node that always fails instantly
				this.nodes.add(NodeDummy.Blueprint.INSTANCE.create(this, NodeDummy.Blueprint.CONFIGURATION));
			} else {
				INodeConfiguration configuration = validConfigurations.get(0);
				INodeBlueprint<?, RuneExecutionContext> nodeBlueprint = this.blueprint.getNodeBlueprint(i);
				this.nodes.add(nodeBlueprint.create(this, configuration));
			}
		}
	}

	@Override
	public INodeCompositionBlueprint<RuneExecutionContext> getBlueprint() {
		return this.blueprint;
	}

	@Override
	public INode<?, RuneExecutionContext> getNode(int node) {
		return this.nodes.get(node);
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void run(RuneExecutionContext context) {
		Preconditions.checkNotNull(this.aspectBuffer, "Aspect buffer must be set before running rune chain");
		
		this.nextNode = 0;
		this.context = context;
		this.running = true;
		this.delay = 0;
		this.branches = new LinkedList<>();
		this.branches.add(new Branch(null)); // Add root branch
		this.currentCombination = 0;
		this.inputValues = null;
		this.sourceBranchAdded = false;
		this.currentNode = 0;
		this.combinations = 1;
		this.divs = null;
		this.itemCounts = null;
		this.outputValues = null;
		this.combination = null;
		this.scheduledTask = null;
		this.newBranches = null;

		this.update();
	}

	/**
	 * Sets the aspect buffer that provides the runes with aspect. Must be set before calling
	 * {@link #run(IRuneUser)}.
	 * @param buffer - aspect buffer that provides the runes with aspect
	 */
	public void setAspectBuffer(IAspectBuffer buffer) {
		this.aspectBuffer = buffer;
	}
	
	/**
	 * Starts the execution of this rune chain. Requires an aspect buffer
	 * before running, see {@link #setAspectBuffer(IAspectBuffer)}!
	 * @param user - rune user that is executing this rune chain
	 */
	public void run(IRuneUser user) {
		this.run(new RuneExecutionContext(user));
	}

	/**
	 * Updates the rune chain execution if the rune
	 * chain is running
	 */
	public void update() {
		if(this.running) {
			boolean resumeSuspension = false;

			if(this.delay >= 1.0F) {
				this.delay -= 1.0F;

				if(this.scheduledTask != null && this.delay < 1.0F) {
					if(this.updateTask()) {
						// Delay has accumulated > 1.0, exit and continue next tick
						return;
					}
				}

				if(this.scheduledTask == null && this.delay < 1.0F) {
					resumeSuspension = true;
				}
			}

			if(this.delay < 1.0F) {
				while(this.nextNode < this.nodes.size() || resumeSuspension) {
					if(!resumeSuspension) {
						this.newBranches = new LinkedList<>();
					}

					if(!resumeSuspension) {
						this.currentNode = this.nextNode++;
						this.context.branchCount = this.branches.size();
						this.context.branch = 0;
					}

					INode<?, RuneExecutionContext> node = this.nodes.get(this.currentNode);
					@SuppressWarnings("unchecked")
					INodeBlueprint<INode<?, RuneExecutionContext>, RuneExecutionContext> blueprint = (INodeBlueprint<INode<?, RuneExecutionContext>, RuneExecutionContext>) node.getBlueprint();
					INodeConfiguration configuration = node.getConfiguration();
					List<IConfigurationInput> inputs = configuration.getInputs();

					while(!this.branches.isEmpty() || resumeSuspension) {
						if(!resumeSuspension) {
							this.sourceBranchAdded = false;
							this.sourceBranch = this.branches.remove();

							this.inputValues = new ArrayList<>();

							// Collect input values
							for(int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
								//TODO Is it possible to merge the collections without having to collect all values
								//and only get the values later on iteratively?
								List<Object> values = new ArrayList<>();
								ILink link = this.blueprint.getLink(this.currentNode, inputIndex);
								values.addAll(this.sourceBranch.getOutputValues(link.getNode()).get(link.getOutput()));
								this.inputValues.add(values);
							}

							//Prepare input combinations
							this.combinations = 1;
							this.itemCounts = new int[inputs.size()];
							this.divs = new int[inputs.size()];

							for(int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
								IConfigurationInput input = inputs.get(inputIndex);

								if(input.isCollection()) {
									//If input is multi-input then treat collection as one value
									this.itemCounts[inputIndex] = 1;
								} else {
									this.combinations *= (this.itemCounts[inputIndex] = this.inputValues.get(inputIndex).size());
								}

								int div = 1;
								if(inputIndex > 0) {
									for(int divSlotIndex = inputIndex - 1; divSlotIndex < this.itemCounts.length - 1; divSlotIndex++) {
										div *= this.itemCounts[divSlotIndex];
									}
								}
								this.divs[inputIndex] = div;
							}

							this.outputValues = new ArrayList<>();
							for(int i = 0; i < configuration.getOutputs().size(); i++) {
								this.outputValues.add(Collections.emptyList());
							}

							this.combination = new Object[inputs.size()];
							this.currentCombination = 0;

							this.context.parallelActivationCount = this.combinations;
						}

						//Execute node for each combination
						while(this.currentCombination < this.combinations) {
							this.context.parallelActivation = this.currentCombination;

							// Get input value combination
							for(int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
								IConfigurationInput input = inputs.get(inputIndex);
								if(input.isCollection()) {
									this.combination[inputIndex] = this.inputValues.get(inputIndex);
								} else {
									this.combination[inputIndex] = this.inputValues.get(inputIndex).get((this.currentCombination / this.divs[inputIndex]) % this.itemCounts[inputIndex]);
								}
							}

							// Reset I/O for new node execution
							this.nodeIO.reset(this.sourceBranch, blueprint, node);

							blueprint.run(node, this.context, this.nodeIO);

							// Increment before potentially suspending
							this.currentCombination++;

							// Already resumed don't try again next loop
							resumeSuspension = false;

							if(!this.nodeIO.failed && !this.nodeIO.terminated) {
								// Add output values to new branch
								this.nodeIO.branch.addOverrideOutputValues(this.currentNode, this.outputValues);

								if(this.nodeIO.branch != this.sourceBranch) {
									this.newBranches.add(this.nodeIO.branch);

									// Override values at nodes that produced the input values
									for(int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
										ILink link = this.blueprint.getLink(this.currentNode, inputIndex);
										this.nodeIO.branch.addOverrideOutputValue(link.getNode(), link.getOutput(), Collections.singleton(this.combination[inputIndex]));
									}
								} else if(this.nodeIO.branch == this.sourceBranch && !this.sourceBranchAdded) {
									this.newBranches.add(this.sourceBranch);
									this.sourceBranchAdded = true;
								}
							} else if(this.nodeIO.terminated) {
								this.terminate();
								return;
							} else if(this.nodeIO.branch == this.sourceBranch) {
								this.newBranches.remove(this.sourceBranch);
								this.currentCombination = this.combinations; //Exit loop
							}
							
							if(!this.nodeIO.terminated && this.nodeIO.task != null) {
								this.scheduledTask = this.nodeIO.task;
								if(this.updateTask()) {
									// Delay has accumulated > 1.0, exit and continue next tick
									return;
								}
							}
						}

						this.context.branch++;

						// Already resumed don't try again next loop
						resumeSuspension = false;
					}

					this.branches = this.newBranches;

					// Already resumed don't try again next loop
					resumeSuspension = false;
				}

				this.terminate();
			}
		}
	}

	private void terminate() {
		this.running = false;

		for(INode<?, RuneExecutionContext> node : this.nodes) {
			@SuppressWarnings("unchecked")
			INodeBlueprint<INode<?, RuneExecutionContext>, RuneExecutionContext> blueprint = (INodeBlueprint<INode<?, RuneExecutionContext>, RuneExecutionContext>) node.getBlueprint();
			blueprint.terminate(node, this.context);
		}
	}

	private boolean updateTask() {
		this.scheduler.reset();

		while(this.delay < 1.0F) {
			this.scheduledTask.update(this.scheduler);

			this.delay += this.scheduler.delay;

			if(this.scheduler.terminated) {
				this.scheduledTask = null;
				return this.delay >= 1.0F;
			}
		}

		return true;
	}
}
