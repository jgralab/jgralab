/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.parser;

public enum GreqlTokenType {
	AND("and"), FALSE("false"), NOT("not"), UNDEFINED("undefined"), OR("or"), TRUE(
			"true"), XOR("xor"), AS("as"), MAP("map"), E("E"), EXISTS_ONE(
			"exists!"), EXISTS("exists"), END("end"), FORALL("forall"), FROM(
			"from"), ON("on"), IN("in"), LET("let"), LIST("list"), REC("rec"), REPORT(
			"report"), REPORTSET("reportSet"), REPORTSETN("reportSetN"), REPORTLIST(
			"reportList"), REPORTLISTN("reportListN"), REPORTMAP("reportMap"), REPORTMAPN(
			"reportMapN"), STORE("store"), SET("set"), TUP("tup"), USING(
			"using"), V("V"), WHERE("where"), WITH("with"), QUESTION("?"), EXCL(
			"!"), COLON(":"), COMMA(","), DOT("."), DOTDOT(".."), AT("@"), LPAREN(
			"("), RPAREN(")"), LBRACK("["), RBRACK("]"), LCURLY("{"), RCURLY(
			"}"), EDGESTART("<-"), EDGEEND("->"), EDGE("--"), RARROW("-->"), LARROW(
			"<--"), ARROW("<->"), ASSIGN(":="), EQUAL("="), MATCH("=~"), NOT_EQUAL(
			"<>"), LE("<="), GE(">="), L_T("<"), G_T(">"), DIV("/"), PLUS("+"), MINUS(
			"-"), STAR("*"), MOD("%"), SEMI(";"), CARET("^"), BOR("|"), AMP("&"), SMILEY(
			":-)"), HASH("#"), OUTAGGREGATION("<>--"), INAGGREGATION("--<>"), PATHSYSTEMSTART(
			"-<"), IMPORT("import"), STRING(null), IDENTIFIER(null), DOUBLELITERAL(
			null), LONGLITERAL(null), THISEDGE("thisEdge"), THISVERTEX(
			"thisVertex"), EOF(null), PLUSPLUS("++"), POS_INFINITY(
			"POSITIVE_INFINITY"), NEG_INFINITY("NEGATIVE_INFINITY"), NOT_A_NUMBER(
			"NaN");

	private String text;

	private GreqlTokenType(String t) {
		text = t;
	}

	public String getText() {
		return text;
	}
}
