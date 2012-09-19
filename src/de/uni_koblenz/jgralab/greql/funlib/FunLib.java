/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.funlib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.funlib.Function.Category;
import de.uni_koblenz.jgralab.greql.funlib.misc.GreqlQueryFunction;
import de.uni_koblenz.jgralab.greql.funlib.misc.GreqlQueryFunctionWithGraphArgument;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.greql.types.Types;
import de.uni_koblenz.jgralab.greql.types.Undefined;

@SuppressWarnings("deprecation")
public class FunLib {
	private static final Map<String, FunctionInfo> functions;
	private static final TreeSet<String> functionNames;
	private static final Logger logger = JGraLab.getLogger(FunLib.class);

	static {
		functions = new HashMap<String, FunctionInfo>();
		functionNames = new TreeSet<String>();
		// register builtin functions
		logger.fine("Registering builtin functions");
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Abs.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Add.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Ceil.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Cos.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Div.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Exp.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Floor.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Ln.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Mod.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Mul.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Neg.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Round.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Sin.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Sqrt.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Sub.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.Tan.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.ToDouble.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.ToInteger.class);
		register(de.uni_koblenz.jgralab.greql.funlib.artithmetics.ToLong.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitAnd.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitNot.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitOr.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitShl.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitShr.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitUnsignedShr.class);
		register(de.uni_koblenz.jgralab.greql.funlib.bitops.BitXor.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Contains.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.ContainsKey.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.ContainsValue.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Difference.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.EntrySet.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Get.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.IndexOf.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Intersection.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.IsEmpty.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.IsSubSet.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.KeySet.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Pos.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Sort.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.SortByColumn.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.SubCollection.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.TheElement.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.ToList.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.ToSet.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Union.class);
		register(de.uni_koblenz.jgralab.greql.funlib.collections.Values.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Alpha.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Degree.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.base.DegreeFunction.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Depth.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Describe.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Distance.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Edges.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EdgesConnected.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EdgesFrom.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EdgesTo.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EdgeTrace.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EdgeTypeSubgraph.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EdgeSetSubgraph.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.ElementSetSubgraph.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.EndVertex.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.ExtractPaths.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.First.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.FirstEdge.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.FirstIn.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.FirstOut.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.FirstVertex.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.GetEdge.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.GetValue.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.GetVertex.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Id.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.InDegree.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Incidences.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.InIncidences.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.IsAcyclic.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.IsIsolated.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.IsLoop.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.IsReachable.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Last.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.LastIn.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.LastOut.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Leaves.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Next.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.NextGraphElement.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.NextIn.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.NextOut.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Omega.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.OutDegree.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.OutIncidences.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.PathLength.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.PathSystem.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.ReachableVertices.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Slice.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.StartVertex.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.This.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.That.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.TopologicalSort.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.VertexTrace.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.Vertices.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.VertexTypeSubgraph.class);
		register(de.uni_koblenz.jgralab.greql.funlib.graph.VertexSetSubgraph.class);
		register(de.uni_koblenz.jgralab.greql.funlib.logics.And.class);
		register(de.uni_koblenz.jgralab.greql.funlib.logics.Not.class);
		register(de.uni_koblenz.jgralab.greql.funlib.logics.Or.class);
		register(de.uni_koblenz.jgralab.greql.funlib.logics.Xor.class);
		register(de.uni_koblenz.jgralab.greql.funlib.misc.IsDefined.class);
		register(de.uni_koblenz.jgralab.greql.funlib.misc.IsUndefined.class);
		register(de.uni_koblenz.jgralab.greql.funlib.misc.Log.class);
		register(de.uni_koblenz.jgralab.greql.funlib.misc.ValueType.class);
		register(de.uni_koblenz.jgralab.greql.funlib.relations.Equals.class);
		register(de.uni_koblenz.jgralab.greql.funlib.relations.GrEqual.class);
		register(de.uni_koblenz.jgralab.greql.funlib.relations.GrThan.class);
		register(de.uni_koblenz.jgralab.greql.funlib.relations.LeEqual.class);
		register(de.uni_koblenz.jgralab.greql.funlib.relations.LeThan.class);
		register(de.uni_koblenz.jgralab.greql.funlib.relations.Nequals.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.AttributeNames.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.Attributes.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.HasAttribute.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.HasComponent.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.HasType.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.Type.class);
		register(de.uni_koblenz.jgralab.greql.funlib.schema.TypeName.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Count.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Max.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Mean.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Min.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Sdev.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Sum.class);
		register(de.uni_koblenz.jgralab.greql.funlib.statistics.Variance.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.CapitalizeFirst.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.Concat.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.EndsWith.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.Join.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.Length.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.LowerCase.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.ReMatch.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.Split.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.StartsWith.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.Substring.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.ToString.class);
		register(de.uni_koblenz.jgralab.greql.funlib.strings.UpperCase.class);
	}

	private FunLib() {
	}

	private static class SignatureComparator implements Comparator<Signature> {
		private static int checkSpecialCase(Class<?>[] s1, Class<?>[] s2) {
			// evaluate(Number, Number) and evaluate(Comparable,Comparable) is a
			// special case, because it'll sort the comparable version before
			// the number version, which is wrong since comparisons of Integer
			// with Doubles etc. have to be supported.
			if ((s1.length == 2) && (s2.length == 2) && (s1[0] == Number.class)
					&& (s1[1] == Number.class) && (s2[1] == Comparable.class)
					&& (s2[0] == Comparable.class)) {
				return -1;
			} else if ((s1.length == 2) && (s2.length == 2)
					&& (s2[0] == Number.class) && (s2[1] == Number.class)
					&& (s1[1] == Comparable.class)
					&& (s1[0] == Comparable.class)) {
				return 1;
			}
			return 0;
		}

		@Override
		public int compare(Signature s1, Signature s2) {
			int x = checkSpecialCase(s1.parameterTypes, s2.parameterTypes);
			if (x != 0) {
				return x;
			}

			for (int i = 0; i < Math.min(s1.parameterTypes.length,
					s2.parameterTypes.length); i++) {
				Class<?> ps1 = s1.parameterTypes[i];
				Class<?> ps2 = s2.parameterTypes[i];
				if (ps1 == ps2) {
					continue;
				} else if (ps1.isAssignableFrom(ps2)) {
					// ps1 is a super type of ps2
					return 1;
				} else if (ps2.isAssignableFrom(ps1)) {
					// ps2 is a super type of ps1
					return -1;
				}
			}
			// Ok, we cannot decide cause none of the parameter types are
			// subtypes of each other.
			return 0;
		}
	}

	private static class Signature {
		Class<?>[] parameterTypes;
		Method evaluateMethod;

		final boolean matches(Object[] params) {
			if (params.length != parameterTypes.length) {
				return false;
			}
			for (int i = 0; i < params.length; i++) {
				if (!parameterTypes[i].isInstance(params[i])) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(evaluateMethod.getName());
			sb.append("(");
			boolean first = true;
			for (Class<?> pt : parameterTypes) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(pt.getName());
			}
			sb.append(")");
			return sb.toString();
		}
	}

	public static class FunctionInfo {
		String name;
		Class<? extends Function> functionClass;
		Function function;
		Signature[] signatures;
		boolean needsGraphArgument;
		boolean acceptsUndefinedValues;
		boolean needsEvaluatorArgument;

		FunctionInfo(String name, Class<? extends Function> cls) {
			this.name = name;
			functionClass = cls;
			ArrayList<Signature> functionSignatures = new ArrayList<Signature>();
			try {
				function = cls.newInstance();
			} catch (InstantiationException e) {
				throw new GreqlException("Could not instantiate '"
						+ cls.getName() + "'", e);
			} catch (IllegalAccessException e) {
				throw new GreqlException(
						"Could not instantiate '"
								+ cls.getName()
								+ "' (class must be public and needs public default constructor)",
						e);
			}
			needsGraphArgument = cls
					.isAnnotationPresent(NeedsGraphArgument.class);
			acceptsUndefinedValues = cls
					.isAnnotationPresent(AcceptsUndefinedArguments.class);
			needsEvaluatorArgument = cls
					.isAnnotationPresent(NeedsEvaluatorArgument.class);
			registerSignatures(functionSignatures, cls);
			signatures = new Signature[functionSignatures.size()];
			functionSignatures.toArray(signatures);
			Arrays.sort(signatures, new SignatureComparator());
		}

		FunctionInfo(String name, Function func) {
			this.name = name;
			functionClass = func.getClass();
			ArrayList<Signature> functionSignatures = new ArrayList<Signature>();
			function = func;
			needsGraphArgument = functionClass
					.isAnnotationPresent(NeedsGraphArgument.class);
			acceptsUndefinedValues = functionClass
					.isAnnotationPresent(AcceptsUndefinedArguments.class);
			needsEvaluatorArgument = functionClass
					.isAnnotationPresent(NeedsEvaluatorArgument.class);
			registerSignatures(functionSignatures, functionClass);
			signatures = new Signature[functionSignatures.size()];
			functionSignatures.toArray(signatures);
			Arrays.sort(signatures, new SignatureComparator());
		}

		void registerSignatures(ArrayList<Signature> signatures,
				Class<? extends Function> cls) {
			for (Method m : cls.getMethods()) {
				if (Modifier.isPublic(m.getModifiers())
						&& !Modifier.isAbstract(m.getModifiers())
						&& m.getName().equals("evaluate")) {
					logger.finest("\t" + m);
					Signature sig = new Signature();
					sig.evaluateMethod = m;
					sig.parameterTypes = m.getParameterTypes();
					signatures.add(sig);
				}
			}
		}

		public final Function getFunction() {
			return function;
		}

		public final boolean needsGraphArgument() {
			return needsGraphArgument;
		}

		public final boolean acceptsUndefinedValues() {
			return acceptsUndefinedValues;
		}

		public final boolean needsEvaluatorArgument() {
			return needsEvaluatorArgument;
		}

		public final String getHtmlDescription() {
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<html><body><p>GReQL function <font color=\"blue\"><strong>")
					.append(name).append("</strong></font></p><dl>");
			assert (functionClass.getConstructors().length == 1);
			Constructor<?> cons = functionClass.getConstructors()[0];

			Description consDesc = cons.getAnnotation(Description.class);

			for (Signature sig : signatures) {
				Description funDesc = sig.evaluateMethod
						.getAnnotation(Description.class);
				if (funDesc == null) {
					funDesc = consDesc;
				}
				if (funDesc == null) {
					continue;
				}
				Class<?> ret = sig.evaluateMethod.getReturnType();
				String returnType = Types.getGreqlTypeName(ret);

				boolean acceptsType = sig.parameterTypes[sig.parameterTypes.length - 1] == TypeCollection.class;
				sb.append("<dt><strong><font color=\"purple\">")
						.append(returnType).append(" <font color=\"blue\">")
						.append(name).append("</font></strong>");
				if (acceptsType) {
					sb.append(" { <font color=\"#008000\">types...</font> } ");
				}
				sb.append("(");
				String delim = "";
				int i = 0;
				for (String p : funDesc.params()) {
					if (i == 0 && needsGraphArgument) {
						// don't show Graph argument
						++i;
						continue;
					}
					if (i == sig.parameterTypes.length - 1 && acceptsType) {
						// don't show TypeCollection argument
						++i;
						continue;
					}
					Class<?> cls = sig.parameterTypes[i++];
					String type = Types.getGreqlTypeName(cls);
					sb.append(delim).append("<strong><font color=\"purple\">")
							.append(type).append("</font></strong> ").append(p);
					delim = ", ";
				}
				sb.append(")</dt><dd>").append(funDesc.description())
						.append("</dd>");
			}
			sb.append("</dl></body></html>");
			return sb.toString();
		}
	}

	public static final boolean contains(String name) {
		return functions.containsKey(name);
	}

	private static final String getFunctionName(String className) {
		return Character.toLowerCase(className.charAt(0))
				+ className.substring(1);
	}

	private static final String getFunctionName(Class<? extends Function> cls) {
		return getFunctionName(cls.getSimpleName());
	}

	public static final String getArgumentAsString(Object arg) {
		if (arg == null) {
			arg = Undefined.UNDEFINED;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Types.getGreqlTypeName(arg));
		if (arg instanceof String) {
			sb.append(": ").append('"')
					.append(arg.toString().replace("\"", "\\\"")).append('"');
		} else if (!(arg instanceof Graph) && !(arg instanceof Undefined)) {
			sb.append(": ").append(arg);
		}
		return sb.toString();
	}

	public static final Object apply(PrintStream os, String name,
			Object... args) {
		assert (name != null) && (name.length() >= 1);
		assert args != null;
		assert validArgumentTypes(args);
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (args.length == 0) {
			sb.append("()");
		} else {
			String delim = "(";
			for (Object arg : args) {
				sb.append(delim).append(getArgumentAsString(arg));
				delim = ", ";
			}
			sb.append(")");
		}
		os.print(sb);
		Object result = apply(name, args);
		os.println(" -> " + getArgumentAsString(result));
		return result;
	}

	public static final Object apply(FunctionInfo fi, Object... args) {
		assert fi != null;
		if (!fi.acceptsUndefinedValues) {
			for (Object arg : args) {
				if ((arg == null) || (arg == Undefined.UNDEFINED)) {
					return Undefined.UNDEFINED;
				}
			}
		}
		for (Signature sig : fi.signatures) {
			if (sig.matches(args)) {
				try {
					Object result = sig.evaluateMethod
							.invoke(fi.function, args);
					assert Types.isValidGreqlValue(result);
					return result == null ? Undefined.UNDEFINED : result;
				} catch (IllegalArgumentException e) {
					if (e.getCause() instanceof GreqlException) {
						throw (GreqlException) e.getCause();
					} else {
						throw new GreqlException(e.getMessage(), e.getCause());
					}
				} catch (IllegalAccessException e) {
					if (e.getCause() instanceof GreqlException) {
						throw (GreqlException) e.getCause();
					} else {
						throw new GreqlException(e.getMessage(), e.getCause());
					}
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof GreqlException) {
						throw (GreqlException) e.getCause();
					} else {
						throw new GreqlException(e.getMessage(), e.getCause());
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Function '").append(fi.name)
				.append("' not defined for argument types (");
		String delim = "";
		for (Object arg : args) {
			sb.append(delim).append(Types.getGreqlTypeName(arg));
			delim = ", ";
		}
		sb.append(")");
		throw new GreqlException(sb.toString());
	}

	public static final Object apply(String name, Object... args) {
		assert (name != null) && (name.length() >= 1);
		assert args != null;
		assert validArgumentTypes(args);
		FunctionInfo fi = getFunctionInfo(name);
		if (fi == null) {
			throw new GreqlException("Call to unknown function '" + name + "'");
		}
		return apply(fi, args);
	}

	private static final boolean validArgumentTypes(Object[] args) {
		for (Object arg : args) {
			if (!Types.isValidGreqlValue(arg)) {
				throw new GreqlException("Type unknown to GReQL: "
						+ arg.getClass().getName() + ", value: " + arg);
			}
		}
		return true;
	}

	public static final void register(String className)
			throws ClassNotFoundException {
		Class<?> cls = Class.forName(className);
		register(cls.asSubclass(Function.class));
	}

	public static final void register(Class<? extends Function> cls) {
		int mods = cls.getModifiers();
		if (Modifier.isAbstract(mods) || Modifier.isInterface(mods)
				|| !Modifier.isPublic(mods)) {
			return;
		}
		String name = getFunctionName(cls);
		FunctionInfo fn = functions.get(name);
		if (fn != null) {
			if (fn.functionClass == cls) {
				// ok, same class is already registered
				return;
			}
			throw new GreqlException("Duplicate function name '" + name + "'");
		}
		logger.fine("Registering " + cls.getName() + " as '" + name + "'");
		functions.put(name, new FunctionInfo(name, cls));
		functionNames.add(name);
	}

	public static final void registerGreqlQueryFunction(GreqlQuery query,
			boolean needsGraphArgument, long costs, long cardinality,
			double selectivity) {
		String name = query.getName();
		if (name == null) {
			throw new GreqlException(
					"The name of a GReQL function must not be null!");
		}
		if (!name.matches("^\\w+$")) {
			throw new GreqlException("Invalid GReQL function name '" + name
					+ "'. Only word characters are allowed.");
		}
		FunctionInfo fn = getFunctionInfo(name);
		if (fn != null) {
			throw new GreqlException("Duplicate function name '" + name + "'");
		}

		logger.fine("Registering GReQL function as '" + name + "'");

		GreqlQueryFunction greqlFunction = needsGraphArgument ? new GreqlQueryFunctionWithGraphArgument(
				query, costs, cardinality, selectivity)
				: new GreqlQueryFunction(query, costs, cardinality, selectivity);

		functions.put(name, new FunctionInfo(name, greqlFunction));
		functionNames.add(name);
	}

	public static final void removeGreqlQueryFunction(String name) {
		FunctionInfo fn = getFunctionInfo(name);
		if (fn.getFunction() instanceof GreqlQueryFunction) {
			functions.remove(name);
			functionNames.remove(name);
		} else {
			throw new IllegalArgumentException("Function " + name
					+ " is not a GreqlQueryFunction.");
		}
	}

	public static final Set<String> getFunctionNames() {
		return Collections.unmodifiableSet(functionNames);
	}

	public static final FunctionInfo getFunctionInfo(String functionName) {
		return functions.get(functionName);
	}

	public static void generateLaTeXFunctionDocs(String fileName)
			throws IOException {
		LaTeXFunctionDocsGenerator docGen = new LaTeXFunctionDocsGenerator(
				fileName, functions);
		docGen.generate();
	}

	private static class LaTeXFunctionDocsGenerator {
		private final BufferedWriter bw;
		private final Map<Category, SortedMap<String, AnnotationInfo>> cat2funs = new HashMap<Function.Category, SortedMap<String, AnnotationInfo>>();

		LaTeXFunctionDocsGenerator(String fileName,
				final Map<String, FunctionInfo> funs) throws IOException {
			bw = new BufferedWriter(new FileWriter(fileName));
			fillCat2Funs(funs);
		}

		private class AnnotationInfo {
			String name;
			String constructorDescription;
			SignatureInfo[] signatureInfos;
		}

		private class SignatureInfo {
			String description;
			String[] params;
			Signature signature;

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append("### SignatureInfo ###\n");
				sb.append(signature.toString());
				sb.append('\n');
				if (params != null) {
					sb.append(Arrays.toString(params));
				}
				sb.append('\n');
				sb.append(description);
				return sb.toString();
			}
		}

		private void fillCat2Funs(final Map<String, FunctionInfo> funs) {
			for (Entry<String, FunctionInfo> e : funs.entrySet()) {

				Class<?> funClass = e.getValue().getFunction().getClass();
				assert (funClass.getConstructors().length == 1);
				Constructor<?> cons = funClass.getConstructors()[0];

				String name = e.getKey();
				String constructorDescription = null;

				Description consAnno = cons.getAnnotation(Description.class);
				if (consAnno != null) {
					constructorDescription = consAnno.description();
				}

				HashMap<Category, ArrayList<SignatureInfo>> cat2sig = new HashMap<Function.Category, ArrayList<SignatureInfo>>();
				int methodCount = e.getValue().signatures.length;
				for (int i = 0; i < methodCount; i++) {
					createSigInfo(e, consAnno, cat2sig, i);
				}

				for (Category cat : cat2sig.keySet()) {
					SortedMap<String, AnnotationInfo> m = cat2funs.get(cat);
					if (m == null) {
						m = new TreeMap<String, AnnotationInfo>();
						cat2funs.put(cat, m);
					}
					AnnotationInfo aninfo = new AnnotationInfo();
					aninfo.name = name;
					aninfo.constructorDescription = constructorDescription;
					aninfo.signatureInfos = cat2sig.get(cat).toArray(
							new SignatureInfo[] {});
					m.put(aninfo.name, aninfo);
					cat2funs.put(cat, m);
				}
			}
		}

		private void createSigInfo(Entry<String, FunctionInfo> e,
				Description consAnno,
				HashMap<Category, ArrayList<SignatureInfo>> cat2sig, int i) {
			SignatureInfo si = new SignatureInfo();
			si.signature = e.getValue().signatures[i];
			Method m = si.signature.evaluateMethod;

			Description des = m.getAnnotation(Description.class);
			if ((des == null) || (des.params() == null)) {
				si.params = consAnno.params();
			} else {
				si.description = des.description();
				si.params = des.params();
			}
			if ((des != null) && (des.categories() != null)) {
				for (Category cat : des.categories()) {
					if (!cat2sig.containsKey(cat)) {
						cat2sig.put(cat, new ArrayList<SignatureInfo>());
					}
					cat2sig.get(cat).add(si);
				}
			} else {
				for (Category cat : consAnno.categories()) {
					if (!cat2sig.containsKey(cat)) {
						cat2sig.put(cat, new ArrayList<SignatureInfo>());
					}
					cat2sig.get(cat).add(si);
				}
			}
		}

		/**
		 * Set to true to generate a complete latex doc that can be compiled
		 * standalone. Useful when changing this generator...
		 */
		private final boolean STANDALONE = false;

		void generate() throws IOException {
			try {
				if (STANDALONE) {
					write("\\documentclass{article}");
					newLine();
					write("\\begin{document}");
					newLine();
				}
				write("\\twocolumn");
				newLine();
				newLine();
				for (Category cat : Category.values()) {
					if (cat2funs.get(cat) != null) {
						generateCategoryDocs(cat);
					}
				}
				if (STANDALONE) {
					write("\\end{document}");
					newLine();
				}
			} finally {
				bw.close();
			}
		}

		private void write(String... strings) throws IOException {
			for (String s : strings) {
				bw.write(s);
			}
		}

		private void newLine() throws IOException {
			bw.newLine();
		}

		private void generateCategoryDocs(Category cat) throws IOException {
			String heading = cat.toString().toLowerCase().replace('_', ' ');
			heading = heading.substring(0, 1).toUpperCase()
					.concat(heading.substring(1));
			newLine();
			write("\\subsection{" + heading + "}");
			newLine();

			SortedMap<String, AnnotationInfo> funs = cat2funs.get(cat);
			for (AnnotationInfo e : funs.values()) {
				generateFunctionDocs(e);
			}
		}

		private void generateFunctionDocs(AnnotationInfo info)
				throws IOException {
			newLine();
			write("\\paragraph*{" + info.name + ".}");
			if (info.constructorDescription != null) {
				write(info.constructorDescription);
			}
			newLine();
			System.out.println("Generating docs for function: " + info.name);
			generateSignatures(info);

			newLine();

		}

		private void generateSignatures(AnnotationInfo info) throws IOException {
			write("\\begin{description}");
			for (SignatureInfo sig : info.signatureInfos) {
				write("\\item [$" + info.name + ":$ ] $");
				for (int i = 0; i < sig.signature.parameterTypes.length; i++) {
					if (i != 0) {
						write(" \\times ");
					}

					write(Types
							.getGreqlTypeName(sig.signature.parameterTypes[i]));
					write("\\; ");
					write(sig.params[i]);
				}
				write(" \\longrightarrow ");
				write(Types.getGreqlTypeName(sig.signature.evaluateMethod
						.getReturnType()));
				write("$");
				if (sig.description != null) {
					write("\\\\");
					write(sig.description);
				}
			}
			write("\\end{description}");
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out
					.println("Generate a LaTeX documentation for all known GReQL functions.");
			System.out.println("Usage: java FunLib /path/to/fundocs.tex");
		} else {
			generateLaTeXFunctionDocs(args[0]);
		}
	}
}
