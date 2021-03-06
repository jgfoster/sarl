/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014-2015 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.tests.kernel.services.jdk.contextspace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.inject.Injector;
import io.janusproject.kernel.services.jdk.contextspace.Context;
import io.janusproject.kernel.services.jdk.contextspace.SpaceRepository;
import io.janusproject.services.contextspace.SpaceRepositoryListener;
import io.janusproject.services.distributeddata.DMap;
import io.janusproject.services.distributeddata.DistributedDataStructureService;
import io.janusproject.services.logging.LogService;
import io.janusproject.tests.testutils.AbstractJanusTest;
import io.janusproject.util.TwoStepConstruction;
import javassist.Modifier;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.sarl.core.SpaceCreated;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedCollection;
import io.sarl.tests.api.Nullable;
import io.sarl.util.Collections3;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class ContextTest extends AbstractJanusTest {

	@Nullable
	private UUID contextId;

	@Nullable
	private UUID spaceId;

	@Nullable
	private Map<UUID, OpenEventSpace> spaces;

	@Nullable
	private SpaceRepository spaceRepository;

	@Nullable
	private Context context;

	@Nullable
	private SpaceRepositoryListener spaceListener;

	@Nullable
	private SpaceRepositoryListener privateListener;

	@Before
	public void setUp() throws Exception {
		this.contextId = UUID.randomUUID();
		this.spaceId = UUID.randomUUID();
		this.spaces = new HashMap<>();
		this.spaceListener = Mockito.mock(SpaceRepositoryListener.class);
		this.spaceRepository = Mockito.mock(SpaceRepository.class);
		Mockito.when(this.spaceRepository.createSpace(ArgumentMatchers.any(SpaceID.class), ArgumentMatchers.any(Class.class)))
				.thenAnswer(new Answer<Space>() {
					@Override
					public Space answer(InvocationOnMock invocation) throws Throwable {
						OpenEventSpace space = Mockito.mock(OpenEventSpace.class);
						Mockito.when(space.getSpaceID()).thenReturn((SpaceID) invocation.getArguments()[0]);
						ContextTest.this.spaces.put(((SpaceID) invocation.getArguments()[0]).getID(), space);
						assert (ContextTest.this.privateListener != null);
						ContextTest.this.privateListener.spaceCreated(space, true);
						return space;
					}
				});
		Mockito.when(this.spaceRepository.getOrCreateSpaceWithSpec(ArgumentMatchers.any(SpaceID.class), ArgumentMatchers.any(Class.class)))
				.thenAnswer(new Answer<Space>() {
					@Override
					public Space answer(InvocationOnMock invocation) throws Throwable {
						for (Space s : ContextTest.this.spaces.values()) {
							if (s.getSpaceID().equals(invocation.getArguments()[0])) {
								return s;
							}
						}
						OpenEventSpace space = Mockito.mock(OpenEventSpace.class);
						Mockito.when(space.getSpaceID()).thenReturn((SpaceID) invocation.getArguments()[0]);
						ContextTest.this.spaces.put(((SpaceID) invocation.getArguments()[0]).getID(), space);
						assert (ContextTest.this.privateListener != null);
						ContextTest.this.privateListener.spaceCreated(space, true);
						return space;
					}
				});
		Mockito.when(this.spaceRepository.getOrCreateSpaceWithID(ArgumentMatchers.any(SpaceID.class), ArgumentMatchers.any(Class.class)))
				.thenAnswer(new Answer<Space>() {
					@Override
					public Space answer(InvocationOnMock invocation) throws Throwable {
						for (Space s : ContextTest.this.spaces.values()) {
							if (s.getSpaceID().equals(invocation.getArguments()[0])) {
								return s;
							}
						}
						OpenEventSpace space = Mockito.mock(OpenEventSpace.class);
						Mockito.when(space.getSpaceID()).thenReturn((SpaceID) invocation.getArguments()[0]);
						ContextTest.this.spaces.put(((SpaceID) invocation.getArguments()[0]).getID(), space);
						assert (ContextTest.this.privateListener != null);
						ContextTest.this.privateListener.spaceCreated(space, true);
						return space;
					}
				});
		Mockito.when(this.spaceRepository.getSpaces(ArgumentMatchers.any(Class.class)))
				.thenAnswer(new Answer<SynchronizedCollection<? extends Space>>() {
					@Override
					public SynchronizedCollection<? extends Space> answer(InvocationOnMock invocation) throws Throwable {
						Collection<Space> c = new ArrayList<>();
						for (OpenEventSpace space : ContextTest.this.spaces.values()) {
							if (invocation.getArguments()[0].equals(space.getSpaceID().getSpaceSpecification())) {
								c.add(space);
							}
						}
						return Collections3.synchronizedCollection(c, c);
					}
				});
		Mockito.when(this.spaceRepository.getSpace(ArgumentMatchers.any(SpaceID.class))).thenAnswer(new Answer<Space>() {
			@Override
			public Space answer(InvocationOnMock invocation) throws Throwable {
				for (OpenEventSpace space : ContextTest.this.spaces.values()) {
					if (invocation.getArguments()[0].equals(space.getSpaceID())) {
						return space;
					}
				}
				return null;
			}
		});
		Mockito.when(this.spaceRepository.getSpaces())
				.thenReturn(Collections3.synchronizedCollection((Collection) this.spaces.values(), this.spaces));

		Context.DefaultSpaceRepositoryFactory spaceRepoFactory = new TestFactory(this);
		spaceRepoFactory = Mockito.spy(spaceRepoFactory);

		this.context = new Context(this.contextId, this.spaceId, spaceRepoFactory, this.spaceListener);
		this.context.postConstruction();
	}

	@Test
	public void twoStepConstruction() throws Exception {
		TwoStepConstruction annotation = Context.class.getAnnotation(TwoStepConstruction.class);
		assertNotNull(annotation);
		for (String name : annotation.names()) {
			for (Method method : Context.class.getMethods()) {
				if (name.equals(method.getName())) {
					assertTrue(Modifier.isPackage(method.getModifiers()) || Modifier.isPublic(method.getModifiers()));
					break;
				}
			}
		}
	}

	@Test
	public void postConstruction() {
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Class> argument2 = ArgumentCaptor.forClass(Class.class);
		Mockito.verify(this.spaceRepository, new Times(1)).createSpace(argument1.capture(), argument2.capture());
		assertEquals(this.contextId, argument1.getValue().getContextID());
		assertEquals(this.spaceId, argument1.getValue().getID());
	}

	@Test
	public void getID() {
		assertSame(this.contextId, this.context.getID());
	}

	@Test
	public void getDefaultSpace() {
		OpenEventSpace space = this.context.getDefaultSpace();
		assertNotNull(space);
		assertEquals(this.contextId, space.getSpaceID().getContextID());
		assertEquals(this.spaceId, space.getSpaceID().getID());
	}

	@Test
	public void createSpace() {
		Collection<? extends Space> c;
		UUID id = UUID.randomUUID();
		OpenEventSpace space = this.context.createSpace(OpenEventSpaceSpecification.class, id);
		//
		assertNotNull(space);
		assertEquals(id, space.getSpaceID().getID());
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for (Space sp : c) {
			ids.remove(sp.getSpaceID().getID());
		}
		assertTrue(ids.isEmpty());
		//
		assertNotNull(this.privateListener);
		ArgumentCaptor<Space> argument1 = ArgumentCaptor.forClass(Space.class);
		ArgumentCaptor<Boolean> argument2 = ArgumentCaptor.forClass(Boolean.class);
		// CAUTION: invoked two times due to the default space and the created space.
		Mockito.verify(this.spaceListener, new Times(2)).spaceCreated(argument1.capture(), argument2.capture());
		assertSame(space, argument1.getValue());
		assertTrue(argument2.getValue());
		//
		OpenEventSpace defSpace = this.spaces.get(this.spaceId);
		assertNotNull(defSpace);
		ArgumentCaptor<Event> argument3 = ArgumentCaptor.forClass(Event.class);
		Mockito.verify(defSpace, new Times(1)).emit(argument3.capture());
		assertThat(argument3.getValue(), new IsInstanceOf(SpaceCreated.class));
		assertEquals(id, ((SpaceCreated) argument3.getValue()).spaceID.getID());
	}

	@Test
	public void getOrCreateSpaceWithSpec() {
		Collection<? extends Space> c;
		UUID id = UUID.randomUUID();
		OpenEventSpace space = this.context.getOrCreateSpaceWithSpec(OpenEventSpaceSpecification.class, id);
		//
		assertNotNull(space);
		assertEquals(id, space.getSpaceID().getID());
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for (Space sp : c) {
			ids.remove(sp.getSpaceID().getID());
		}
		assertTrue(ids.isEmpty());
		//
		assertNotNull(this.privateListener);
		ArgumentCaptor<Space> argument1 = ArgumentCaptor.forClass(Space.class);
		ArgumentCaptor<Boolean> argument2 = ArgumentCaptor.forClass(Boolean.class);
		// CAUTION: invoked two times due to the default space and the created space.
		Mockito.verify(this.spaceListener, new Times(2)).spaceCreated(argument1.capture(), argument2.capture());
		assertSame(space, argument1.getValue());
		assertTrue(argument2.getValue());
		//
		OpenEventSpace defSpace = this.spaces.get(this.spaceId);
		assertNotNull(defSpace);
		ArgumentCaptor<Event> argument3 = ArgumentCaptor.forClass(Event.class);
		Mockito.verify(defSpace, new Times(1)).emit(argument3.capture());
		assertThat(argument3.getValue(), new IsInstanceOf(SpaceCreated.class));
		assertEquals(id, ((SpaceCreated) argument3.getValue()).spaceID.getID());
		//
		OpenEventSpace space2 = this.context.getOrCreateSpaceWithSpec(OpenEventSpaceSpecification.class, id);
		assertSame(space, space2);
	}

	@Test
	public void getOrCreateSpaceWithID() {
		Collection<? extends Space> c;
		UUID id = UUID.randomUUID();
		OpenEventSpace space = this.context.getOrCreateSpaceWithID(id, OpenEventSpaceSpecification.class);
		//
		assertNotNull(space);
		assertEquals(id, space.getSpaceID().getID());
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for (Space sp : c) {
			ids.remove(sp.getSpaceID().getID());
		}
		assertTrue(ids.isEmpty());
		//
		assertNotNull(this.privateListener);
		ArgumentCaptor<Space> argument1 = ArgumentCaptor.forClass(Space.class);
		ArgumentCaptor<Boolean> argument2 = ArgumentCaptor.forClass(Boolean.class);
		// CAUTION: invoked two times due to the default space and the created space.
		Mockito.verify(this.spaceListener, new Times(2)).spaceCreated(argument1.capture(), argument2.capture());
		assertSame(space, argument1.getValue());
		assertTrue(argument2.getValue());
		//
		OpenEventSpace defSpace = this.spaces.get(this.spaceId);
		assertNotNull(defSpace);
		ArgumentCaptor<Event> argument3 = ArgumentCaptor.forClass(Event.class);
		Mockito.verify(defSpace, new Times(1)).emit(argument3.capture());
		assertThat(argument3.getValue(), new IsInstanceOf(SpaceCreated.class));
		assertEquals(id, ((SpaceCreated) argument3.getValue()).spaceID.getID());
		//
		OpenEventSpace space2 = this.context.getOrCreateSpaceWithID(id, OpenEventSpaceSpecification.class);
		assertSame(space, space2);
	}

	@Test
	public void getSpaces() {
		Collection<? extends Space> c;
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(1, c.size());
		assertEquals(this.spaceId, c.iterator().next().getSpaceID().getID());
		//
		UUID id = UUID.randomUUID();
		this.context.createSpace(OpenEventSpaceSpecification.class, id);
		//
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for (Space space : c) {
			ids.remove(space.getSpaceID().getID());
		}
		assertTrue(ids.isEmpty());
	}

	@Test
	public void getSpacesClass() {
		Collection<OpenEventSpace> c;
		c = this.context.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(c);
		assertEquals(1, c.size());
		assertEquals(this.spaceId, c.iterator().next().getSpaceID().getID());
		//
		UUID id = UUID.randomUUID();
		this.context.createSpace(OpenEventSpaceSpecification.class, id);
		//
		c = this.context.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for (Space space : c) {
			ids.remove(space.getSpaceID().getID());
		}
		assertTrue(ids.isEmpty());
	}

	@Test
	public void getSpaceUUID() {
		assertNotNull(this.context.getSpace(this.spaceId));
		assertNull(this.context.getSpace(UUID.randomUUID()));
	}

	@Test
	public void destroy() {
		this.context.destroy();
		Mockito.verify(this.spaceRepository, new Times(1)).destroy();
	}

	public static class TestFactory extends Context.DefaultSpaceRepositoryFactory {
		private final ContextTest test;

		public TestFactory(ContextTest test) {
			super(mock(Injector.class), mock(DistributedDataStructureService.class), mock(LogService.class));
			this.test = test;
		}

		@Override
		protected SpaceRepository newInstanceWithPrivateSpaceListener(Context context, String distributedSpaceSetName,
				SpaceRepositoryListener listener) {
			this.test.privateListener = listener;
			return this.test.spaceRepository;
		}
	}

}
