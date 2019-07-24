package summary;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import insomnia.automaton.AutomatonException;
import insomnia.query.CouchQueryFactory;
import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexAutomaton.Builder.BuilderException;
import insomnia.regex.automaton.RegexToAutomatonConverter;
import insomnia.regex.element.IElement;
import insomnia.summary.CouchSummaryFactory;
import insomnia.summary.Summary;

@SuppressWarnings("unused")
public class TestCouchSummary
{

	public static void main(String[] argv) throws Exception
	{
		String regex = "a.b+.c?";
		// Automate
		RegexParser parser = new RegexParser();
		IElement elements = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
		RegexAutomaton automaton = RegexToAutomatonConverter.convert(elements);

		// Résumé
		CouchSummaryFactory factory = CouchSummaryFactory.getInstance();
		Summary summary;

		// CouchDB
		HttpClient httpClient = new StdHttpClient.Builder().url("http://localhost:5984").build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		CouchDbConnector db = new StdCouchDbConnector("test", dbInstance);
		db.createDatabaseIfNotExists();
		ViewQuery q = new ViewQuery().allDocs().includeDocs(true);
		ViewResult view = db.queryView(q);

		// Generation du résumé
		// summary = factory.generate(view.getRows());

		// Enregistrement du résumé
		// OutputStream out = new
		// FileOutputStream("src/test/ressources/summary/testsummary.json");
		// factory.save(out, summary);
		// out.close();

		// Chargement du résumé
		InputStream in = new FileInputStream("src/test/ressources/summary/TestSummary.json");
		summary = factory.load(in);
		in.close();

		// Chemins
		ArrayList<String> paths = automaton.getPathsFromSummary(summary);
		System.out.println("Regex: " + regex);
		System.out.println("Valid paths:");
		for(String s : paths)
			System.out.println(s);

		// Requêtes
		System.out.println("\nCouchDB queries:");
		ArrayList<String> queries = CouchQueryFactory.getInstance().getQueries(paths, summary);
		for(String s : queries)
			System.out.println(s);
	}

}
