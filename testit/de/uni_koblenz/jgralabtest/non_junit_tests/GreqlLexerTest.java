package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.List;

import de.uni_koblenz.jgralab.greql.parser.GreqlLexer;
import de.uni_koblenz.jgralab.greql.parser.Token;

public class GreqlLexerTest {

	public static void main(String[] args) {

		// String q =
		// "let 0 020 \"abc\" <-- <>-- -< 1 'de\\'f' fromi 5e1\nin asi");
		// String q = "NaN";
		// String q = "let found := (\n"
		// + "let spec := set(\n"
		// + "tup('kbj210', 'ZBJ003_SORT.MELDSAETZE', set('lfmkbj210dat')),\n"
		// + "tup('zbj003_sort.meldsaetze', 'zbj001', set('lfmzbj003101')),\n"
		// + "tup('zbj001', 'zbj002', set('ZB01_STAB')),\n"
		// +
		// "tup('zbj002', 'kbj211', set('ZB01_Verarb_Vertr', 'ZB01_Vertrag', 'ZB01_VP', 'ZB01_MBK', 'ZB01_Tarif', 'ZB01_BRE')),\n"
		// +
		// "tup('zbj002', 'kbw221_beg.meldung', set('ZB01_Verarb_Vertr', 'ZB01_Vertrag', 'ZB01_VP', 'ZB01_MBK', 'ZB01_Tarif', 'ZB01_BRE')),\n"
		// +
		// "tup('zbj002', 'kbj211', set('ZB01_Verarb_Vertr', 'ZB01_Vertrag', 'ZB01_VP', 'ZB01_MBK', 'ZB01_Tarif', 'ZB01_BRE')),\n"
		// +
		// "tup('kbw221_beg.meldung', 'kbj213_beg.papyrus', set('ZB01_Meld_Beitrdaten', 'ZB01_Meldung')),\n"
		// +
		// "tup('kbw221_beg.meldung', 'kbw223_beg.papyrus', set('ZB01_Meld_Beitrdaten', 'ZB01_Meldung')),\n"
		// +
		// "tup('kbj211', 'kbj213_beg.papyrus', set('ZB01_Meld_Beitrdaten', 'ZB01_Meldung')),\n"
		// +
		// "tup('kbj211', 'kbw223_beg.papyrus', set('ZB01_Meld_Beitrdaten', 'ZB01_Meldung')),\n"
		// +
		// "tup('kbw221_beg.meldung', 'kbb226_beg.zfafehler', set('ZB01_Meldung')),\n"
		// + "tup('kbj211', 'kbb226_beg.zfafehler', set('ZB01_Meldung')),\n"
		// +
		// "tup('kbw221_beg.meldung', 'kbj219_feh.rohdat.beg', set('ZB01_Meldung')),\n"
		// + "tup('kbj211', 'kbj219_feh.rohdat.beg', set('ZB01_Meldung')),\n"
		// + "tup('kbj213_beg.papyrus', 'kbj225_plz.sort', set('lfmkb2113')),\n"
		// + "tup('kbw223_beg.papyrus', 'kbj225_plz.sort', set('lfmkb2113')),\n"
		// +
		// "tup('kbj213_beg.papyrus', 'kbw224_beg.rohdaten', set('lfmkb2113')),\n"
		// +
		// "tup('kbw223_beg.papyrus', 'kbw224_beg.rohdaten', set('lfmkb2113')),\n"
		// +
		// "tup('kbj225_plz.sort', 'kbj214_rohdaten.beg', set('lfmkb225plz', 'lfmkb225sort', 'lfmkbj225s01', 'lfmkbj225s02', 'lfmkbj225s03', 'lfmkbj225s04', 'lfmkbj225s05', 'lfmkbj225s06', 'lfmkbj225s07', 'lfmkbj225s08', 'lfmkbj225s09', 'lfmkbj225s10')),\n"
		// +
		// "tup('kbj217_zb01.speichern', 'kbj213_beg.papyrus', set('ZB01_Meldung')),\n"
		// +
		// "tup('kbj217_zb01.speichern', 'kbw223_beg.papyrus', set('ZB01_Meldung')),\n"
		// +
		// "tup('kbj217_zb01.speichern', 'kbj219_feh.rohdat.beg', set('ZB01_Meldung')),\n"
		// +
		// "tup('kbj217_zb01.speichern', 'kbb226_beg.zfafehler', set('ZB01_Meldung')),\n"
		// +
		// "tup('kbj216_meldung.fehler', 'kbj217_zb01.speichern', set('ZB01_STAB03')),\n"
		// + "tup('kbj211', 'kbj212', set('ZB01_STAB02')),\n"
		// + "tup('kbj211', 'kbw222_beg.xml', set('ZB01_STAB02')),\n"
		// + "tup('kbw221_beg.meldung', 'kbj212', set('ZB01_STAB02')),\n"
		// +
		// "tup('kbw221_beg.meldung', 'kbw222_beg.xml', set('ZB01_STAB02')),\n"
		// + "tup('kbj211', 'kbb220_einspeich.beg', set('lfmkb2115tin')),\n"
		// +
		// "tup('kbw221_beg.meldung', 'kbb220_einspeich.beg', set('lfmkb2115tin')),\n"
		// + "tup('kbj212', 'kbj218', set('lfmkb2118')),\n"
		// + "tup('kbj212', 'kbw228_beg.ftpzfa', set('lfmkb2118')),\n"
		// + "tup('kbw222_beg.xml', 'kbj218', set('lfmkb2118')),\n"
		// + "tup('kbw222_beg.xml', 'kbw228_beg.ftpzfa', set('lfmkb2118')),\n"
		// +
		// "tup('kbw013_zfa.antwort', 'kbj216_meldung.fehler', set('debeka-kv-mz10-*.xml')),\n"
		// + "tup('kbw013_zfa.antwort', 'kbw002', set('IM01')),\n"
		// +
		// "tup('kbw002', 'kbw014_mav.tinvergabe', set('lfmkbw002man', 'lfmkbw002mav'))\n"
		// + ")\n"
		// + "in (\n"
		// + "from sp: spec,\n"
		// + "  dataName: dataNames,\n"
		// + "  data: getDataVertices(dataName),\n"
		// +
		// "    s: source (-->{batch.structure.ContainsUC4Element} -->{batch.structure.StartsSchedulerplan})*,\n"
		// +
		// "    d: dest (-->{batch.structure.ContainsUC4Element} -->{batch.structure.StartsSchedulerplan})*,\n"
		// + "    df: data <--{dataflow.generic.DfFlowsViaData}\n"
		// +
		// "   with s -->{dataflow.generic.DfDataFlowsOut} df -->{dataflow.generic.DfDataFlowsIn} d\n"
		// + "  reportSet\n"
		// + "    df\n"
		// + "  end    \n"
		// + "where\n"
		// + "  source := getSchedulerPlan(sp[0]),\n"
		// + "  dest := getSchedulerPlan(sp[1]),\n"
		// + "  dataNames := sp[2]\n"
		// + ")),\n"
		// + "analysed :=\n"
		// + "unionOfSets(\n"
		// + "from sp: V{batch.structure.Schedulerplan}\n"
		// + "reportSet\n"
		// +
		// "  sp (-->{dataflow.generic.DfDataFlowsOut} | <--{dataflow.generic.DfDataFlowsIn})\n"
		// + "end\n" + ")\n" + "in\n"
		// + "tup(found, analysed, difference(analysed,found))";
		// String q = "list(2..4)";
		String q = "exists! n:list(1..9)@n=5";
		List<Token> l = GreqlLexer.scan(q);

		for (Token t : l) {
			System.out.println(t);
		}
	}
}
