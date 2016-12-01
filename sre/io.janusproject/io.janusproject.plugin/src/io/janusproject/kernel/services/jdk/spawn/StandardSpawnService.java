/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2016 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.janusproject.kernel.services.jdk.spawn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.inject.Inject;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.janusproject.kernel.bic.BuiltinCapacityUtil;
import io.janusproject.services.AbstractDependentService;
import io.janusproject.services.contextspace.ContextSpaceService;
import io.janusproject.services.spawn.KernelAgentSpawnListener;
import io.janusproject.services.spawn.SpawnService;
import io.janusproject.services.spawn.SpawnServiceListener;
import io.janusproject.util.ListenerCollection;

import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.util.SynchronizedCollection;
import io.sarl.lang.util.SynchronizedSet;
import io.sarl.sarlspecification.SarlSpecificationChecker;
import io.sarl.util.Collections3;

/**
 * Implementation of a spawning service that is based on the other services of the Janus platform.
 *
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class StandardSpawnService extends AbstractDependentService implements SpawnService {

	private final ListenerCollection<?> globalListeners = new ListenerCollection<>();

	// TODO The use of two maps is slowly the platform
	private final Map<UUID, ListenerCollection<SpawnServiceListener>> agentLifecycleListeners = new TreeMap<>();

	// TODO The use of two maps is slowly the platform
	private final Map<UUID, Agent> agents = new TreeMap<>();

	private final Injector injector;

	private final SarlSpecificationChecker sarlSpecificationChecker;

	/**
	 * Constructs the service with the given (injected) injector.
	 *
	 * @param injector
	 *            the injector that should be used by this service for creating the agents.
	 * @param sarlSpecificationChecker the tool for checking the validity of the SARL specification supported by
	 *      the agents to launch.
	 */
	@Inject
	public StandardSpawnService(Injector injector, SarlSpecificationChecker sarlSpecificationChecker) {
		this.injector = injector;
		this.sarlSpecificationChecker = sarlSpecificationChecker;
	}

	@Override
	public final Class<? extends Service> getServiceType() {
		return SpawnService.class;
	}

	@Override
	public Collection<Class<? extends Service>> getServiceDependencies() {
		return Arrays.<Class<? extends Service>>asList(ContextSpaceService.class);
	}

	private void ensureSarlSpecificationVersion(Class<? extends Agent> agentClazz) {
		if (!this.sarlSpecificationChecker.isValidSarlElement(agentClazz)) {
			throw new InvalidSarlSpecificationException(agentClazz);
		}
	}

	@Override
	public List<UUID> spawn(int nbAgents, AgentContext parent, UUID agentID, Class<? extends Agent> agentClazz, Object... params) {
		if (isRunning() && nbAgents > 0) {
			try {
				// Check if the version of the SARL agent class is compatible.
				ensureSarlSpecificationVersion(agentClazz);
				final JustInTimeAgentInjectionModule agentInjectionModule = new JustInTimeAgentInjectionModule(this.injector, agentClazz,
						parent.getID(), nbAgents > 1 ? null : agentID);
				final Injector agentInjector = this.injector.createChildInjector(agentInjectionModule);
				final List<Agent> agents = new ArrayList<>(nbAgents);
				for (int i = 0; i < nbAgents; ++i) {
					final Agent agent = agentInjector.getInstance(Agent.class);
					assert agent != null;
					synchronized (this.agents) {
						this.agents.put(agent.getID(), agent);
					}
					agents.add(agent);
					fireAgentSpawnedInAgent(parent, agent, params);
				}
				fireAgentSpawnedOutsideAgent(parent, agentClazz, agents, params);
				return Collections.unmodifiableList(Lists.transform(agents, (it) -> it.getID()));
			} catch (Throwable e) {
				throw new CannotSpawnException(agentClazz, e);
			}
		}
		throw new SpawnDisabledException(parent.getID(), agentClazz);
	}

	/** Notify the listeners about the agents' spawning.
	 *
	 * @param context the context in which the agents were spawned.
	 * @param agentClazz the type of the spwnaed agents.
	 * @param agents the spawned agents.
	 * @param initializationParameters the initialization parameters.
	 */
	protected void fireAgentSpawnedOutsideAgent(AgentContext context, Class<? extends Agent> agentClazz, List<Agent> agents,
			Object... initializationParameters) {
		// Notify the listeners on the spawn events (not restricted to a single agent)
		for (final SpawnServiceListener l : this.globalListeners.getListeners(SpawnServiceListener.class)) {
			l.agentSpawned(context, agents, initializationParameters);
		}

		// Send the event in the default space.
		final EventSpace defSpace = context.getDefaultSpace();
		assert defSpace != null : "A context does not contain a default space"; //$NON-NLS-1$
		final Address source = new Address(defSpace.getSpaceID(), context.getID());
		assert source != null;
		defSpace.emit(new AgentSpawned(source, agentClazz.getName(),
				Collections2.transform(agents, (it) -> it.getID())));
	}

	/** Notify the agent's listeners about its spawning.
	 *
	 * @param context the context in which the agent was spawned.
	 * @param agent the spawned agent.
	 * @param initializationParameters the initialization parameters.
	 */
	protected void fireAgentSpawnedInAgent(AgentContext context, Agent agent, Object... initializationParameters) {
		// Notify the listeners on the lifecycle events on
		// the just spawned agent.
		// Usually, only BICs and the AgentLifeCycleSupport in
		// io.janusproject.kernel.bic.StandardBuiltinCapacitiesProvider
		// is invoked.
		final ListenerCollection<SpawnServiceListener> list;
		synchronized (this.agentLifecycleListeners) {
			list = this.agentLifecycleListeners.get(agent.getID());
		}
		if (list != null) {
			for (final SpawnServiceListener l : list.getListeners(SpawnServiceListener.class)) {
				l.agentSpawned(context, Collections.singletonList(agent), initializationParameters);
			}
		}
	}

	/**
	 * Simple structure to store the result of various tests that must be done synchronously before killing agents.
	 * @author $Author: ngaud$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 *
	 */
	private class KillAgentResultStructure {
		Boolean canKill = Boolean.FALSE;

		Boolean isLast = Boolean.FALSE;

		Agent killAgent;

		KillAgentResultStructure() {
			//
		}
	}

	private KillAgentResultStructure $killAgent(UUID agentID) {
		// We should check if it is possible to kill the agent BEFORE killing it.
		final KillAgentResultStructure k;
		synchronized (this.agents) {
			final Agent agent = this.agents.get(agentID);
			if (agent != null) {
				k = new KillAgentResultStructure();
				if (canKillAgent(agent)) {
					this.agents.remove(agentID);
					k.isLast = Boolean.valueOf(this.agents.isEmpty());
					k.canKill = Boolean.TRUE;
					k.killAgent = agent;
					return k;
				}
				return k;
			}
			return null;
		}
	}

	@Override
	public void killAgent(UUID agentID) throws AgentKillException {
		final boolean error = !isRunning();

		final KillAgentResultStructure synchroTests = $killAgent(agentID);
		if (synchroTests != null && synchroTests.canKill.booleanValue()) {
			fireAgentDestroyed(synchroTests.killAgent);

			if (synchroTests.isLast.booleanValue()) {
				fireKernelAgentDestroy();
			}
			if (error) {
				throw new SpawnServiceStopException(agentID);
			}
		} else {
			if (synchroTests == null) {
				throw new AgentKillException(agentID);
			}
		}

	}

	/**
	 * Replies the registered agents.
	 *
	 * @return the registered agents.
	 */
	public synchronized SynchronizedSet<UUID> getAgents() {
		return Collections3.synchronizedSet(this.agents.keySet(), this);
	}

	/**
	 * Replies the registered agent.
	 *
	 * @param id
	 *            is the identifier of the agent.
	 * @return the registered agent, or <code>null</code>.
	 */
	synchronized Agent getAgent(UUID id) {
		assert id != null;
		return this.agents.get(id);
	}

	@Override
	public void addKernelAgentSpawnListener(KernelAgentSpawnListener listener) {
		this.globalListeners.add(KernelAgentSpawnListener.class, listener);
	}

	@Override
	public void removeKernelAgentSpawnListener(KernelAgentSpawnListener listener) {
		this.globalListeners.remove(KernelAgentSpawnListener.class, listener);
	}

	/**
	 * Notifies the listeners about the kernel agent creation.
	 */
	protected void fireKernelAgentSpawn() {
		for (final KernelAgentSpawnListener l : this.globalListeners.getListeners(KernelAgentSpawnListener.class)) {
			l.kernelAgentSpawn();
		}
	}

	/**
	 * Notifies the listeners about the kernel agent destruction.
	 */
	protected void fireKernelAgentDestroy() {
		for (final KernelAgentSpawnListener l : this.globalListeners.getListeners(KernelAgentSpawnListener.class)) {
			l.kernelAgentDestroy();
		}
	}

	@Override
	public void addSpawnServiceListener(UUID id, SpawnServiceListener agentLifecycleListener) {
		synchronized (this.agentLifecycleListeners) {
			this.agentLifecycleListeners.compute(id, (key, value) -> {
				ListenerCollection<SpawnServiceListener> list = value;
				if (list == null) {
					list = new ListenerCollection<>();
				}
				list.add(SpawnServiceListener.class, agentLifecycleListener);
				return value;
			});
		}
	}

	@Override
	public void addSpawnServiceListener(SpawnServiceListener agentLifecycleListener) {
		this.globalListeners.add(SpawnServiceListener.class, agentLifecycleListener);
	}

	@Override
	public void removeSpawnServiceListener(UUID id, SpawnServiceListener agentLifecycleListener) {
		synchronized (this.agentLifecycleListeners) {
			this.agentLifecycleListeners.remove(id, agentLifecycleListener);
		}
	}

	@Override
	public void removeSpawnServiceListener(SpawnServiceListener agentLifecycleListener) {
		this.globalListeners.remove(SpawnServiceListener.class, agentLifecycleListener);
	}

	/**
	 * Replies if the given agent can be killed.
	 *
	 * @param agent
	 *            - agent to test.
	 * @return <code>true</code> if the given agent can be killed, otherwise <code>false</code>.
	 */
	@SuppressWarnings("static-method")
	public synchronized boolean canKillAgent(Agent agent) {
		try {
			final AgentContext ac = BuiltinCapacityUtil.getContextIn(agent);
			if (ac != null) {
				final Set<UUID> participants = ac.getDefaultSpace().getParticipants();
				if (participants != null && (participants.size() > 1 || (participants.size() == 1 && !participants.contains(agent.getID())))) {
					return false;
				}
			}
			return true;
		} catch (Throwable exception) {
			return false;
		}
	}

	/**
	 * Notifies the listeners about the agent destruction.
	 *
	 * @param agent
	 *            - the destroyed agent.
	 */
	protected void fireAgentDestroyed(Agent agent) {
		final ListenerCollection<SpawnServiceListener> list;
		synchronized (this.agentLifecycleListeners) {
			list = this.agentLifecycleListeners.get(agent.getID());
		}
		final SpawnServiceListener[] ilisteners = list.getListeners(SpawnServiceListener.class);
		final SpawnServiceListener[] ilisteners2 = this.globalListeners.getListeners(SpawnServiceListener.class);

		try {
			final SynchronizedCollection<AgentContext> sc = BuiltinCapacityUtil.getContextsOf(agent);
			synchronized (sc.mutex()) {
				for (final AgentContext context : sc) {
					final EventSpace defSpace = context.getDefaultSpace();
					defSpace.emit(new AgentKilled(defSpace.getAddress(agent.getID()), agent.getID(), agent.getClass().getName()));
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (final SpawnServiceListener l : ilisteners) {
			l.agentDestroy(agent);
		}
		for (final SpawnServiceListener l : ilisteners2) {
			l.agentDestroy(agent);
		}
	}

	@Override
	protected synchronized void doStart() {
		// Assume that when the service is starting, the kernel agent is up.
		fireKernelAgentSpawn();
		notifyStarted();
	}

	@Override
	protected synchronized void doStop() {
		synchronized (this.agentLifecycleListeners) {
			this.agentLifecycleListeners.clear();
		}
		notifyStopped();
	}

	/**
	 * This exception is thrown when the spawning service of agents is disabled.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpawnDisabledException extends RuntimeException {

		private static final long serialVersionUID = -380402400888610762L;

		/**
		 * @param parentID
		 *            - the identifier of the parent entity that is creating the agent.
		 * @param agentClazz
		 *            - the type of the agent to spawn.
		 */
		public SpawnDisabledException(UUID parentID, Class<? extends Agent> agentClazz) {
			super(MessageFormat.format(Messages.StandardSpawnService_0, parentID, agentClazz));
		}

	}

	/**
	 * This exception is thrown when the spawning service is not running when the killing function on an agent is called.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpawnServiceStopException extends RuntimeException {

		private static final long serialVersionUID = 8104012713598435249L;

		/**
		 * @param agentID
		 *            - the identifier of the agent.
		 */
		public SpawnServiceStopException(UUID agentID) {
			super(MessageFormat.format(Messages.StandardSpawnService_1, agentID));
		}

	}

	/**
	 * This exception is thrown when the agent to spawn is not generated according to a valid SARL specification version.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class InvalidSarlSpecificationException extends RuntimeException {

		private static final long serialVersionUID = -3194494637438344108L;

		/**
		 * @param agentType
		 *            the invalid type of agent.
		 */
		public InvalidSarlSpecificationException(Class<? extends Agent> agentType) {
			super(MessageFormat.format(Messages.StandardSpawnService_2, agentType.getName()));
		}

	}

	/**
	 * This exception is thrown when an agent cannot be spawned.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class CannotSpawnException extends RuntimeException {

		private static final long serialVersionUID = -380402400888610762L;

		/**
		 * @param agentClazz
		 *            - the type of the agent to spawn.
		 * @param cause
		 *            - the cause of the exception.
		 */
		public CannotSpawnException(Class<? extends Agent> agentClazz, Throwable cause) {
			super(MessageFormat.format(Messages.StandardSpawnService_3, agentClazz,
					(cause == null) ? null : cause.getLocalizedMessage()), cause);
		}

	}

	/**
	 * An injection module that is able to inject the parent ID and agent ID when creating an agent.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class JustInTimeAgentInjectionModule extends AbstractModule implements Provider<Agent> {

		private final Injector injector;

		private final Class<? extends Agent> agentType;

		private final UUID parentID;

		private final UUID agentID;

		JustInTimeAgentInjectionModule(Injector injector, Class<? extends Agent> agentType, UUID parentID, UUID agentID) {
			assert injector != null;
			assert agentType != null;
			assert parentID != null;
			this.injector = injector;
			this.agentType = agentType;
			this.parentID = parentID;
			this.agentID = agentID;
		}

		@Override
		public void configure() {
			bind(Agent.class).toProvider(this);
		}

		@Override
		public Agent get() {
			try {
				final BuiltinCapacitiesProvider capacityProvider = this.injector.getInstance(BuiltinCapacitiesProvider.class);
				final Constructor<? extends Agent> constructor = this.agentType.getConstructor(BuiltinCapacitiesProvider.class, UUID.class,
						UUID.class);
				return constructor.newInstance(capacityProvider, this.parentID, this.agentID);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException exception) {
				throw new CannotSpawnException(this.agentType, exception);
			}
		}

	}

}
