/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2017 the original authors or authors.
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

package io.sarl.lang.ui.images;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.xtend.ide.labeling.XtendImages;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper;

/**
 * Providers of images for the SARL IDE.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class SARLImages extends XtendImages {

	@Inject
	private IImageDescriptorHelper imageHelper;

	/** Replies the image descriptor for the given element.
	 *
	 * @param type - the type of the SARL element, or <code>null</code> if unknown.
	 * @param isInner - indicates if the element is inner.
	 * @param isInInterfaceOrAnnotation - indicates if the element is defined inside an interface or an annotation.
	 * @param flags - the adornments.
	 * @param useLightIcons - indicates of light icons should be used.
	 * @return the image descriptor.
	 */
	protected ImageDescriptor getTypeImageDescriptor(
			SarlElementType type,
			boolean isInner, boolean isInInterfaceOrAnnotation, int flags, boolean useLightIcons) {
		if (type != null) {
			switch (type) {
			case AGENT:
				return this.imageHelper.getImageDescriptor("sarl-agent.png"); //$NON-NLS-1$
			case BEHAVIOR:
				return this.imageHelper.getImageDescriptor("sarl-behavior.png"); //$NON-NLS-1$
			case CAPACITY:
				return this.imageHelper.getImageDescriptor("sarl-capacity.png"); //$NON-NLS-1$
			case SKILL:
				return this.imageHelper.getImageDescriptor("sarl-skill.png"); //$NON-NLS-1$
			case EVENT:
				return this.imageHelper.getImageDescriptor("sarl-event.png"); //$NON-NLS-1$
			case BEHAVIOUR_UNIT:
				return this.imageHelper.getImageDescriptor("sarl-behavior-unit.png"); //$NON-NLS-1$
			default:
			}
		}
		return JavaElementImageProvider.getTypeImageDescriptor(isInner, isInInterfaceOrAnnotation, flags, useLightIcons);
	}

	/** Replies the image descriptor for the "agents".
	 *
	 * @param visibility - the visibility of the agent.
	 * @param flags - the mark flags. See {@link JavaElementImageDescriptor#setAdornments(int)} for
	 *                a description of the available flags.
	 * @return the image descriptor for the agents.
	 */
	public ImageDescriptor forAgent(JvmVisibility visibility, int flags) {
		return getDecorated(getTypeImageDescriptor(
				SarlElementType.AGENT, false, false, toFlags(visibility), false), flags);
	}

	/** Replies the image descriptor for the "behaviors".
	 *
	 * @param visibility - the visibility of the behavior.
	 * @param flags - the mark flags. See {@link JavaElementImageDescriptor#setAdornments(int)} for
	 *                a description of the available flags.
	 * @return the image descriptor for the behaviors.
	 */
	public ImageDescriptor forBehavior(JvmVisibility visibility, int flags) {
		return getDecorated(getTypeImageDescriptor(
				SarlElementType.BEHAVIOR, false, false, toFlags(visibility), false), flags);
	}

	/** Replies the image descriptor for the "capacities".
	 *
	 * @param visibility - the visibility of the capacity.
	 * @param flags - the mark flags. See {@link JavaElementImageDescriptor#setAdornments(int)} for
	 *                a description of the available flags.
	 * @return the image descriptor for the capacities.
	 */
	public ImageDescriptor forCapacity(JvmVisibility visibility, int flags) {
		return getDecorated(getTypeImageDescriptor(
				SarlElementType.CAPACITY, false, false, toFlags(visibility), false), flags);
	}

	/** Replies the image descriptor for the "skills".
	 *
	 * @param visibility - the visibility of the skill.
	 * @param flags - the mark flags. See {@link JavaElementImageDescriptor#setAdornments(int)} for
	 *                a description of the available flags.
	 * @return the image descriptor for the skills.
	 */
	public ImageDescriptor forSkill(JvmVisibility visibility, int flags) {
		return getDecorated(getTypeImageDescriptor(
				SarlElementType.SKILL, false, false, toFlags(visibility), false), flags);
	}

	/** Replies the image descriptor for the "events".
	 *
	 * @param visibility - the visibility of the event.
	 * @param flags - the mark flags. See {@link JavaElementImageDescriptor#setAdornments(int)} for
	 *                a description of the available flags.
	 * @return the image descriptor for the events.
	 */
	public ImageDescriptor forEvent(JvmVisibility visibility, int flags) {
		return getDecorated(getTypeImageDescriptor(
				SarlElementType.EVENT, false, false, toFlags(visibility), false), flags);
	}

	/** Replies the image descriptor for the "behavior units".
	 *
	 * @return the image descriptor for the behavior units.
	 */
	public ImageDescriptor forBehaviorUnit() {
		return getDecorated(getTypeImageDescriptor(
				SarlElementType.BEHAVIOUR_UNIT, false, false,
				toFlags(JvmVisibility.PUBLIC), false), 0);
	}

	/** Replies the image descriptor for the "SARL script".
	 *
	 * @return the image descriptor for the SARL script.
	 */
	@Override
	public ImageDescriptor forFile() {
		return this.imageHelper.getImageDescriptor("sarl-file.png"); //$NON-NLS-1$
	}

	/** Replies the image descriptor for the "capacity uses".
	 *
	 * @return the image descriptor for the capacity uses.
	 */
	public ImageDescriptor forCapacityUses() {
		return forImportContainer();
	}

	/** Replies the image descriptor for the "capacity use".
	 *
	 * @return the image descriptor for the capacity use.
	 */
	public ImageDescriptor forCapacityUse() {
		return forImport();
	}

	/** Replies the image descriptor for the "capacity requirements".
	 *
	 * @return the image descriptor for the capacity requirements.
	 */
	public ImageDescriptor forCapacityRequirements() {
		return forImportContainer();
	}

	/** Replies the image descriptor for the "capacity requirement".
	 *
	 * @return the image descriptor for the capacity requirement.
	 */
	public ImageDescriptor forCapacityRequirement() {
		return forCapacity(JvmVisibility.PRIVATE, 0);
	}

	/**
	 * Type of the SARL element.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	protected enum SarlElementType {
		/** Agent.
		 */
		AGENT,
		/** Behavior.
		 */
		BEHAVIOR,
		/** Capacity.
		 */
		CAPACITY,
		/** Event.
		 */
		EVENT,
		/** Skill.
		 */
		SKILL,
		/** Behavior unit.
		 */
		BEHAVIOUR_UNIT;
	}

}