/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.gretl;

import java.util.Collection;

import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;

public class CopyDomain extends Transformation<Domain> {
	private Domain sourceDomain;

	public CopyDomain(final Context c, final Domain sourceDomain) {
		super(c);
		this.sourceDomain = sourceDomain;
	}

	public static CopyDomain parseAndCreate(ExecuteTransformation et) {
		Domain d = et.matchDomain();
		return new CopyDomain(et.context, d);
	}

	@Override
	protected Domain transform() {
		switch (context.phase) {
		case SCHEMA:
			if (sourceDomain instanceof BooleanDomain) {
				return getBooleanDomain();
			} else if (sourceDomain instanceof DoubleDomain) {
				return getDoubleDomain();
			} else if (sourceDomain instanceof IntegerDomain) {
				return getIntegerDomain();
			} else if (sourceDomain instanceof LongDomain) {
				return getLongDomain();
			} else if (sourceDomain instanceof StringDomain) {
				return getStringDomain();
			} else if (sourceDomain instanceof ListDomain) {
				return new CreateListDomain(context, new CopyDomain(context,
						((ListDomain) sourceDomain).getBaseDomain()).execute())
						.execute();
			} else if (sourceDomain instanceof SetDomain) {
				return new CreateSetDomain(context, new CopyDomain(context,
						((SetDomain) sourceDomain).getBaseDomain()).execute())
						.execute();
			} else if (sourceDomain instanceof MapDomain) {
				MapDomain source = (MapDomain) sourceDomain;
				return new CreateMapDomain(context, new CopyDomain(context,
						source.getKeyDomain()).execute(), new CopyDomain(
						context, source.getValueDomain()).execute()).execute();
			} else if (sourceDomain instanceof RecordDomain) {
				RecordDomain source = (RecordDomain) sourceDomain;
				Collection<RecordComponent> coll = source.getComponents();
				RecordComponent[] comps = new RecordComponent[coll.size()];
				int i = 0;
				for (RecordComponent curComp : coll) {
					comps[i] = new RecordComponent(curComp.getName(),
							new CopyDomain(context, curComp.getDomain())
									.execute());
					i++;
				}
				return new CreateRecordDomain(context,
						source.getQualifiedName(), comps).execute();
			} else if (sourceDomain instanceof EnumDomain) {
				EnumDomain source = (EnumDomain) sourceDomain;
				new CreateEnumDomain(context, source.getQualifiedName(), source
						.getConsts().toArray(new String[0])).execute();
			} else {
				throw new GReTLException(context, "Unknown Domain '"
						+ sourceDomain + "'.");
			}
		case GRAPH:
			return domain(sourceDomain.getQualifiedName());
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

}
